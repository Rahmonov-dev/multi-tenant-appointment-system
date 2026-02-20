package org.architect.multitenantappointmentsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.architect.multitenantappointmentsystem.dto.*;
import org.architect.multitenantappointmentsystem.dto.request.CreateStaffRequest;
import org.architect.multitenantappointmentsystem.dto.request.CreateStaffScheduleRequest;
import org.architect.multitenantappointmentsystem.dto.request.UpdateStaffRequest;
import org.architect.multitenantappointmentsystem.dto.request.UpdateStaffScheduleRequest;
import org.architect.multitenantappointmentsystem.dto.response.StaffDetailResponse;
import org.architect.multitenantappointmentsystem.dto.response.StaffResponse;
import org.architect.multitenantappointmentsystem.dto.response.StaffScheduleResponse;
import org.architect.multitenantappointmentsystem.dto.response.StaffStatisticsResponse;
import org.architect.multitenantappointmentsystem.entity.StaffRole;
import org.architect.multitenantappointmentsystem.service.interfaces.StaffService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/{tenantId}/staff")
@Log4j2
@org.springframework.validation.annotation.Validated
public class StaffController {

    private final StaffService staffService;

    /**
     * Staff yaratish
     * POST /api/staff
     */
    @PostMapping
    public ResponseEntity<ResponseDto<StaffResponse>> createStaff(
            @PathVariable UUID tenantId,
            @Valid @RequestBody CreateStaffRequest request) {
        StaffResponse response = staffService.createStaff(tenantId, request);
        return ResponseDto.ok(response).toResponseEntity();
    }

    /**
     * Staff ma'lumotlarini olish (ID bo'yicha)
     * GET /api/staff/{id}
     */
    /**
     * Staff ma'lumotlarini olish (ID bo'yicha)
     * GET /api/staff/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto<StaffResponse>> getStaff(
            @PathVariable UUID tenantId,
            @PathVariable UUID id) {
        StaffResponse response = staffService.getStaffById(tenantId, id);
        return ResponseDto.ok(response).toResponseEntity();
    }

    /**
     * Staff batafsil ma'lumotlarini olish (schedules va services bilan)
     * GET /api/staff/{id}/detail
     */
    @GetMapping("/{id}/detail")
    public ResponseEntity<ResponseDto<StaffDetailResponse>> getStaffDetail(
            @PathVariable UUID tenantId,
            @PathVariable UUID id) {
        StaffDetailResponse response = staffService.getStaffDetailById(tenantId, id);
        return ResponseDto.ok(response).toResponseEntity();
    }

    /**
     * Staff ma'lumotlarini yangilash
     * PUT /api/staff/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ResponseDto<StaffResponse>> updateStaff(
            @PathVariable UUID tenantId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStaffRequest request) {
        StaffResponse response = staffService.updateStaff(tenantId, id, request);
        return ResponseDto.ok(response).toResponseEntity();
    }

    /**
     * Staff o'chirish (soft delete)
     * DELETE /api/staff/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDto<String>> deleteStaff(
            @PathVariable UUID tenantId,
            @PathVariable UUID id) {
        staffService.deleteStaff(tenantId, id);
        return ResponseDto.ok("", "Staff muvaffaqiyatli o'chirildi").toResponseEntity();
    }

    /**
     * Staff aktivlashtirish
     * PUT /api/staff/{id}/activate
     */
    @PutMapping("/{id}/activate")
    public ResponseEntity<ResponseDto<StaffResponse>> activateStaff(
            @PathVariable UUID tenantId,
            @PathVariable UUID id) {
        StaffResponse response = staffService.activateStaff(tenantId, id);
        return ResponseDto.ok(response).toResponseEntity();
    }

    /**
     * Staff deaktivlashtirish
     * PUT /api/staff/{id}/deactivate
     */
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<ResponseDto<StaffResponse>> deactivateStaff(
            @PathVariable UUID tenantId,
            @PathVariable UUID id) {
        StaffResponse response = staffService.deactivateStaff(tenantId, id);
        return ResponseDto.ok(response).toResponseEntity();
    }

    // ==================== STAFF QUERIES ====================

