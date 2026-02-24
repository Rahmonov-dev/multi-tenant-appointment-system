package org.architect.multitenantappointmentsystem.service.interfaces;

import org.architect.multitenantappointmentsystem.dto.request.CreateTenantRequest;
import org.architect.multitenantappointmentsystem.dto.response.TenantResponse;
import org.architect.multitenantappointmentsystem.dto.request.UpdateTenantRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TenantService {
    TenantResponse createTenant(CreateTenantRequest request);
    TenantResponse getTenantById(UUID id);
    TenantResponse getTenantBySlug(String slug);
    TenantResponse updateTenant(UUID id, UpdateTenantRequest request);
    void deleteTenant(UUID id);
    Page<TenantResponse> getAllTenants(Pageable pageable);
    Page<TenantResponse> searchTenants(String q, Pageable pageable);
}

