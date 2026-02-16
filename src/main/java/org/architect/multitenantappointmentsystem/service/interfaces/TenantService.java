package org.architect.multitenantappointmentsystem.service.interfaces;

import org.architect.multitenantappointmentsystem.dto.request.CreateTenantRequest;
import org.architect.multitenantappointmentsystem.dto.response.TenantResponse;
import org.architect.multitenantappointmentsystem.dto.request.UpdateTenantRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TenantService {
    TenantResponse createTenant(CreateTenantRequest request);
    TenantResponse getTenantById(Long id);
    TenantResponse getTenantByKey(String tenantKey);
    TenantResponse getTenantBySlug(String slug);
    TenantResponse updateTenant(Long id, UpdateTenantRequest request);
    void deleteTenant(Long id);
    Page<TenantResponse> getAllTenants(Pageable pageable);
}
