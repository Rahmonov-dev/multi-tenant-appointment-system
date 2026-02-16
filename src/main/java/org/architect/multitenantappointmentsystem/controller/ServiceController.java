package org.architect.multitenantappointmentsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.architect.multitenantappointmentsystem.dto.*;
import org.architect.multitenantappointmentsystem.dto.request.CreateServiceRequest;
import org.architect.multitenantappointmentsystem.dto.request.UpdateServiceRequest;
import org.architect.multitenantappointmentsystem.dto.response.ServiceDetailResponse;
import org.architect.multitenantappointmentsystem.dto.response.ServiceResponse;
import org.architect.multitenantappointmentsystem.dto.response.ServiceStatisticsResponse;
import org.architect.multitenantappointmentsystem.service.interfaces.ServiceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Employement management controller
 */
@RestController
@RequestMapping("/{slug}/api/services")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceService serviceService;

    // ==================== SERVICE CRUD ====================

    /**
     * Employement yaratish
     * POST /api/services
     */
    @PostMapping
    public ResponseEntity<ResponseDto<ServiceResponse>> createService(
            @Valid @RequestBody CreateServiceRequest request, @PathVariable String slug) {
        ServiceResponse response = serviceService.createService(request);
        return ResponseDto.ok(response).toResponseEntity();
    }

    /**
     * Employement ma'lumotlarini olish (ID bo'yicha)
     * GET /api/services/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto<ServiceResponse>> getService(@PathVariable Long id, @PathVariable String slug) {
        ServiceResponse response = serviceService.getServiceById(id);
        return ResponseDto.ok(response).toResponseEntity();
    }

    /**
     * Employement batafsil ma'lumotlarini olish (staff bilan)
     * GET /api/services/{id}/detail
     */
    @GetMapping("/{id}/detail")
    public ResponseEntity<ResponseDto<ServiceDetailResponse>> getServiceDetail(@PathVariable Long id, @PathVariable String slug) {
        ServiceDetailResponse response = serviceService.getServiceDetailById(id);
        return ResponseDto.ok(response).toResponseEntity();
    }

    /**
     * Employement ma'lumotlarini yangilash
     * PUT /api/services/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ResponseDto<ServiceResponse>> updateService(
            @PathVariable Long id,
            @Valid @RequestBody UpdateServiceRequest request, @PathVariable String slug) {
        ServiceResponse response = serviceService.updateService(id, request);
        return ResponseDto.ok(response).toResponseEntity();
    }

    /**
     * Employement o'chirish (soft delete)
     * DELETE /api/services/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDto<Void>> deleteService(@PathVariable Long id, @PathVariable String slug) {
        serviceService.deleteService(id);
        return ResponseDto.ok().toResponseEntity();
    }

    /**
     * Employement aktivlashtirish
     * PUT /api/services/{id}/activate
     */
    @PutMapping("/{id}/activate")
    public ResponseEntity<ResponseDto<ServiceResponse>> activateService(@PathVariable Long id, @PathVariable String slug) {
        ServiceResponse response = serviceService.activateService(id);
        return ResponseDto.ok(response).toResponseEntity();
    }

    /**
     * Employement deaktivlashtirish
     * PUT /api/services/{id}/deactivate
     */
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<ResponseDto<ServiceResponse>> deactivateService(@PathVariable Long id, @PathVariable String slug) {
        ServiceResponse response = serviceService.deactivateService(id);
        return ResponseDto.ok(response).toResponseEntity();
    }

    // ==================== SERVICE QUERIES ====================

    /**
     * Tenant bo'yicha barcha servicelarni olish
     * GET /api/services/by-tenant/{tenantId}
     */
    @GetMapping("/by-tenant")
    public ResponseEntity<ResponseDto<List<ServiceResponse>>> getServicesByTenant(
            @RequestParam(defaultValue = "false") Boolean activeOnly,
            @RequestParam(defaultValue = "false") Boolean ordered, @PathVariable String slug) {
        
        List<ServiceResponse> services = ordered
                ? serviceService.getServicesByTenantOrdered( activeOnly)
                : (activeOnly 
                    ? serviceService.getActiveServicesByTenant()
                    : serviceService.getAllServicesByTenant());
        
        return ResponseDto.ok(services).toResponseEntity();
    }

    /**
     * Tenant bo'yicha servicelarni pagination bilan olish
     * GET /api/services/by-tenant/{tenantId}/paginated
     */
    @GetMapping("/by-tenant/paginated")
    public ResponseEntity<ResponseDto<List<ServiceResponse>>> getServicesByTenantPaginated(
            @RequestParam(defaultValue = "true") Boolean activeOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size, @PathVariable String slug) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ServiceResponse> servicePage = serviceService.getServicesByTenantPaginated(
                activeOnly, pageable);
        return ResponseDto.ok(servicePage).toResponseEntity();
    }

    /**
     * Employement qidirish
     * GET /api/services/search
     */
    @GetMapping("/search")
    public ResponseEntity<ResponseDto<List<ServiceResponse>>> searchServices(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "true") Boolean activeOnly, @PathVariable String slug) {
        
        List<ServiceResponse> services = serviceService.searchServices( keyword, activeOnly);
        return ResponseDto.ok(services).toResponseEntity();
    }

    /**
     * Narx oralig'i bo'yicha servicelarni olish
     * GET /api/services/by-price-range
     */
    @GetMapping("/by-price-range")
    public ResponseEntity<ResponseDto<List<ServiceResponse>>> getServicesByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice, @PathVariable String slug) {
        
        List<ServiceResponse> services = serviceService.getServicesByPriceRange(
                minPrice, maxPrice);
        return ResponseDto.ok(services).toResponseEntity();
    }

    /**
     * Maksimum davomiylik bo'yicha servicelarni olish
     * GET /api/services/by-max-duration
     */
    @GetMapping("/by-max-duration")
    public ResponseEntity<ResponseDto<List<ServiceResponse>>> getServicesByMaxDuration(
            @RequestParam Integer maxDuration, @PathVariable String slug) {

        List<ServiceResponse> services = serviceService.getServicesByMaxDuration(
                 maxDuration);
        return ResponseDto.ok(services).toResponseEntity();
    }

    /**
     * Staff bo'yicha servicelarni olish
     * GET /api/services/by-staff/{staffId}
     */
    @GetMapping("/by-staff/{staffId}")
    public ResponseEntity<ResponseDto<List<ServiceResponse>>> getServicesByStaff(
            @PathVariable Long staffId, @PathVariable String slug) {
        
        List<ServiceResponse> services = serviceService.getServicesByStaff(staffId);
        return ResponseDto.ok(services).toResponseEntity();
    }

    /**
     * Mashhur servicelar
     * GET /api/services/popular/{tenantId}
     */
    @GetMapping("/popular")
    public ResponseEntity<ResponseDto<List<ServiceResponse>>> getPopularServices(
            @RequestParam(defaultValue = "10") Integer limit, @PathVariable String slug) {
        
        List<ServiceResponse> services = serviceService.getPopularServices(limit);
        return ResponseDto.ok(services).toResponseEntity();
    }

    // ==================== STAFF ASSIGNMENT ====================

    /**
     * Employement ga staff biriktirish
     * POST /api/services/{serviceId}/staff/{staffId}
     */
    @PostMapping("/{serviceId}/staff/{staffId}")
    public ResponseEntity<ResponseDto<ServiceResponse>> assignStaff(
            @PathVariable Long serviceId,
            @PathVariable Long staffId, @PathVariable String slug) {
        
        ServiceResponse response = serviceService.assignStaffToService(serviceId, staffId);
        return ResponseDto.ok(response).toResponseEntity();
    }

    /**
     * Employement dan staff olib tashlash
     * DELETE /api/services/{serviceId}/staff/{staffId}
     */
    @DeleteMapping("/{serviceId}/staff/{staffId}")
    public ResponseEntity<ResponseDto<ServiceResponse>> removeStaff(
            @PathVariable Long serviceId,
            @PathVariable Long staffId, @PathVariable String slug) {
        
        ServiceResponse response = serviceService.removeStaffFromService(serviceId, staffId);
        return ResponseDto.ok(response).toResponseEntity();
    }

    /**
     * Employement ga bir nechta staff biriktirish
     * POST /api/services/{serviceId}/staff/bulk
     */
    @PostMapping("/{serviceId}/staff/bulk")
    public ResponseEntity<ResponseDto<ServiceResponse>> assignStaffs(
            @PathVariable Long serviceId,
            @RequestBody List<Long> staffIds, @PathVariable String slug) {
        
        ServiceResponse response = serviceService.assignStaffsToService(serviceId, staffIds);
        return ResponseDto.ok(response).toResponseEntity();
    }

    /**
     * Employement dan barcha stafflarni olib tashlash
     * DELETE /api/services/{serviceId}/staff
     */
    @DeleteMapping("/{serviceId}/staff")
    public ResponseEntity<ResponseDto<ServiceResponse>> removeAllStaff(@PathVariable Long serviceId, @PathVariable String slug) {
        ServiceResponse response = serviceService.removeAllStaffFromService(serviceId);
        return ResponseDto.ok(response).toResponseEntity();
    }

    // ==================== DISPLAY ORDER ====================

    /**
     * Display order yangilash
     * PUT /api/services/{id}/display-order
     */
    @PutMapping("/{id}/display-order")
    public ResponseEntity<ResponseDto<ServiceResponse>> updateDisplayOrder(
            @PathVariable Long id,
            @RequestParam Integer displayOrder, @PathVariable String slug) {
        
        ServiceResponse response = serviceService.updateDisplayOrder(id, displayOrder);
        return ResponseDto.ok(response).toResponseEntity();
    }

    /**
     * Bir nechta service display order ni yangilash
     * PUT /api/services/display-order/bulk
     */
    @PutMapping("/display-order/bulk")
    public ResponseEntity<ResponseDto<Void>> updateMultipleDisplayOrders(
            @RequestBody List<Long> serviceIds, @PathVariable String slug) {
        
        serviceService.updateMultipleDisplayOrders(serviceIds);
        return ResponseDto.ok().toResponseEntity();
    }

    // ==================== STATISTICS ====================

    /**
     * Tenant bo'yicha service statistikasi
     * GET /api/services/statistics/{tenantId}
     */
    @GetMapping("/statistics")
    public ResponseEntity<ResponseDto<ServiceStatisticsResponse>> getServiceStatistics(@PathVariable String slug) {
        
        ServiceStatisticsResponse statistics = serviceService.getServiceStatistics();
        return ResponseDto.ok(statistics).toResponseEntity();
    }
}