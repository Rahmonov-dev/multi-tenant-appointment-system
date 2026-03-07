package org.architect.multitenantappointmentsystem.integration;

// =====================================================================
//  DARS 4: @SpringBootTest — To'liq integratsiya testi
// =====================================================================
//
//  @SpringBootTest nima qiladi?
//  ─────────────────────────────
//  ✅ BUTUN Spring Context ni yuklaydi (Controller + Service + Repository + Security)
//  ✅ Haqiqiy HTTP so'rovlar (TestRestTemplate yoki MockMvc bilan)
//  ✅ Haqiqiy DB (biz H2 ni application-test.yaml da sozladik)
//  ✅ Haqiqiy biznes logika ishlaydi — hech narsa mock emas
//  ❌ Eng SEKIN tur (butun context yuklanadi)
//  ❌ Muvaffaqiyatsiz bo'lganda qayerda xato ekanini topish qiyin
//
//  QOIDASI: Faqat muhim end-to-end ssenariylarni shu bilan test qiling.
//  Barcha biznes logikani Unit test da, HTTP qatlamni WebMvcTest da test qiling.
//  SpringBootTest faqat: "Tizim umuman ishlayaptimi?" savoliga javob beradi.
//
//  Unit vs WebMvc vs SpringBoot:
//  ┌──────────────┬──────────┬────────────┬──────────────┐
//  │              │ Tezlik   │ Qamrov     │ Foydasi      │
//  ├──────────────┼──────────┼────────────┼──────────────┤
//  │ Unit         │ 🟢 Tez   │ 1 klass    │ Biznes logik │
//  │ @DataJpaTest │ 🟡 O'rta │ JPA qatlam │ SQL so'rovlar│
//  │ @WebMvcTest  │ 🟡 O'rta │ Web qatlam │ HTTP/JSON    │
//  │ @SpringBoot  │ 🔴 Sekin │ Hammasi    │ End-to-end   │
//  └──────────────┴──────────┴────────────┴──────────────┘
// =====================================================================

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.architect.multitenantappointmentsystem.entity.*;
import org.architect.multitenantappointmentsystem.repository.*;
import org.architect.multitenantappointmentsystem.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// ── @SpringBootTest: butun Spring Application Context yuklanadi
// ── @AutoConfigureMockMvc: MockMvc ni avtomatik sozlaydi
// ── @AutoConfigureTestDatabase: PostgreSQL ni H2 bilan almashtiradi
// ── @ActiveProfiles("test"): application-test.yaml dan konfiguratsiya oladi
// ── @Transactional: har test keyin rollback — DB ning tozaligini saqlaydi
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@Transactional
class AppointmentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // ── Bu HAQIQIY repository lar — H2 DB ga ulangan
    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private TenantRepository tenantRepository;
    @Autowired private StaffRepository staffRepository;
    @Autowired private ServiceRepository serviceRepository;
    @Autowired private UserRepository userRepository;

    // ── JwtService — test uchun haqiqiy JWT token generatsiya qiladi
    @Autowired
    private JwtService jwtService;

    private ObjectMapper objectMapper;
    private Tenant tenant;
    private Staff staff;
    private Employement service;
    private String jwtToken;
    private UUID userId;

    // ── @BeforeEach: har test uchun DB ga to'liq ma'lumotlar yoziladi
    //    @Transactional bo'lgani uchun har test keyin rollback qilinadi
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        userId = UUID.randomUUID();

        // 1. Haqiqiy User DB ga yoziladi
        User user = new User();
        user.setFirstName("Integration");
        user.setLastName("Test User");
        user.setEmail("integration@test.com");
        user.setPasswordHash("$2a$10$hashed_password");
        user.setPhone("+998901234567");
        user = userRepository.save(user);

        // 2. Haqiqiy Tenant DB ga yoziladi
        tenant = new Tenant();
        tenant.setSlug("integration-salon-" + UUID.randomUUID().toString().substring(0, 8));
        tenant.setBusinessType(BusinessType.BEAUTY_SALON);
        tenant.setOrganizationName("Integration Salon");
        tenant.setEmail("integration@salon.com");
        tenant.setPhone("+998901234567");
        tenant.setAddress("Toshkent");
        tenant.setWorkingHoursStart(LocalTime.of(9, 0));
        tenant.setWorkingHoursEnd(LocalTime.of(18, 0));
        tenant.setSlotDuration(30);
        tenant.setAdvanceBookingDays(30);
        tenant.setAutoConfirmBooking(false);
        tenant.setTimezone("Asia/Tashkent");
        tenant.setIsActive(true);
        tenant = tenantRepository.save(tenant);

        // 3. Haqiqiy Staff DB ga yoziladi
        staff = new Staff();
        staff.setUser(user);
        staff.setTenant(tenant);
        staff.setRole(StaffRole.STAFF);
        staff.setDisplayName("Integration Tester");
        staff.setPosition("Sartarosh");
        staff.setIsActive(true);
        staff = staffRepository.save(staff);

        // 4. Haqiqiy Xizmat DB ga yoziladi
        service = new Employement();
        service.setTenant(tenant);
        service.setName("Soch olish");
        service.setDuration(30);
        service.setPrice(new BigDecimal("50000"));
        service.setIsActive(true);
        service = serviceRepository.save(service);

        // 5. Test uchun haqiqiy JWT token generatsiya qilinadi
        //    Bu token keyingi so'rovlarda Authorization headerida ishlatiladi
        jwtToken = jwtService.generateToken(user.getEmail(), user.getId());
    }

    // =====================================================================
    //  GET /api/{tenantId}/appointments/{id}
    //  To'liq zanjir: HTTP → Controller → Service → Repository → H2 DB
    // =====================================================================

    @Test
    @DisplayName("✅ Mavjud appointment ni olish — butun zanjir ishlashi kerak")
    void getAppointment_FullChain_ShouldReturnFromRealDB() throws Exception {

        // DB ga haqiqiy appointment yozamiz
        Appointment appointment = new Appointment();
        appointment.setTenant(tenant);
        appointment.setStaff(staff);
        appointment.setEmployement(service);
        appointment.setCustomerName("Haqiqiy Mijoz");
        appointment.setCustomerPhone("+998901112233");
        appointment.setAppointmentDate(LocalDate.now().plusDays(1));
        appointment.setStartTime(LocalTime.of(10, 0));
        appointment.setEndTime(LocalTime.of(10, 30));
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setTotalPrice(new BigDecimal("50000"));
        appointment = appointmentRepository.save(appointment);

        // HTTP so'rov yuboramiz — Authorization headerida JWT token
        mockMvc.perform(
                    get("/api/{tenantId}/appointments/{id}", tenant.getId(), appointment.getId())
                    .header("Authorization", "Bearer " + jwtToken)
                )
                .andExpect(status().isOk())
                // Haqiqiy DB dan kelgan ma'lumot tekshiriladi
                .andExpect(jsonPath("$.data.customerName").value("Haqiqiy Mijoz"))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.tenantId").value(tenant.getId().toString()));
    }

    @Test
    @DisplayName("❌ Mavjud bo'lmagan appointment — 404 qaytishi kerak")
    void getAppointment_WhenNotExists_ShouldReturn404() throws Exception {

        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(
                    get("/api/{tenantId}/appointments/{id}", tenant.getId(), nonExistentId)
                    .header("Authorization", "Bearer " + jwtToken)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("❌ Token yo'q — 401 qaytishi kerak")
    void getAppointment_WithoutToken_ShouldReturn401() throws Exception {

        UUID someId = UUID.randomUUID();

        // Authorization header yo'q
        mockMvc.perform(
                    get("/api/{tenantId}/appointments/{id}", tenant.getId(), someId)
                )
                .andExpect(status().isUnauthorized());
    }

    // =====================================================================
    //  GET /api/{tenantId}/appointments/today
    //  Bugungi appointmentlar ro'yxati
    // =====================================================================

    @Test
    @DisplayName("✅ Bugungi appointmentlar ro'yxati — DB dan real ma'lumot kelishi kerak")
    void getTodayAppointments_ShouldReturnRealData() throws Exception {

        // Bugungi appointment qo'shamiz
        Appointment today = new Appointment();
        today.setTenant(tenant);
        today.setStaff(staff);
        today.setEmployement(service);
        today.setCustomerName("Bugungi Mijoz");
        today.setCustomerPhone("+998907654321");
        today.setAppointmentDate(LocalDate.now());
        today.setStartTime(LocalTime.of(14, 0));
        today.setEndTime(LocalTime.of(14, 30));
        today.setStatus(AppointmentStatus.CONFIRMED);
        today.setTotalPrice(new BigDecimal("50000"));
        appointmentRepository.save(today);

        mockMvc.perform(
                    get("/api/{tenantId}/appointments/today", tenant.getId())
                    .header("Authorization", "Bearer " + jwtToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
