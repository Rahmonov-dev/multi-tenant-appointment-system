package org.architect.multitenantappointmentsystem.repository;

// =====================================================================
//  DARS 2: @DataJpaTest — Repository ni real H2 DB da test qilish
// =====================================================================
//
//  @DataJpaTest nima qiladi?
//  ─────────────────────────
//  ✅ Faqat JPA qatlamini yuklaydi (Entity, Repository)
//  ✅ H2 in-memory DB ni avtomatik sozlaydi
//  ✅ Flyway/Liquibase ni o'chiradi
//  ✅ Hibernate DDL orqali schemani avtomatik yaratadi
//  ✅ Har bir test @Transactional — test tugagach rollback qiladi
//  ❌ Web qatlamini YUKLMAYDI (Controller, Service, Security yo'q)
//
//  QO'SHIMCHA SO'Z:
//  Unit testdan farqi — bu yerda HAQIQIY SQL so'rovlar ishlaydi.
//  Mock yo'q. DB ga yozamiz, DB dan o'qiymiz.
//  Bu turdagi testlar Repository @Query metodlari to'g'ri
//  yozilganligini tekshiradi.
// =====================================================================

import org.architect.multitenantappointmentsystem.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

// ── @DataJpaTest: faqat JPA qatlamini yuklaydi, H2 avtomatik uladi
// ── @ActiveProfiles("test"): application-test.yaml ni ishlatadi
@DataJpaTest
@ActiveProfiles("test")
class AppointmentRepositoryTest {

    // ── TestEntityManager — test uchun mo'ljallangan EntityManager
    //    Biz test ma'lumotlarini shu orqali DB ga yozamiz
    @Autowired
    private TestEntityManager entityManager;

    // ── Haqiqiy Repository (mock emas!)
    @Autowired
    private AppointmentRepository appointmentRepository;

    // Test ma'lumotlari — @BeforeEach da saqlanadi
    private Tenant tenant;
    private Staff staff;
    private Employement service;
    private LocalDate today;

