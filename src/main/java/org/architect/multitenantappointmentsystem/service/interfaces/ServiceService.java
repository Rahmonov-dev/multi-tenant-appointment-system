package org.architect.multitenantappointmentsystem.service.interfaces;

import org.architect.multitenantappointmentsystem.dto.request.CreateServiceRequest;
import org.architect.multitenantappointmentsystem.dto.request.UpdateServiceRequest;
import org.architect.multitenantappointmentsystem.dto.response.ServiceDetailResponse;
import org.architect.multitenantappointmentsystem.dto.response.ServiceResponse;
import org.architect.multitenantappointmentsystem.dto.response.ServiceStatisticsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Employement management service interface
 */
public interface ServiceService {

    // ==================== CRUD OPERATIONS ====================

    ServiceResponse createService(UUID tenantId, CreateServiceRequest request);
    ServiceResponse getServiceById(UUID tenantId, UUID id);
    ServiceDetailResponse getServiceDetailById(UUID tenantId, UUID id);
    ServiceResponse updateService(UUID tenantId, UUID id, UpdateServiceRequest request);
    void deleteService(UUID tenantId, UUID id);
    ServiceResponse activateService(UUID tenantId, UUID id);
    ServiceResponse deactivateService(UUID tenantId, UUID id);

    // ==================== QUERY OPERATIONS ====================

    List<ServiceResponse> getAllServicesByTenant(UUID tenantId);
    List<ServiceResponse> getActiveServicesByTenant(UUID tenantId);
    List<ServiceResponse> getServicesByTenantOrdered(UUID tenantId, Boolean activeOnly);
    Page<ServiceResponse> getServicesByTenantPaginated(UUID tenantId, Boolean activeOnly, Pageable pageable);
    List<ServiceResponse> searchServices(UUID tenantId, String keyword, Boolean activeOnly);
    List<ServiceResponse> getServicesByPriceRange(UUID tenantId, BigDecimal minPrice, BigDecimal maxPrice);
    List<ServiceResponse> getServicesByMaxDuration(UUID tenantId, Integer maxDuration);
    List<ServiceResponse> getServicesByStaff(UUID tenantId, UUID staffId);
    List<ServiceResponse> getPopularServices(UUID tenantId, Integer limit);

    // ==================== STAFF ASSIGNMENT ====================

    ServiceResponse assignStaffToService(UUID tenantId, UUID serviceId, UUID staffId);
    ServiceResponse removeStaffFromService(UUID tenantId, UUID serviceId, UUID staffId);
    ServiceResponse assignStaffsToService(UUID tenantId, UUID serviceId, List<UUID> staffIds);
    ServiceResponse removeAllStaffFromService(UUID tenantId, UUID serviceId);

    // ==================== DISPLAY ORDER ====================

    ServiceResponse updateDisplayOrder(UUID tenantId, UUID id, Integer displayOrder);
    void updateMultipleDisplayOrders(UUID tenantId, List<UUID> serviceIds);

    // ==================== STATISTICS ====================

    ServiceStatisticsResponse getServiceStatistics(UUID tenantId);
}
