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
import java.util.UUID;

/**
 * Employement management controller
 */
@RestController
@RequestMapping("/api/{tenantId}/services")
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
            @PathVariable UUID tenantId,
            @Valid @RequestBody CreateServiceRequest request) {
        ServiceResponse response = serviceService.createService(tenantId, request);
        return ResponseDto.ok(response).toResponseEntity();
    }

    /**
     * Employement ma'lumotlarini olish (ID bo'yicha)
     * GET /api/services/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto<ServiceResponse>> getService(
            @PathVariable UUID tenantId,
            @PathVariable UUID id) {
        ServiceResponse response = serviceService.getServiceById(tenantId, id);
        return ResponseDto.ok(response).toResponseEntity();
    }

    /**
     * Employement batafsil ma'lumotlarini olish (staff bilan)
     * GET /api/services/{id}/detail
     */
    @GetMapping("/{id}/detail")
    public ResponseEntity<ResponseDto<ServiceDetailResponse>> getServiceDetail(
            @PathVariable UUID tenantId,
            @PathVariable UUID id) {
        ServiceDetailResponse response = serviceService.getServiceDetailById(tenantId, id);
        return ResponseDto.ok(response).toResponseEntity();
    }

    // Removed redundant private helper method

    /**
     * Employement ma'lumotlarini yangilash
     * PUT /api/services/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ResponseDto<ServiceResponse>> updateService(
            @PathVariable UUID tenantId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateServiceRequest request) {
        ServiceResponse response = serviceService.updateService(tenantId, id, request);
        return ResponseDto.ok(response).toResponseEntity();
    }

    /**
     * Employement o'chirish (soft delete)
     * DELETE /api/services/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDto<Void>> deleteService(
            @PathVariable UUID tenantId,
            @PathVariable UUID id) {
        serviceService.deleteService(tenantId, id);
        return ResponseDto.ok().toResponseEntity();
    }

    /**
     * Employement aktivlashtirish
     * PUT /api/services/{id}/activate
     */
    @PutMapping("/{id}/activate")
    public ResponseEntity<ResponseDto<ServiceResponse>> activateService(
            @PathVariable UUID tenantId,
            @PathVariable UUID id) {
        ServiceResponse response = serviceService.activateService(tenantId, id);
        return ResponseDto.ok(response).toResponseEntity();
    }

    /**
     * Employement deaktivlashtirish
     * PUT /api/services/{id}/deactivate
     */
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<ResponseDto<ServiceResponse>> deactivateService(
            @PathVariable UUID tenantId,
            @PathVariable UUID id) {
        ServiceResponse response = serviceService.deactivateService(tenantId, id);
        return ResponseDto.ok(response).toResponseEntity();
    }

    // ==================== SERVICE QUERIES ====================

    /**
     * Tenant bo'yicha barcha servicelarni olish
     * GET /api/services/by-tenant/{tenantId}
     */
    @GetMapping("/by-tenant")
    public ResponseEntity<ResponseDto<List<ServiceResponse>>> getServicesByTenant(
            @PathVariable UUID tenantId,
            @RequestParam(defaultValue = "false") Boolean activeOnly,
            @RequestParam(defaultValue = "false") Boolean ordered) {

        List<ServiceResponse> services = ordered
                ? serviceService.getServicesByTenantOrdered(tenantId, activeOnly)
                : (activeOnly
                    ? serviceService.getActiveServicesByTenant(tenantId)
                    : serviceService.getAllServicesByTenant(tenantId));

        return ResponseDto.ok(services).toResponseEntity();
    }

    /**
     * Tenant bo'yicha servicelarni pagination bilan olish
     * GET /api/services/by-tenant/{tenantId}/paginated
     */
    @GetMapping("/by-tenant/paginated")
    public ResponseEntity<ResponseDto<List<ServiceResponse>>> getServicesByTenantPaginated(
            @PathVariable UUID tenantId,
            @RequestParam(defaultValue = "true") Boolean activeOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ServiceResponse> servicePage = serviceService.getServicesByTenantPaginated(
                tenantId, activeOnly, pageable);
        return ResponseDto.ok(servicePage).toResponseEntity();
    }

    /**
     * Employement qidirish
     * GET /api/services/search
     */
    @GetMapping("/search")
    public ResponseEntity<ResponseDto<List<ServiceResponse>>> searchServices(
            @PathVariable UUID tenantId,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "true") Boolean activeOnly) {

        List<ServiceResponse> services = serviceService.searchServices(tenantId, keyword, activeOnly);
        return ResponseDto.ok(services).toResponseEntity();
    }

    /**
     * Narx oralig'i bo'yicha servicelarni olish
     * GET /api/services/by-price-range
     */
    @GetMapping("/by-price-range")
    public ResponseEntity<ResponseDto<List<ServiceResponse>>> getServicesByPriceRange(
            @PathVariable UUID tenantId,
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {

        List<ServiceResponse> services = serviceService.getServicesByPriceRange(
                tenantId, minPrice, maxPrice);
        return ResponseDto.ok(services).toResponseEntity();
    }

    /**
     * Maksimum davomiylik bo'yicha servicelarni olish
     * GET /api/services/by-max-duration
     */
    @GetMapping("/by-max-duration")
    public ResponseEntity<ResponseDto<List<ServiceResponse>>> getServicesByMaxDuration(
            @PathVariable UUID tenantId,
            @RequestParam Integer maxDuration) {

        List<ServiceResponse> services = serviceService.getServicesByMaxDuration(
                tenantId, maxDuration);
        return ResponseDto.ok(services).toResponseEntity();
    }

    /**
     * Staff bo'yicha servicelarni olish
     * GET /api/services/by-staff/{staffId}
     */
    @GetMapping("/by-staff/{staffId}")
    public ResponseEntity<ResponseDto<List<ServiceResponse>>> getServicesByStaff(
            @PathVariable UUID tenantId,
            @PathVariable UUID staffId) {

        List<ServiceResponse> services = serviceService.getServicesByStaff(tenantId, staffId);
        return ResponseDto.ok(services).toResponseEntity();
    }

    /**
     * Mashhur servicelar
     * GET /api/services/popular/{tenantId}
     */
    @GetMapping("/popular")
    public ResponseEntity<ResponseDto<List<ServiceResponse>>> getPopularServices(
            @PathVariable UUID tenantId,
            @RequestParam(defaultValue = "10") Integer limit) {

        List<ServiceResponse> services = serviceService.getPopularServices(tenantId, limit);
        return ResponseDto.ok(services).toResponseEntity();
    }

    // ==================== STAFF ASSIGNMENT ====================

    /**
     * Employement ga staff biriktirish
     * POST /api/services/{serviceId}/staff/{staffId}
     */
    @PostMapping("/{serviceId}/staff/{staffId}")
    public ResponseEntity<ResponseDto<ServiceResponse>> assignStaff(
            @PathVariable UUID tenantId,
            @PathVariable UUID serviceId,
            @PathVariable UUID staffId) {

        ServiceResponse response = serviceService.assignStaffToService(tenantId, serviceId, staffId);
        return ResponseDto.ok(response).toResponseEntity();
    }

    /**
     * Employement dan staff olib tashlash
     * DELETE /api/services/{serviceId}/staff/{staffId}
     */
    @DeleteMapping("/{serviceId}/staff/{staffId}")
    public ResponseEntity<ResponseDto<ServiceResponse>> removeStaff(
            @PathVariable UUID tenantId,
            @PathVariable UUID serviceId,
            @PathVariable UUID staffId) {

        ServiceResponse response = serviceService.removeStaffFromService(tenantId, serviceId, staffId);
        return ResponseDto.ok(response).toResponseEntity();
    }

    /**
     * Employement ga bir nechta staff biriktirish
     * POST /api/services/{serviceId}/staff/bulk
     */
    @PostMapping("/{serviceId}/staff/bulk")
    public ResponseEntity<ResponseDto<ServiceResponse>> assignStaffs(
            @PathVariable UUID tenantId,
            @PathVariable UUID serviceId,
            @RequestBody List<UUID> staffIds) {

        ServiceResponse response = serviceService.assignStaffsToService(tenantId, serviceId, staffIds);
        return ResponseDto.ok(response).toResponseEntity();
    }

    /**
     * Employement dan barcha stafflarni olib tashlash
     * DELETE /api/services/{serviceId}/staff
     */
    @DeleteMapping("/{serviceId}/staff")
    public ResponseEntity<ResponseDto<ServiceResponse>> removeAllStaff(
            @PathVariable UUID tenantId,
            @PathVariable UUID serviceId) {
        ServiceResponse response = serviceService.removeAllStaffFromService(tenantId, serviceId);
        return ResponseDto.ok(response).toResponseEntity();
    }

    // ==================== DISPLAY ORDER ====================

    /**
     * Display order yangilash
     * PUT /api/services/{id}/display-order
     */
    @PutMapping("/{id}/display-order")
    public ResponseEntity<ResponseDto<ServiceResponse>> updateDisplayOrder(
            @PathVariable UUID tenantId,
            @PathVariable UUID id,
            @RequestParam Integer displayOrder) {

        ServiceResponse response = serviceService.updateDisplayOrder(tenantId, id, displayOrder);
        return ResponseDto.ok(response).toResponseEntity();
    }

    /**
     * Bir nechta service display order ni yangilash
     * PUT /api/services/display-order/bulk
     */
    @PutMapping("/display-order/bulk")
    public ResponseEntity<ResponseDto<Void>> updateMultipleDisplayOrders(
            @PathVariable UUID tenantId,
            @RequestBody List<UUID> serviceIds) {

        serviceService.updateMultipleDisplayOrders(tenantId, serviceIds);
        return ResponseDto.ok().toResponseEntity();
    }

    // ==================== STATISTICS ====================

    /**
     * Tenant bo'yicha service statistikasi
     * GET /api/services/statistics/{tenantId}
     */
    @GetMapping("/statistics")
    public ResponseEntity<ResponseDto<ServiceStatisticsResponse>> getServiceStatistics(
            @PathVariable UUID tenantId) {
        
        ServiceStatisticsResponse statistics = serviceService.getServiceStatistics(tenantId);
        return ResponseDto.ok(statistics).toResponseEntity();
    }
}