    /**
     * Tenant bo'yicha barcha stafflarni olish
     * GET /api/staff/by-tenant/{tenantId}
     */
    @GetMapping("/by-tenant")
    public ResponseEntity<ResponseDto<List<StaffResponse>>> getStaffByTenant(
            @PathVariable UUID tenantId,
            @RequestParam(defaultValue = "true") Boolean activeOnly) {
        List<StaffResponse> staff = activeOnly
                ? staffService.getActiveStaffByTenant(tenantId)
                : staffService.getAllStaffByTenant(tenantId);
        return ResponseDto.ok(staff).toResponseEntity();
    }

    /**
     * Tenant bo'yicha stafflarni pagination bilan olish
     * GET /api/staff/by-tenant/{tenantId}/paginated
     */
    @GetMapping("/by-tenant/paginated")
    public ResponseEntity<ResponseDto<List<StaffResponse>>> getStaffByTenantPaginated(
            @PathVariable UUID tenantId,
            @RequestParam(defaultValue = "true") Boolean activeOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<StaffResponse> staffPage = staffService.getStaffByTenantPaginated(tenantId, activeOnly, pageable);
        return ResponseDto.ok(staffPage.getContent()).toResponseEntity();
    }

    /**
     * Tenant va role bo'yicha stafflarni olish
     * GET /api/staff/by-tenant/{tenantId}/role/{role}
     */
    @GetMapping("/by-tenant/role/{role}")
    public ResponseEntity<ResponseDto<List<StaffResponse>>> getStaffByRole(
            @PathVariable UUID tenantId,
            @PathVariable StaffRole role) {
        List<StaffResponse> staff = staffService.getStaffByTenantAndRole(tenantId, role);
        return ResponseDto.ok(staff).toResponseEntity();
    }

    /**
     * Employement bo'yicha stafflarni olish
     * GET /api/staff/by-service/{serviceId}
     */
    @GetMapping("/by-service/{serviceId}")
    public ResponseEntity<ResponseDto<List<StaffResponse>>> getStaffByService(
            @PathVariable UUID tenantId,
            @PathVariable UUID serviceId) {
        List<StaffResponse> staff = staffService.getStaffByService(tenantId, serviceId);
        return ResponseDto.ok(staff).toResponseEntity();
    }

    // ==================== SCHEDULE MANAGEMENT ====================

    /**
     * Staff schedule yaratish/yangilash
     * POST /api/staff/{staffId}/schedules
     */
    @PostMapping("/{staffId}/schedules")
    public ResponseEntity<ResponseDto<StaffScheduleResponse>> createSchedule(
            @PathVariable UUID tenantId,
            @PathVariable UUID staffId,
            @Valid @RequestBody CreateStaffScheduleRequest request) {
        StaffScheduleResponse response = staffService.createOrUpdateSchedule(tenantId, staffId, request);
        return ResponseDto.ok(response).toResponseEntity();
    }

    /**
     * Staff barcha schedules ni olish
     * GET /api/staff/{staffId}/schedules
     */
    @GetMapping("/{staffId}/schedules")
    public ResponseEntity<ResponseDto<List<StaffScheduleResponse>>> getStaffSchedules(
            @PathVariable UUID tenantId,
            @PathVariable UUID staffId) {
        List<StaffScheduleResponse> schedules = staffService.getStaffSchedules(tenantId, staffId);
        return ResponseDto.ok(schedules).toResponseEntity();
    }

    /**
     * Staff bitta kunning schedule ni olish
     * GET /api/staff/{staffId}/schedules/{dayOfWeek}
     */
    @GetMapping("/{staffId}/schedules/{dayOfWeek}")
    public ResponseEntity<ResponseDto<StaffScheduleResponse>> getStaffScheduleByDay(
            @PathVariable UUID tenantId,
            @PathVariable UUID staffId,
            @PathVariable @jakarta.validation.constraints.Min(1) @jakarta.validation.constraints.Max(7) Integer dayOfWeek) {
        StaffScheduleResponse schedule = staffService.getStaffScheduleByDay(tenantId, staffId, dayOfWeek);
        return ResponseDto.ok(schedule).toResponseEntity();
    }