    // ── @BeforeEach: har bir testdan OLDIN DB ga test ma'lumotlarini yozadi
    //    @DataJpaTest @Transactional bo'lgani uchun, har test tugagach
    //    bu ma'lumotlar ROLLBACK qilinadi → testlar bir-birini buzmayd
    @BeforeEach
    void setUp() {
        today = LocalDate.now();

        // 1. User yaratish (Staff uchun kerak)
        User user = new User();
        user.setFirstName("Ali");
        user.setLastName("Karimov");
        user.setEmail("ali@test.com");
        user.setPasswordHash("hashed_password");
        user.setPhone("+998901234567");
        // persistAndFlush — DB ga yozadi va darhol FLUSH qiladi (SQL ishga tushadi)
        user = entityManager.persistAndFlush(user);

        // 2. Tenant yaratish
        tenant = new Tenant();
        tenant.setSlug("test-salon");
        tenant.setBusinessType(BusinessType.BEAUTY_SALON);
        tenant.setOrganizationName("Test Salon");
        tenant.setEmail("salon@test.com");
        tenant.setPhone("+998901234567");
        tenant.setAddress("Toshkent");
        tenant.setWorkingHoursStart(LocalTime.of(9, 0));
        tenant.setWorkingHoursEnd(LocalTime.of(18, 0));
        tenant.setSlotDuration(30);
        tenant.setAdvanceBookingDays(30);
        tenant.setAutoConfirmBooking(false);
        tenant.setTimezone("Asia/Tashkent");
        tenant.setIsActive(true);
        tenant = entityManager.persistAndFlush(tenant);

        // 3. Staff yaratish (User + Tenant bog'liq)
        staff = new Staff();
        staff.setUser(user);
        staff.setTenant(tenant);
        staff.setRole(StaffRole.STAFF);
        staff.setDisplayName("Ali Karimov");
        staff.setPosition("Sartarosh");
        staff.setIsActive(true);
        staff = entityManager.persistAndFlush(staff);

        // 4. Xizmat (service) yaratish
        service = new Employement();
        service.setTenant(tenant);
        service.setName("Soch olish");
        service.setDuration(30);
        service.setPrice(new BigDecimal("50000"));
        service.setIsActive(true);
        service = entityManager.persistAndFlush(service);

        // 5. Bugungi appointment yaratish
        Appointment appointment = new Appointment();
        appointment.setTenant(tenant);
        appointment.setStaff(staff);
        appointment.setEmployement(service);
        appointment.setCustomerName("Bobur Toshmatov");
        appointment.setCustomerPhone("+998901112233");
        appointment.setAppointmentDate(today);
        appointment.setStartTime(LocalTime.of(10, 0));
        appointment.setEndTime(LocalTime.of(10, 30));
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setTotalPrice(new BigDecimal("50000"));
        entityManager.persistAndFlush(appointment);

        // 6. Boshqa tenantga tegishli appointment (boshqa testlarda aralashmasligi uchun)
        Tenant otherTenant = new Tenant();
        otherTenant.setSlug("other-salon");
        otherTenant.setBusinessType(BusinessType.BEAUTY_SALON);
        otherTenant.setOrganizationName("Other Salon");
        otherTenant.setEmail("other@test.com");
        otherTenant.setPhone("+998909876543");
        otherTenant.setAddress("Samarkand");
        otherTenant.setWorkingHoursStart(LocalTime.of(9, 0));
        otherTenant.setWorkingHoursEnd(LocalTime.of(18, 0));
        otherTenant.setSlotDuration(30);
        otherTenant.setAdvanceBookingDays(30);
        otherTenant.setAutoConfirmBooking(false);
        otherTenant.setTimezone("Asia/Tashkent");
        otherTenant.setIsActive(true);
        otherTenant = entityManager.persistAndFlush(otherTenant);

        // entityManager.clear() — 1st level cache ni tozalaydi
        // Shundan keyin repository so'rovlar DB dan to'g'ridan-to'g'ri o'qiydi
        entityManager.clear();
    }

    // =====================================================================
    //  findByTenantIdAndAppointmentDate()
    //  Sana bo'yicha tenant appointmentlarini topish
    // =====================================================================
    @Nested
    @DisplayName("findByTenantIdAndAppointmentDate() testlari")
    class FindByTenantIdAndDateTests {

        @Test
        @DisplayName("✅ Bugungi appointment topilishi kerak")
        void whenTodayHasAppointment_ShouldReturnIt() {
            List<Appointment> result = appointmentRepository
                    .findByTenantIdAndAppointmentDate(tenant.getId(), today);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCustomerName()).isEqualTo("Bobur Toshmatov");
        }

        @Test
        @DisplayName("✅ Boshqa sanada appointment yo'q — bo'sh ro'yxat qaytishi kerak")
        void whenOtherDateHasNoAppointment_ShouldReturnEmpty() {
            List<Appointment> result = appointmentRepository
                    .findByTenantIdAndAppointmentDate(tenant.getId(), today.plusDays(1));

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("✅ Boshqa tenantning appointmentlari aralashmasligi kerak")
        void whenOtherTenantId_ShouldNotReturnOtherTenantsData() {
            List<Appointment> result = appointmentRepository
                    .findByTenantIdAndAppointmentDate(java.util.UUID.randomUUID(), today);

            assertThat(result).isEmpty();
        }
    }

    // =====================================================================
    //  hasTimeConflict()
    //  Vaqt to'qnashuvi tekshiruvi — booking da eng muhim metod
    // =====================================================================
    @Nested
    @DisplayName("hasTimeConflict() testlari")
    class HasTimeConflictTests {

        @Test
        @DisplayName("✅ Ayni shu vaqtda boshqa appointment bor — conflict bo'lishi kerak")
        void whenSameTimeExists_ShouldReturnTrue() {
            // DB da 10:00-10:30 appointment bor (setUp da yaratildi)
            boolean conflict = appointmentRepository.hasTimeConflict(
                    staff.getId(),
                    today,
                    LocalTime.of(10, 0),   // ayni shu vaqt
                    LocalTime.of(10, 30)
            );

            assertThat(conflict).isTrue();
        }

