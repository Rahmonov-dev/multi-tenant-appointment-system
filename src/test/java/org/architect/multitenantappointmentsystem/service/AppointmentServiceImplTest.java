package org.architect.multitenantappointmentsystem.service;

// =====================================================================
//  DARS 1: UNIT TEST ASOSLARI
//  Mockito + JUnit 5 bilan AppointmentService ni testlash
// =====================================================================
//
//  GLOSSARIY:
//  @Test          — bu metod test ekanligini bildiradi
//  @Mock          — soxta (fake) obyekt. Real DB ga bormaydi
//  @InjectMocks   — testlanadigan asosiy klass. @Mock lar unga inject bo'ladi
//  when(...).thenReturn(...) — "agar bu chaqirilsa, shuni qaytarsin"
//  assertThat(...)  — natijani tekshirish
//  verify(...)      — metodning chaqirilgan-chaqirilmaganini tekshirish
//
//  AAA PATTERN (har bir testda):
//  ARRANGE — ma'lumotlarni tayyorlash
//  ACT     — metodini chaqirish
//  ASSERT  — natijani tekshirish
// =====================================================================

import org.architect.multitenantappointmentsystem.dto.request.CreateAppointmentRequest;
import org.architect.multitenantappointmentsystem.dto.response.AppointmentResponse;
import org.architect.multitenantappointmentsystem.entity.*;
import org.architect.multitenantappointmentsystem.exception.BusinessException;
import org.architect.multitenantappointmentsystem.exception.NotFoundException;
import org.architect.multitenantappointmentsystem.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// ── @ExtendWith: JUnit 5 ga Mockito ni ulaydi
@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {

    // ── @Mock: bular SOXTA obyektlar. Hech qanday real logika ishlamaydi.
    //    Biz o'zimiz "agar X so'ralsa, Y qaytarsin" deb ko'rsatamiz.
    @Mock private AppointmentRepository appointmentRepository;
    @Mock private TenantRepository tenantRepository;
    @Mock private StaffRepository staffRepository;
    @Mock private ServiceRepository serviceRepository;
    @Mock private StaffScheduleRepository staffScheduleRepository;
    @Mock private CurrentStaffService currentStaffService;

    // ── @InjectMocks: HAQIQIY klass. Yuqoridagi @Mock lar konstruktor orqali unga beriladi.
    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    // ── Test ma'lumotlari (har bir testda qayta ishlatiladi)
    private UUID tenantId;
    private UUID staffId;
    private UUID serviceId;
    private Tenant tenant;
    private Staff staff;
    private Employement service;
    private StaffSchedule schedule;

    // ── @BeforeEach: har bir @Test DAN OLDIN ishga tushadi
    //    Bu yerda umumiy ma'lumotlarni bir marta tayyorlaymiz
    @BeforeEach
    void setUp() {
        tenantId  = UUID.randomUUID();
        staffId   = UUID.randomUUID();
        serviceId = UUID.randomUUID();

        // Tenant obyektini yaratish
        tenant = new Tenant();
        tenant.setId(tenantId);
        tenant.setOrganizationName("Test Salon");
        tenant.setAdvanceBookingDays(30);          // 30 kun oldinga bron qilsa bo'ladi
        tenant.setAutoConfirmBooking(false);       // qo'lda tasdiqlash

        // Staff obyektini yaratish
        staff = new Staff();
        staff.setId(staffId);
        staff.setTenant(tenant);                   // shu tenantga tegishli
        staff.setDisplayName("Ali Karimov");
        staff.setIsActive(true);

        // Xizmat (service) obyektini yaratish
        service = new Employement();
        service.setId(serviceId);
        service.setTenant(tenant);
        service.setName("Soch olish");
        service.setDuration(30);                   // 30 daqiqa
        service.setPrice(new BigDecimal("50000")); // 50,000 so'm

        // Jadval: dushanba 09:00-18:00
        schedule = new StaffSchedule();
        schedule.setDayOfWeek(1);                  // 1 = Dushanba
        schedule.setIsAvailable(true);
        schedule.setStartTime(LocalTime.of(9, 0));
        schedule.setEndTime(LocalTime.of(18, 0));
    }

    // =====================================================================
    //  @Nested — testlarni mantiqiy guruhlash uchun
    //  Har bir ichki klass bitta ssenariy guruhini ifodalaydi
    // =====================================================================
    @Nested
    @DisplayName("createAppointment() testlari")
    class CreateAppointmentTests {

        // ── Muvaffaqiyatli ssenariy: barcha ma'lumotlar to'g'ri
        @Test
        @DisplayName("✅ To'g'ri so'rov kelganda appointment yaratilishi kerak")
        void createAppointment_WhenValidRequest_ShouldReturnResponse() {

            // ── ARRANGE: mock larga "qanday javob bersin" deb o'rgatamiz
            //
            //    when(mock.metod(argument)).thenReturn(natija)
            //    ya'ni: "agar tenantRepository.findById(tenantId) chaqirilsa,
            //             Optional.of(tenant) ni qaytarsin"

            when(tenantRepository.findById(tenantId))
                    .thenReturn(Optional.of(tenant));
            when(staffRepository.findById(staffId))
                    .thenReturn(Optional.of(staff));
            when(serviceRepository.findByIdAndTenantId(serviceId, tenantId))
                    .thenReturn(Optional.of(service));

            // Jadval: kelasi dushanba uchun
            LocalDate nextMonday = LocalDate.now().plusDays(
                    (8 - LocalDate.now().getDayOfWeek().getValue()) % 7 + 1
            );
            when(staffScheduleRepository.findByStaffIdAndDayOfWeek(staffId, nextMonday.getDayOfWeek().getValue()))
                    .thenReturn(Optional.of(schedule));

            // Vaqt nizosi yo'q
            when(appointmentRepository.hasTimeConflict(any(), any(), any(), any()))
                    .thenReturn(false);

            // save() chaqirilganda appointment ni o'zini qaytarsin
            when(appointmentRepository.save(any(Appointment.class)))
                    .thenAnswer(invocation -> {
                        // invocation.getArgument(0) — save() ga berilgan birinchi argument
                        Appointment saved = invocation.getArgument(0);
                        saved.setId(UUID.randomUUID()); // DB kabi ID beradi
                        return saved;
                    });

            CreateAppointmentRequest request = new CreateAppointmentRequest(
                    staffId,
                    serviceId,
                    "Bobur Toshmatov",
                    "+998901234567",
                    "bobur@example.com",
                    nextMonday,
                    LocalTime.of(10, 0),   // 10:00 — ish vaqtida
                    "Izoh yo'q"
            );

            // ── ACT: haqiqiy metodini chaqiramiz
            AppointmentResponse result = appointmentService.createAppointment(tenantId, request);

            // ── ASSERT: natijani tekshiramiz
            //    assertThat(actual).isNotNull()        — null emas
            //    assertThat(actual).isEqualTo(expected) — teng
            //    assertThat(actual).isTrue()            — true
            assertThat(result).isNotNull();
            assertThat(result.customerName()).isEqualTo("Bobur Toshmatov");
            assertThat(result.customerPhone()).isEqualTo("+998901234567");
            assertThat(result.staffId()).isEqualTo(staffId);
            assertThat(result.serviceId()).isEqualTo(serviceId);
            assertThat(result.status()).isEqualTo("PENDING"); // autoConfirm = false

            // ── VERIFY: appointmentRepository.save() bir marta chaqirilganini tekshirish
            //    verify(mock, times(1)) — aynan 1 marta chaqirilishi kerak
            //    verify(mock, never())  — hech chaqirilmasligi kerak
            verify(appointmentRepository, times(1)).save(any(Appointment.class));
        }

        // ── AutoConfirm yoqilganda CONFIRMED status berilishi kerak
        @Test
        @DisplayName("✅ autoConfirmBooking=true bo'lsa status CONFIRMED bo'lishi kerak")
        void createAppointment_WhenAutoConfirm_ShouldReturnConfirmedStatus() {

            tenant.setAutoConfirmBooking(true); // AutoConfirm yoqildi

            LocalDate nextMonday = LocalDate.now().plusDays(
                    (8 - LocalDate.now().getDayOfWeek().getValue()) % 7 + 1
            );

            when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
            when(staffRepository.findById(staffId)).thenReturn(Optional.of(staff));
            when(serviceRepository.findByIdAndTenantId(serviceId, tenantId)).thenReturn(Optional.of(service));
            when(staffScheduleRepository.findByStaffIdAndDayOfWeek(staffId, nextMonday.getDayOfWeek().getValue()))
                    .thenReturn(Optional.of(schedule));
            when(appointmentRepository.hasTimeConflict(any(), any(), any(), any())).thenReturn(false);
            when(appointmentRepository.save(any())).thenAnswer(inv -> {
                Appointment a = inv.getArgument(0);
                a.setId(UUID.randomUUID());
                return a;
            });

            CreateAppointmentRequest request = new CreateAppointmentRequest(
                    staffId, serviceId, "Test User", "+998901234567",
                    null, nextMonday, LocalTime.of(10, 0), null
            );

            AppointmentResponse result = appointmentService.createAppointment(tenantId, request);

            assertThat(result.status()).isEqualTo("CONFIRMED"); // ← muhim tekshiruv
        }

        // =====================================================================
        //  XATO HOLATLARI: biznes mantiq tekshiruvi
        //  assertThatThrownBy — exception chiqishini tekshiradi
        // =====================================================================

        @Test
        @DisplayName("❌ Tenant topilmasa NotFoundException chiqishi kerak")
        void createAppointment_WhenTenantNotFound_ShouldThrowNotFoundException() {

            // findById bo'sh Optional qaytarsin → NotFoundException chiqadi
            when(tenantRepository.findById(tenantId)).thenReturn(Optional.empty());

            CreateAppointmentRequest request = new CreateAppointmentRequest(
                    staffId, serviceId, "Test", "+998901234567",
                    null, LocalDate.now().plusDays(1), LocalTime.of(10, 0), null
            );

            // ── assertThatThrownBy: bu lambda exception chiqarishini kutadi
            assertThatThrownBy(() -> appointmentService.createAppointment(tenantId, request))
                    .isInstanceOf(NotFoundException.class)   // qaysi exception turi
                    .hasMessage("Tenant topilmadi");         // message to'g'riligini tekshirish

            // Muhim: appointment HECH saqlanmasligi kerak
            verify(appointmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("❌ Staff boshqa tenantga tegishli bo'lsa BusinessException chiqishi kerak")
        void createAppointment_WhenStaffBelongsToDifferentTenant_ShouldThrowBusinessException() {

            // Staff boshqa tenantga tegishli
            Tenant otherTenant = new Tenant();
            otherTenant.setId(UUID.randomUUID()); // BOSHQA tenant ID
            staff.setTenant(otherTenant);         // ← staff boshqa tenantda

            when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
            when(staffRepository.findById(staffId)).thenReturn(Optional.of(staff));
            when(serviceRepository.findByIdAndTenantId(serviceId, tenantId)).thenReturn(Optional.of(service));

            CreateAppointmentRequest request = new CreateAppointmentRequest(
                    staffId, serviceId, "Test", "+998901234567",
                    null, LocalDate.now().plusDays(1), LocalTime.of(10, 0), null
            );

            assertThatThrownBy(() -> appointmentService.createAppointment(tenantId, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Staff boshqa tenantga tegishli");

            verify(appointmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("❌ 30 kundan ortiq oldinga bron qilinsa BusinessException chiqishi kerak")
        void createAppointment_WhenDateTooFarInFuture_ShouldThrowBusinessException() {

            when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
            when(staffRepository.findById(staffId)).thenReturn(Optional.of(staff));
            when(serviceRepository.findByIdAndTenantId(serviceId, tenantId)).thenReturn(Optional.of(service));

            // 31 kun keyin — limit 30 kun
            LocalDate tooFar = LocalDate.now().plusDays(31);

            CreateAppointmentRequest request = new CreateAppointmentRequest(
                    staffId, serviceId, "Test", "+998901234567",
                    null, tooFar, LocalTime.of(10, 0), null
            );

            assertThatThrownBy(() -> appointmentService.createAppointment(tenantId, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("30 kun"); // xabar ichida "30 kun" bor

            verify(appointmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("❌ Staff shu kuni ishlamasa BusinessException chiqishi kerak")
        void createAppointment_WhenStaffUnavailable_ShouldThrowBusinessException() {

            schedule.setIsAvailable(false); // ← dam olish kuni

            LocalDate nextMonday = LocalDate.now().plusDays(
                    (8 - LocalDate.now().getDayOfWeek().getValue()) % 7 + 1
            );

            when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
            when(staffRepository.findById(staffId)).thenReturn(Optional.of(staff));
            when(serviceRepository.findByIdAndTenantId(serviceId, tenantId)).thenReturn(Optional.of(service));
            when(staffScheduleRepository.findByStaffIdAndDayOfWeek(staffId, nextMonday.getDayOfWeek().getValue()))
                    .thenReturn(Optional.of(schedule));

            CreateAppointmentRequest request = new CreateAppointmentRequest(
                    staffId, serviceId, "Test", "+998901234567",
                    null, nextMonday, LocalTime.of(10, 0), null
            );

            assertThatThrownBy(() -> appointmentService.createAppointment(tenantId, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Staff shu kuni ishlamaydi");
        }

        @Test
        @DisplayName("❌ Vaqt band bo'lsa BusinessException chiqishi kerak")
        void createAppointment_WhenTimeConflict_ShouldThrowBusinessException() {

            LocalDate nextMonday = LocalDate.now().plusDays(
                    (8 - LocalDate.now().getDayOfWeek().getValue()) % 7 + 1
            );

            when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
            when(staffRepository.findById(staffId)).thenReturn(Optional.of(staff));
            when(serviceRepository.findByIdAndTenantId(serviceId, tenantId)).thenReturn(Optional.of(service));
            when(staffScheduleRepository.findByStaffIdAndDayOfWeek(staffId, nextMonday.getDayOfWeek().getValue()))
                    .thenReturn(Optional.of(schedule));

            // ← Vaqt band!
            when(appointmentRepository.hasTimeConflict(any(), any(), any(), any())).thenReturn(true);

            CreateAppointmentRequest request = new CreateAppointmentRequest(
                    staffId, serviceId, "Test", "+998901234567",
                    null, nextMonday, LocalTime.of(10, 0), null
            );

            assertThatThrownBy(() -> appointmentService.createAppointment(tenantId, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Bu vaqt allaqachon band");

            verify(appointmentRepository, never()).save(any());
        }
    }

    // =====================================================================
    //  DARS 2: getAppointmentById() — oddiy metod uchun test
    // =====================================================================
    @Nested
    @DisplayName("getAppointmentById() testlari")
    class GetAppointmentByIdTests {

        @Test
        @DisplayName("✅ Topilganda AppointmentResponse qaytarishi kerak")
        void getById_WhenFound_ShouldReturnResponse() {

            UUID appointmentId = UUID.randomUUID();

            Appointment appointment = new Appointment();
            appointment.setId(appointmentId);
            appointment.setTenant(tenant);
            appointment.setStaff(staff);
            appointment.setEmployement(service);
            appointment.setCustomerName("Dilnoza");
            appointment.setCustomerPhone("+998901112233");
            appointment.setAppointmentDate(LocalDate.now().plusDays(1));
            appointment.setStartTime(LocalTime.of(11, 0));
            appointment.setEndTime(LocalTime.of(11, 30));
            appointment.setStatus(AppointmentStatus.PENDING);
            appointment.setTotalPrice(new BigDecimal("50000"));

            when(appointmentRepository.findByIdAndTenantId(appointmentId, tenantId))
                    .thenReturn(Optional.of(appointment));

            AppointmentResponse result = appointmentService.getAppointmentById(tenantId, appointmentId);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(appointmentId);
            assertThat(result.customerName()).isEqualTo("Dilnoza");
        }

        @Test
        @DisplayName("❌ Topilmasa NotFoundException chiqishi kerak")
        void getById_WhenNotFound_ShouldThrowNotFoundException() {

            UUID wrongId = UUID.randomUUID();
            when(appointmentRepository.findByIdAndTenantId(wrongId, tenantId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> appointmentService.getAppointmentById(tenantId, wrongId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Appointment topilmadi");
        }
    }
}
