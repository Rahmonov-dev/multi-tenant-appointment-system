package org.architect.multitenantappointmentsystem.service;

import org.architect.multitenantappointmentsystem.dto.request.CreateTenantRequest;
import org.architect.multitenantappointmentsystem.dto.response.TenantResponse;
import org.architect.multitenantappointmentsystem.entity.BusinessType;
import org.architect.multitenantappointmentsystem.entity.Tenant;
import org.architect.multitenantappointmentsystem.entity.User;
import org.architect.multitenantappointmentsystem.exception.BadRequestException;
import org.architect.multitenantappointmentsystem.repository.StaffRepository;
import org.architect.multitenantappointmentsystem.repository.TenantRepository;
import org.architect.multitenantappointmentsystem.repository.UserRepository;
import org.architect.multitenantappointmentsystem.security.AuthUser;
import org.architect.multitenantappointmentsystem.service.interfaces.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantServiceImplTest {

    @Mock private TenantRepository tenantRepository;
    @Mock private UserRepository userRepository;
    @Mock private StaffRepository staffRepository;

    @InjectMocks
    private TenantServiceImpl tenantService;

    // Test uchun kerakli ma'lumotlar
    private UUID userId;
    private CreateTenantRequest request;
    private User currentUser;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        // Haqiqiy User entity — tenantService ichida getFirstName(), getLastName() chaqiriladi
        currentUser = new User();
        currentUser.setId(userId);
        currentUser.setFirstName("Ali");
        currentUser.setLastName("Karimov");
        currentUser.setEmail("ali@example.com");

        // Tenant yaratish so'rovi
        request = new CreateTenantRequest(
                BusinessType.BEAUTY_SALON,
                "Test Salon",
                "salon@example.com",
                "+998901234567",
                "Toshkent, Chilonzor 5",
                LocalTime.of(9, 0),
                LocalTime.of(18, 0),
                30,
                14,
                false,
                "Asia/Tashkent"
        );
    }

    // =========================================================
    // createTenant() — muvaffaqiyatli holat
    // =========================================================

    @Test
    @DisplayName("✅ OWNER roli yo'q user tenant yaratsa — muvaffaqiyatli bo'lishi kerak")
    void createTenant_WhenUserHasNoOwnerRole_ShouldCreateSuccessfully() {

        // AuthUser — OWNER roli YO'Q (bo'sh ro'yxat)
        AuthUser authUser = AuthUser.createWithoutPassword(
                userId,
                "ali@example.com",
                List.of(),          // tenantIds
                List.of(),          // staffIds
                List.of(),          // roles — OWNER YO'Q
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        // Tenant save() ga soxta ID bersin
        Tenant savedTenant = new Tenant();
        savedTenant.setId(UUID.randomUUID());
        savedTenant.setOrganizationName("Test Salon");
        savedTenant.setSlug("test-salon");
        savedTenant.setBusinessType(BusinessType.BEAUTY_SALON);
        savedTenant.setEmail("salon@example.com");
        savedTenant.setPhone("+998901234567");
        savedTenant.setAddress("Toshkent, Chilonzor 5");
        savedTenant.setWorkingHoursStart(LocalTime.of(9, 0));
        savedTenant.setWorkingHoursEnd(LocalTime.of(18, 0));
        savedTenant.setSlotDuration(30);
        savedTenant.setAdvanceBookingDays(14);
        savedTenant.setAutoConfirmBooking(false);
        savedTenant.setTimezone("Asia/Tashkent");
        savedTenant.setIsActive(true);

        // mockStatic — AuthService.getCurrentUser() static metodini mock qilish
        // try-with-resources: try blok tugagach mock avtomatik o'chiriladi
        try (MockedStatic<AuthService> authMock = mockStatic(AuthService.class)) {

            // "AuthService.getCurrentUser() chaqirilsa authUser ni qaytarsin"
            authMock.when(AuthService::getCurrentUser)
                    .thenReturn(Optional.of(authUser));

            // "test-salon" slug boshqa tenantda yo'q → unique
            when(tenantRepository.existsBySlug(anyString())).thenReturn(false);
            when(tenantRepository.save(any(Tenant.class))).thenReturn(savedTenant);
            when(userRepository.findById(userId)).thenReturn(Optional.of(currentUser));
            when(staffRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // ACT
            TenantResponse result = tenantService.createTenant(request);

            // ASSERT
            assertThat(result).isNotNull();
            assertThat(result.organizationName()).isEqualTo("Test Salon");

            // tenant va staff saqlanganini tekshirish
            verify(tenantRepository, times(1)).save(any(Tenant.class));
            verify(staffRepository, times(1)).save(any());
        }
    }

    // =========================================================
    // createTenant() — OWNER roli bor holat
    // =========================================================

    @Test
    @DisplayName("❌ OWNER roli bor user tenant yaratmoqchi bo'lsa — BadRequestException chiqishi kerak")
    void createTenant_WhenUserAlreadyOwner_ShouldThrowBadRequestException() {

        // AuthUser — OWNER roli BOR
        AuthUser authUser = AuthUser.createWithoutPassword(
                userId,
                "ali@example.com",
                List.of(),
                List.of(),
                List.of("OWNER"),   // ← OWNER roli bor
                List.of(new SimpleGrantedAuthority("ROLE_OWNER"))
        );

        try (MockedStatic<AuthService> authMock = mockStatic(AuthService.class)) {

            authMock.when(AuthService::getCurrentUser)
                    .thenReturn(Optional.of(authUser));

            // ASSERT — exception chiqishi kerak
            assertThatThrownBy(() -> tenantService.createTenant(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("allaqachon organizatsya bor");

            // Hech narsa saqlanmasligi kerak
            verify(tenantRepository, never()).save(any());
            verify(staffRepository, never()).save(any());
        }
    }
}
