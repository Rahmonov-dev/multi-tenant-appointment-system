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

/**
 * Employement management service interface
 */
public interface ServiceService {

    // ==================== CRUD OPERATIONS ====================

    ServiceResponse createService(CreateServiceRequest request);
    ServiceResponse getServiceById(java.util.UUID id);
    ServiceDetailResponse getServiceDetailById(java.util.UUID id);
    ServiceResponse updateService(java.util.UUID id, UpdateServiceRequest request);
    void deleteService(java.util.UUID id);
    ServiceResponse activateService(java.util.UUID id);
    ServiceResponse deactivateService(java.util.UUID id);

    // ==================== QUERY OPERATIONS ====================

    List<ServiceResponse> getAllServicesByTenant();
    List<ServiceResponse> getActiveServicesByTenant();
    List<ServiceResponse> getServicesByTenantOrdered( Boolean activeOnly);
    Page<ServiceResponse> getServicesByTenantPaginated( Boolean activeOnly, Pageable pageable);
    List<ServiceResponse> searchServices( String keyword, Boolean activeOnly);
    List<ServiceResponse> getServicesByPriceRange( BigDecimal minPrice, BigDecimal maxPrice);
    List<ServiceResponse> getServicesByMaxDuration( Integer maxDuration);
    List<ServiceResponse> getServicesByStaff(java.util.UUID staffId);
    List<ServiceResponse> getPopularServices( Integer limit);

    // ==================== STAFF ASSIGNMENT ====================

    ServiceResponse assignStaffToService(java.util.UUID serviceId, java.util.UUID staffId);
    ServiceResponse removeStaffFromService(java.util.UUID serviceId, java.util.UUID staffId);
    ServiceResponse assignStaffsToService(java.util.UUID serviceId, List<java.util.UUID> staffIds);
    ServiceResponse removeAllStaffFromService(java.util.UUID serviceId);

    // ==================== DISPLAY ORDER ====================

    ServiceResponse updateDisplayOrder(java.util.UUID id, Integer displayOrder);
    void updateMultipleDisplayOrders(List<java.util.UUID> serviceIds);

    // ==================== STATISTICS ====================

    ServiceStatisticsResponse getServiceStatistics();
}