        @Test
        @DisplayName("✅ Vaqtlar kesishmasa — conflict bo'lmasligi kerak")
        void whenDifferentTime_ShouldReturnFalse() {
            boolean conflict = appointmentRepository.hasTimeConflict(
                    staff.getId(),
                    today,
                    LocalTime.of(11, 0),   // boshqa vaqt — 11:00-11:30
                    LocalTime.of(11, 30)
            );

            assertThat(conflict).isFalse();
        }

        @Test
        @DisplayName("✅ Boshqa kunda — conflict bo'lmasligi kerak")
        void whenDifferentDate_ShouldReturnFalse() {
            boolean conflict = appointmentRepository.hasTimeConflict(
                    staff.getId(),
                    today.plusDays(1),     // ertaga
                    LocalTime.of(10, 0),
                    LocalTime.of(10, 30)
            );

            assertThat(conflict).isFalse();
        }
    }

    // =====================================================================
    //  findTodayAppointments()
    //  Bugungi PENDING/CONFIRMED appointmentlar
    // =====================================================================
    @Nested
    @DisplayName("findTodayAppointments() testlari")
    class FindTodayAppointmentsTests {

        @Test
        @DisplayName("✅ Bugungi PENDING appointment ro'yxatda bo'lishi kerak")
        void whenPendingTodayAppointment_ShouldBeIncluded() {
            List<Appointment> result = appointmentRepository
                    .findTodayAppointments(tenant.getId());

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo(AppointmentStatus.PENDING);
        }

        @Test
        @DisplayName("✅ CANCELLED appointment bugungi ro'yxatda bo'lmasligi kerak")
        void whenCancelledAppointment_ShouldNotBeIncluded() {
            // Yangi CANCELLED appointment qo'shamiz
            Appointment cancelled = new Appointment();
            cancelled.setTenant(tenant);
            cancelled.setStaff(staff);
            cancelled.setEmployement(service);
            cancelled.setCustomerName("Bekor qilingan");
            cancelled.setCustomerPhone("+998901111111");
            cancelled.setAppointmentDate(today);
            cancelled.setStartTime(LocalTime.of(14, 0));
            cancelled.setEndTime(LocalTime.of(14, 30));
            cancelled.setStatus(AppointmentStatus.CANCELLED);  // ← CANCELLED
            cancelled.setTotalPrice(new BigDecimal("50000"));
            entityManager.persistAndFlush(cancelled);
            entityManager.clear();

            List<Appointment> result = appointmentRepository
                    .findTodayAppointments(tenant.getId());

            // Faqat PENDING/CONFIRMED ko'rinishi kerak → 1 ta (CANCELLED emas)
            assertThat(result).hasSize(1);
            assertThat(result).noneMatch(a -> a.getStatus() == AppointmentStatus.CANCELLED);
        }
    }

    // =====================================================================
    //  findByIdAndTenantId()
    //  ID va tenantId bilan topish — boshqa tenantning ma'lumotini ko'ra olmaslik
    // =====================================================================
    @Test
    @DisplayName("✅ findByIdAndTenantId — boshqa tenant ID bilan topilmasligi kerak")
    void findByIdAndTenantId_WhenWrongTenant_ShouldReturnEmpty() {
        List<Appointment> all = appointmentRepository.findAll();
        assertThat(all).isNotEmpty();

        java.util.UUID appointmentId = all.get(0).getId();

        // To'g'ri tenant → topiladi
        Optional<Appointment> found = appointmentRepository
                .findByIdAndTenantId(appointmentId, tenant.getId());
        assertThat(found).isPresent();

        // Noto'g'ri tenant → topilmaydi
        Optional<Appointment> notFound = appointmentRepository
                .findByIdAndTenantId(appointmentId, java.util.UUID.randomUUID());
        assertThat(notFound).isEmpty();
    }
}
