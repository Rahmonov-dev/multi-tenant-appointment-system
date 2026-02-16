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
    ServiceResponse getServiceById(Long id);
    ServiceDetailResponse getServiceDetailById(Long id);
    ServiceResponse updateService(Long id, UpdateServiceRequest request);
    void deleteService(Long id);
    ServiceResponse activateService(Long id);
    ServiceResponse deactivateService(Long id);

    // ==================== QUERY OPERATIONS ====================

    List<ServiceResponse> getAllServicesByTenant();
    List<ServiceResponse> getActiveServicesByTenant();
    List<ServiceResponse> getServicesByTenantOrdered( Boolean activeOnly);
    Page<ServiceResponse> getServicesByTenantPaginated( Boolean activeOnly, Pageable pageable);
    List<ServiceResponse> searchServices( String keyword, Boolean activeOnly);
    List<ServiceResponse> getServicesByPriceRange( BigDecimal minPrice, BigDecimal maxPrice);
    List<ServiceResponse> getServicesByMaxDuration( Integer maxDuration);
    List<ServiceResponse> getServicesByStaff(Long staffId);
    List<ServiceResponse> getPopularServices( Integer limit);

    // ==================== STAFF ASSIGNMENT ====================

    ServiceResponse assignStaffToService(Long serviceId, Long staffId);
    ServiceResponse removeStaffFromService(Long serviceId, Long staffId);
    ServiceResponse assignStaffsToService(Long serviceId, List<Long> staffIds);
    ServiceResponse removeAllStaffFromService(Long serviceId);

    // ==================== DISPLAY ORDER ====================

    ServiceResponse updateDisplayOrder(Long id, Integer displayOrder);
    void updateMultipleDisplayOrders(List<Long> serviceIds);

    // ==================== STATISTICS ====================

    ServiceStatisticsResponse getServiceStatistics();
}