    /**
     * Staff schedule yangilash
     * PUT /api/staff/{staffId}/schedules/{dayOfWeek}
     */
    @PutMapping("/{staffId}/schedules/{dayOfWeek}")
    public ResponseEntity<ResponseDto<StaffScheduleResponse>> updateSchedule(
            @PathVariable UUID tenantId,
            @PathVariable UUID staffId,
            @PathVariable @jakarta.validation.constraints.Min(1) @jakarta.validation.constraints.Max(7) Integer dayOfWeek,
            @Valid @RequestBody UpdateStaffScheduleRequest request) {
        StaffScheduleResponse response = staffService.updateSchedule(tenantId, staffId, dayOfWeek, request);
        return ResponseDto.ok(response).toResponseEntity();
    }

    /**
     * Staff schedule o'chirish
     * DELETE /api/staff/{staffId}/schedules/{dayOfWeek}
     */
    @DeleteMapping("/{staffId}/schedules/{dayOfWeek}")
    public ResponseEntity<ResponseDto<String>> deleteSchedule(
            @PathVariable UUID tenantId,
            @PathVariable UUID staffId,
            @PathVariable @jakarta.validation.constraints.Min(1) @jakarta.validation.constraints.Max(7) Integer dayOfWeek) {
        staffService.deleteSchedule(tenantId, staffId, dayOfWeek);
        return ResponseDto.ok("", "Schedule muvaffaqiyatli o'chirildi").toResponseEntity();
    }

    /**
     * Tenant bo'yicha barcha staff schedules ni olish
     * GET /api/staff/schedules/by-tenant/{tenantId}
     */
    @GetMapping("/schedules/by-tenant")
    public ResponseEntity<ResponseDto<List<StaffScheduleResponse>>> getAllSchedulesByTenant(
            @PathVariable UUID tenantId) {
        List<StaffScheduleResponse> schedules = staffService.getAllSchedulesByTenant(tenantId);
        return ResponseDto.ok(schedules).toResponseEntity();
    }

    // ==================== SERVICE ASSIGNMENT ====================

    /**
     * Staff ga service biriktirish
     * POST /api/staff/{staffId}/services/{serviceId}
     */
    @PostMapping("/{staffId}/services/{serviceId}")
    public ResponseEntity<ResponseDto<StaffResponse>> assignService(
            @PathVariable UUID tenantId,
            @PathVariable UUID staffId,
            @PathVariable UUID serviceId) {
        StaffResponse response = staffService.assignServiceToStaff(tenantId, staffId, serviceId);
        return ResponseDto.ok(response).toResponseEntity();
    }

    /**
     * Staff dan service olib tashlash
     * DELETE /api/staff/{staffId}/services/{serviceId}
     */
    @DeleteMapping("/{staffId}/services/{serviceId}")
    public ResponseEntity<ResponseDto<StaffResponse>> removeService(
            @PathVariable UUID tenantId,
            @PathVariable UUID staffId,
            @PathVariable UUID serviceId) {
        StaffResponse response = staffService.removeServiceFromStaff(tenantId, staffId, serviceId);
        return ResponseDto.ok(response).toResponseEntity();
    }

    /**
     * Staff ga bir nechta service biriktirish
     * POST /api/staff/{staffId}/services/bulk
     */
    @PostMapping("/{staffId}/services/bulk")
    public ResponseEntity<ResponseDto<StaffResponse>> assignServices(
            @PathVariable UUID tenantId,
            @PathVariable UUID staffId,
            @RequestBody List<UUID> serviceIds) {
        StaffResponse response = staffService.assignServicesToStaff(tenantId, staffId, serviceIds);
        return ResponseDto.ok(response).toResponseEntity();
    }

    // ==================== STATISTICS ====================

    /**
     * Tenant bo'yicha staff statistikasi
     * GET /api/staff/statistics/{tenantId}
     */
    @GetMapping("/statistics")
    public ResponseEntity<ResponseDto<StaffStatisticsResponse>> getStaffStatistics(
            @PathVariable UUID tenantId) {
        StaffStatisticsResponse statistics = staffService.getStaffStatistics(tenantId);
        return ResponseDto.ok(statistics).toResponseEntity();
    }
}
