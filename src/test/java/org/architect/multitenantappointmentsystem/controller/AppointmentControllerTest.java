package org.architect.multitenantappointmentsystem.controller;

// =====================================================================
//  DARS 3: @WebMvcTest — Controller ni HTTP so'rovlar orqali test
// =====================================================================
//
//  @WebMvcTest nima qiladi?
//  ─────────────────────────
//  ✅ Faqat Web qatlamini yuklaydi (Controller, Filter, Security)
//  ✅ MockMvc — real HTTP server ishga tushirmaydi, lekin HTTP ni simulyatsiya qiladi
//  ✅ Service @MockBean bilan mock qilinadi
//  ❌ DB YUKLMAYDI (Repository, JPA yo'q)
//  ❌ Service real logikasi ishlamaydi — biz o'zimiz "nima qaytarsin" deymiz
//
//  UNIT TEST bilan farqi:
//  Unit test — metodlarni to'g'ridan-to'g'ri chaqiradi
//  WebMvcTest — HTTP so'rov yuboradi, JSON javobini tekshiradi
//  Bu controller mapping, JSON serializatsiya,
//  validation va status kodlarini tekshiradi
// =====================================================================

import com.fasterxml.jackson.databind.ObjectMapper;
import org.architect.multitenantappointmentsystem.dto.request.CreateAppointmentRequest;
import org.architect.multitenantappointmentsystem.dto.response.AppointmentResponse;
import org.architect.multitenantappointmentsystem.exception.NotFoundException;
import org.architect.multitenantappointmentsystem.service.interfaces.AppointmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// ── @SpringBootTest + @AutoConfigureMockMvc: to'liq kontekst (DB, Security, Jackson)
//    @WebMvcTest dan farqi — hamma bean yuklanadi, GlobalExceptionHandler, ObjectMapper to'g'ri ishlaydi
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AppointmentService appointmentService;

    // ── @Autowired ObjectMapper: Spring Boot ning o'z ObjectMapper i
    //    LocalDate, LocalTime, LocalDateTime uchun allaqachon to'g'ri sozlangan
    @Autowired
    private ObjectMapper objectMapper;

    private UUID tenantId;
    private UUID appointmentId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        appointmentId = UUID.randomUUID();
    }

    // =====================================================================
    //  POST /api/{tenantId}/appointments — Appointment yaratish
    // =====================================================================
    @Nested
    @DisplayName("POST /api/{tenantId}/appointments")
    class CreateAppointmentTests {

        @Test
        @DisplayName("✅ To'g'ri so'rov — 200 va appointment qaytishi kerak")
        @WithMockUser // ← Spring Security uchun: "autentifikatsiya qilingan foydalanuvchi" simulyatsiyasi
        void createAppointment_WhenValidRequest_ShouldReturn200() throws Exception {

            // ARRANGE: Service mock dan qaytadigan javob
            AppointmentResponse mockResponse = new AppointmentResponse(
                    appointmentId, tenantId, "Test Salon",
                    UUID.randomUUID(), "Ali Karimov", "Sartarosh",
                    UUID.randomUUID(), "Soch olish", 30,
                    "Bobur Toshmatov", "+998901234567", "bobur@test.com",
                    LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(10, 30),
                    "10:00 - 10:30", "PENDING", "Kutilmoqda", "🕐",
                    new BigDecimal("50000"), "50,000.00", null,
                    null, null, null, null, null, null
            );

            // "createAppointment chaqirilsa mockResponse ni qaytarsin"
            when(appointmentService.createAppointment(eq(tenantId), any(CreateAppointmentRequest.class)))
                    .thenReturn(mockResponse);

            // So'rov tanasi (JSON)
            CreateAppointmentRequest request = new CreateAppointmentRequest(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    "Bobur Toshmatov",
                    "+998901234567",
                    "bobur@test.com",
                    LocalDate.now().plusDays(1),
                    LocalTime.of(10, 0),
                    null
            );

            // ACT + ASSERT: HTTP so'rov yuboramiz va javobni tekshiramiz
            mockMvc.perform(
                        // ── post(): POST so'rov yuborish
                        post("/api/{tenantId}/appointments", tenantId)
                        // ── contentType: JSON yuborayotganimizni bildiradi
                        .contentType(MediaType.APPLICATION_JSON)
                        // ── content: request body ni JSON ga aylantirib yuboramiz
                        .content(objectMapper.writeValueAsString(request))
                        // ── csrf(): CSRF token qo'shadi (Spring Security talab qiladi)
                        .with(csrf())
                    )
                    // HTTP status 200 OK bo'lishi kerak
                    .andExpect(status().isOk())
                    // ── jsonPath: JSON javob ichidagi qiymatlarni tekshiradi
                    //    "$.success" — root darajadagi "success" maydoni
                    .andExpect(jsonPath("$.success").value(true))
                    //    "$.data.customerName" — data ichidagi customerName
                    .andExpect(jsonPath("$.data.customerName").value("Bobur Toshmatov"))
                    .andExpect(jsonPath("$.data.status").value("PENDING"))
                    .andExpect(jsonPath("$.data.id").exists());
        }

        @Test
        @DisplayName("❌ Autentifikatsiz so'rov — 401 qaytishi kerak")
        void createAppointment_WhenNotAuthenticated_ShouldReturn401() throws Exception {

            CreateAppointmentRequest request = new CreateAppointmentRequest(
                    UUID.randomUUID(), UUID.randomUUID(),
                    "Test", "+998901234567", null,
                    LocalDate.now().plusDays(1), LocalTime.of(10, 0), null
            );

            // @WithMockUser YO'Q — autentifikatsiz so'rov
            mockMvc.perform(
                        post("/api/{tenantId}/appointments", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf())
                    )
                    // 401 Unauthorized qaytishi kerak
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("❌ customerName bo'sh — 400 Bad Request qaytishi kerak")
        @WithMockUser
        void createAppointment_WhenCustomerNameBlank_ShouldReturn400() throws Exception {

            // ── Noto'g'ri so'rov: customerName bo'sh string
            String invalidJson = """
                    {
                        "staffId": "%s",
                        "serviceId": "%s",
                        "customerName": "",
                        "customerPhone": "+998901234567",
                        "appointmentDate": "%s",
                        "startTime": "10:00:00"
                    }
                    """.formatted(UUID.randomUUID(), UUID.randomUUID(), LocalDate.now().plusDays(1));

            mockMvc.perform(
                        post("/api/{tenantId}/appointments", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson)
                        .with(csrf())
                    )
                    // @NotBlank validation ishladi → 400
                    .andExpect(status().isBadRequest());
        }
    }

    // =====================================================================
    //  GET /api/{tenantId}/appointments/{id} — Appointment olish
    // =====================================================================
    @Nested
    @DisplayName("GET /api/{tenantId}/appointments/{id}")
    class GetAppointmentTests {

        @Test
        @DisplayName("✅ Mavjud ID — 200 va ma'lumot qaytishi kerak")
        @WithMockUser
        void getAppointment_WhenExists_ShouldReturn200() throws Exception {

            AppointmentResponse mockResponse = new AppointmentResponse(
                    appointmentId, tenantId, "Test Salon",
                    UUID.randomUUID(), "Ali Karimov", "Sartarosh",
                    UUID.randomUUID(), "Soch olish", 30,
                    "Dilnoza Yusupova", "+998901112233", null,
                    LocalDate.now(), LocalTime.of(11, 0), LocalTime.of(11, 30),
                    "11:00 - 11:30", "CONFIRMED", "Tasdiqlangan", "✅",
                    new BigDecimal("50000"), "50,000.00", null,
                    null, null, null, null, null, null
            );

            when(appointmentService.getAppointmentById(tenantId, appointmentId))
                    .thenReturn(mockResponse);

            mockMvc.perform(
                        // ── get(): GET so'rov yuborish
                        get("/api/{tenantId}/appointments/{id}", tenantId, appointmentId)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.customerName").value("Dilnoza Yusupova"))
                    .andExpect(jsonPath("$.data.status").value("CONFIRMED"));
        }

        @Test
        @DisplayName("❌ Mavjud bo'lmagan ID — 404 qaytishi kerak")
        @WithMockUser
        void getAppointment_WhenNotFound_ShouldReturn404() throws Exception {

            UUID wrongId = UUID.randomUUID();

            // Service NotFoundException chiqarsin
            when(appointmentService.getAppointmentById(tenantId, wrongId))
                    .thenThrow(new NotFoundException("Appointment topilmadi: " + wrongId));

            mockMvc.perform(
                        get("/api/{tenantId}/appointments/{id}", tenantId, wrongId)
                    )
                    // GlobalExceptionHandler NotFoundException ni 404 ga aylantiradi
                    .andExpect(status().isNotFound());
        }
    }
}
