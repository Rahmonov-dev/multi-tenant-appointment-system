package org.architect.multitenantappointmentsystem.service;

import lombok.RequiredArgsConstructor;
import org.architect.multitenantappointmentsystem.dto.request.CreateTenantRequest;
import org.architect.multitenantappointmentsystem.dto.response.TenantResponse;
import org.architect.multitenantappointmentsystem.dto.request.UpdateTenantRequest;
import org.architect.multitenantappointmentsystem.entity.Staff;
import org.architect.multitenantappointmentsystem.entity.StaffRole;
import org.architect.multitenantappointmentsystem.entity.Tenant;
import org.architect.multitenantappointmentsystem.entity.User;
import org.architect.multitenantappointmentsystem.exception.BadRequestException;
import org.architect.multitenantappointmentsystem.exception.BusinessException;
import org.architect.multitenantappointmentsystem.exception.NotFoundException;
import org.architect.multitenantappointmentsystem.repository.StaffRepository;
import org.architect.multitenantappointmentsystem.repository.TenantRepository;
import org.architect.multitenantappointmentsystem.repository.UserRepository;
import org.architect.multitenantappointmentsystem.security.AuthUser;
import org.architect.multitenantappointmentsystem.service.interfaces.AuthService;
import org.architect.multitenantappointmentsystem.service.interfaces.TenantService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final StaffRepository staffRepository;
    private final StaffServiceImpl staffServiceImpl;

    @Override
    @Transactional
    public TenantResponse createTenant(CreateTenantRequest request) {
        AuthUser user = AuthService.getCurrentUser().get();
        if (user.hasRole("OWNER")) {
            throw new BadRequestException("Sizda allaqachon organizatsya bor faqat 1 marta yaratsh mumkin: ");
        }

        String slug = generateUniqueSlug(request.organizationName());

        Tenant tenant = new Tenant();
        tenant.setSlug(slug);
        tenant.setBusinessType(request.businessType());
        tenant.setOrganizationName(request.organizationName());
        tenant.setEmail(request.email());
        tenant.setPhone(request.phone());
        tenant.setAddress(request.address());
        tenant.setWorkingHoursStart(request.workingHoursStart());
        tenant.setWorkingHoursEnd(request.workingHoursEnd());
        tenant.setSlotDuration(request.slotDuration());
        tenant.setAdvanceBookingDays(request.advanceBookingDays());
        tenant.setAutoConfirmBooking(
                request.autoConfirmBooking() != null ? request.autoConfirmBooking() : false);
        tenant.setTimezone(
                request.timezone() != null ? request.timezone() : "Asia/Tashkent");

        tenant = tenantRepository.save(tenant);
        User currentUser = userRepository.findById(user.getUserId()).orElseThrow(
                () -> new NotFoundException("User topilmadi"));
        Staff owner = new Staff();
        owner.setTenant(tenant);
        owner.setUser(currentUser);
        owner.setRole(StaffRole.OWNER);
        owner.setDisplayName(
                currentUser.getFirstName() + " " + currentUser.getLastName());
        owner.setPosition("Owner");
        owner.setIsActive(true);

        staffRepository.save(owner);

        return TenantResponse.from(tenant);

    }

    private String generateUniqueSlug(String organizationName) {
        String baseSlug = Tenant.generateBaseSlug(organizationName);

        String finalSlug = baseSlug;
        int counter = 2;

        while (tenantRepository.existsBySlug(finalSlug)) {
            finalSlug = baseSlug + "-" + counter;
            counter++;
        }

        return finalSlug;
    }

    @Override
    public TenantResponse getTenantById(Long id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tenant topilmadi: " + id));
        if (!tenant.getIsActive()) {
            throw new BusinessException("Tenant faol emas");
        }
        return TenantResponse.from(tenant);
    }

    @Override
    public TenantResponse getTenantByKey(String tenantKey) {
        Tenant tenant = tenantRepository.findByTenantKey(tenantKey)
                .orElseThrow(() -> new NotFoundException("Tenant topilmadi: " + tenantKey));
        return TenantResponse.from(tenant);
    }

    @Override
    public TenantResponse getTenantBySlug(String slug) {
        Tenant tenant = tenantRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("Tenant topilmadi: " + slug));
        return TenantResponse.from(tenant);
    }

    @Override
    public TenantResponse updateTenant(Long id, UpdateTenantRequest request) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tenant topilmadi: " + id));

        if (request.organizationName() != null) {
            tenant.setOrganizationName(request.organizationName());
        }
        if (request.email() != null) {
            tenant.setEmail(request.email());
        }
        if (request.phone() != null) {
            tenant.setPhone(request.phone());
        }
        if (request.address() != null) {
            tenant.setAddress(request.address());
        }
        if (request.workingHoursStart() != null) {
            tenant.setWorkingHoursStart(request.workingHoursStart());
        }
        if (request.workingHoursEnd() != null) {
            tenant.setWorkingHoursEnd(request.workingHoursEnd());
        }
        if (request.slotDuration() != null) {
            tenant.setSlotDuration(request.slotDuration());
        }
        if (request.advanceBookingDays() != null) {
            tenant.setAdvanceBookingDays(request.advanceBookingDays());
        }
        if (request.autoConfirmBooking() != null) {
            tenant.setAutoConfirmBooking(request.autoConfirmBooking());
        }

        tenant = tenantRepository.save(tenant);
        return TenantResponse.from(tenant);
    }

    @Override
    public void deleteTenant(Long id) {
        Tenant tenant = tenantRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Tenant Topilmadi" + id));

        tenant.setIsActive(false);
        tenantRepository.save(tenant);
    }

    @Override
    public Page<TenantResponse> getAllTenants(Pageable pageable) {
        Page<Tenant> tenants = tenantRepository.findAll(pageable);

        List<TenantResponse> activeTenants = tenants.getContent().stream()
                .filter(Tenant::getIsActive)
                .map(TenantResponse::from)
                .toList();

        return new PageImpl<>(activeTenants, pageable, tenants.getTotalElements());
    }
}
