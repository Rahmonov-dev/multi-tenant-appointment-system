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

@RequiredArgsConstructor
@RestController
@RequestMapping("/{slug}/api/staff")
@Log4j2
public class StaffController {

    private final StaffService staffService;

    /**
     * Staff yaratish
     * POST /api/staff
     */
    @PostMapping
    public ResponseEntity<ResponseDto<StaffResponse>> createStaff(
            @Valid @RequestBody CreateStaffRequest request, @PathVariable String slug) {
        StaffResponse response = staffService.createStaff(request);
        return ResponseDto.ok(response).toResponseEntity();
    }

    /**
     * Staff ma'lumotlarini olish (ID bo'yicha)
     * GET /api/staff/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto<StaffResponse>> getStaff(@PathVariable Long id, @PathVariable String slug) {
        StaffResponse response = staffService.getStaffById(id);
        System.out.println(response);
        return ResponseDto.ok(response).toResponseEntity();
    }

    /**
     * Staff batafsil ma'lumotlarini olish (schedules va services bilan)
     * GET /api/staff/{id}/detail
     */
    @GetMapping("/{id}/detail")
    public ResponseEntity<ResponseDto<StaffDetailResponse>> getStaffDetail(@PathVariable Long id, @PathVariable String slug) {
        StaffDetailResponse response = staffService.getStaffDetailById(id);
        return ResponseDto.ok(response).toResponseEntity();
    }

    /**
     * Staff ma'lumotlarini yangilash
     * PUT /api/staff/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ResponseDto<StaffResponse>> updateStaff(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStaffRequest request, @PathVariable String slug) {
        StaffResponse response = staffService.updateStaff(id, request);
        return ResponseDto.ok(response).toResponseEntity();
    }

    /**
     * Staff o'chirish (soft delete)
     * DELETE /api/staff/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDto<String>> deleteStaff(@PathVariable Long id, @PathVariable String slug) {
        staffService.deleteStaff(id);
        return ResponseDto.ok( "","Muvaffaqqiyatli o'chirildi").toResponseEntity();
    }

    /**
     * Staff aktivlashtirish
     * PUT /api/staff/{id}/activate
     */
    @PutMapping("/{id}/activate")
    public ResponseEntity<ResponseDto<StaffResponse>> activateStaff(@PathVariable Long id, @PathVariable String slug) {
        StaffResponse response = staffService.activateStaff(id);
        return ResponseDto.ok(response).toResponseEntity();
    }

    /**
     * Staff deaktivlashtirish
     * PUT /api/staff/{id}/deactivate
     */
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<ResponseDto<StaffResponse>> deactivateStaff(@PathVariable Long id, @PathVariable String slug) {
        StaffResponse response = staffService.deactivateStaff(id);
        return ResponseDto.ok(response).toResponseEntity();
    }

    // ==================== STAFF QUERIES ====================

    /**
     * Tenant bo'yicha barcha stafflarni olish
     * GET /api/staff/by-tenant/{tenantId}
     */
    @GetMapping("/by-tenant/")
    public ResponseEntity<ResponseDto<List<StaffResponse>>> getStaffByTenant(
            @RequestParam(defaultValue = "true") Boolean activeOnly, @PathVariable String slug) {
        List<StaffResponse> staff = activeOnly
                ? staffService.getActiveStaffByTenant()
                : staffService.getAllStaffByTenant();
        return ResponseDto.ok(staff).toResponseEntity();
    }

    /**
     * Tenant bo'yicha stafflarni pagination bilan olish
     * GET /api/staff/by-tenant/{tenantId}/paginated
     */
    @GetMapping("/by-tenant/paginated")
    public ResponseEntity<ResponseDto<List<StaffResponse>>> getStaffByTenantPaginated(
            @RequestParam(defaultValue = "true") Boolean activeOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size, @PathVariable String slug) {
        Pageable pageable = PageRequest.of(page, size);
        Page<StaffResponse> staffPage = staffService.getStaffByTenantPaginated( activeOnly, pageable);
        return ResponseDto.ok(staffPage.getContent()).toResponseEntity();
    }

    /**
     * Tenant va role bo'yicha stafflarni olish
     * GET /api/staff/by-tenant/{tenantId}/role/{role}
     */
    @GetMapping("/by-tenant/role/{role}")
    public ResponseEntity<ResponseDto<List<StaffResponse>>> getStaffByRole(
            @PathVariable StaffRole role, @PathVariable String slug) {
        List<StaffResponse> staff = staffService.getStaffByTenantAndRole(role);
        return ResponseDto.ok(staff).toResponseEntity();
    }

    /**
     * Employement bo'yicha stafflarni olish
     * GET /api/staff/by-service/{serviceId}
     */
    @GetMapping("/by-service/{serviceId}")
    public ResponseEntity<ResponseDto<List<StaffResponse>>> getStaffByService(
            @PathVariable Long serviceId, @PathVariable String slug) {
        List<StaffResponse> staff = staffService.getStaffByService(serviceId);
        return ResponseDto.ok(staff).toResponseEntity();
    }

    // ==================== SCHEDULE MANAGEMENT ====================

    /**
     * Staff schedule yaratish/yangilash
     * POST /api/staff/{staffId}/schedules
     */
    @PostMapping("/{staffId}/schedules")
    public ResponseEntity<ResponseDto<StaffScheduleResponse>> createSchedule(
            @PathVariable Long staffId,
            @Valid @RequestBody CreateStaffScheduleRequest request, @PathVariable String slug) {
        StaffScheduleResponse response = staffService.createOrUpdateSchedule(staffId, request);
        return ResponseDto.ok(response).toResponseEntity();
    }

    /**
     * Staff barcha schedules ni olish
     * GET /api/staff/{staffId}/schedules
     */
    @GetMapping("/{staffId}/schedules")
    public ResponseEntity<ResponseDto<List<StaffScheduleResponse>>> getStaffSchedules(
            @PathVariable Long staffId, @PathVariable String slug) {
        List<StaffScheduleResponse> schedules = staffService.getStaffSchedules(staffId);
        return ResponseDto.ok(schedules).toResponseEntity();
    }

    /**
     * Staff bitta kunning schedule ni olish
     * GET /api/staff/{staffId}/schedules/{dayOfWeek}
     */
    @GetMapping("/{staffId}/schedules/{dayOfWeek}")
    public ResponseEntity<ResponseDto<StaffScheduleResponse>> getStaffScheduleByDay(
            @PathVariable Long staffId,
            @PathVariable Integer dayOfWeek, @PathVariable String slug) {
        StaffScheduleResponse schedule = staffService.getStaffScheduleByDay(staffId, dayOfWeek);
        return ResponseDto.ok(schedule).toResponseEntity();
    }

    /**
     * Staff schedule yangilash
     * PUT /api/staff/{staffId}/schedules/{dayOfWeek}
     */
    @PutMapping("/{staffId}/schedules/{dayOfWeek}")
    public ResponseEntity<ResponseDto<StaffScheduleResponse>> updateSchedule(
            @PathVariable Long staffId,
            @PathVariable Integer dayOfWeek,
            @Valid @RequestBody UpdateStaffScheduleRequest request, @PathVariable String slug) {
        StaffScheduleResponse response = staffService.updateSchedule(staffId, dayOfWeek, request);
        return ResponseDto.ok(response).toResponseEntity();
    }

    /**
     * Staff schedule o'chirish
     * DELETE /api/staff/{staffId}/schedules/{dayOfWeek}
     */
    @DeleteMapping("/{staffId}/schedules/{dayOfWeek}")
    public ResponseEntity<ResponseDto<String>> deleteSchedule(
            @PathVariable Long staffId,
            @PathVariable Integer dayOfWeek, @PathVariable String slug) {
        staffService.deleteSchedule(staffId, dayOfWeek);
        return ResponseDto.ok("", "Tenant o'chirildi").toResponseEntity();
    }

    /**
     * Tenant bo'yicha barcha staff schedules ni olish
     * GET /api/staff/schedules/by-tenant/{tenantId}
     */
    @GetMapping("/schedules/by-tenant")
    public ResponseEntity<ResponseDto<List<StaffScheduleResponse>>> getAllSchedulesByTenant(
             @PathVariable String slug) {
        List<StaffScheduleResponse> schedules = staffService.getAllSchedulesByTenant();
        return ResponseDto.ok(schedules).toResponseEntity();
    }

    // ==================== SERVICE ASSIGNMENT ====================

    /**
     * Staff ga service biriktirish
     * POST /api/staff/{staffId}/services/{serviceId}
     */
    @PostMapping("/{staffId}/services/{serviceId}")
    public ResponseEntity<ResponseDto<StaffResponse>> assignService(
            @PathVariable Long staffId,
            @PathVariable Long serviceId, @PathVariable String slug) {
        StaffResponse response = staffService.assignServiceToStaff(staffId, serviceId);
        return ResponseDto.ok(response).toResponseEntity();
    }

    /**
     * Staff dan service olib tashlash
     * DELETE /api/staff/{staffId}/services/{serviceId}
     */
    @DeleteMapping("/{staffId}/services/{serviceId}")
    public ResponseEntity<ResponseDto<StaffResponse>> removeService(
            @PathVariable Long staffId,
            @PathVariable Long serviceId, @PathVariable String slug) {
        StaffResponse response = staffService.removeServiceFromStaff(staffId, serviceId);
        return ResponseDto.ok(response).toResponseEntity();
    }

    /**
     * Staff ga bir nechta service biriktirish
     * POST /api/staff/{staffId}/services/bulk
     */
    @PostMapping("/{staffId}/services/bulk")
    public ResponseEntity<ResponseDto<StaffResponse>> assignServices(
            @PathVariable Long staffId,
            @RequestBody List<Long> serviceIds, @PathVariable String slug) {
        StaffResponse response = staffService.assignServicesToStaff(staffId, serviceIds);
        return ResponseDto.ok(response).toResponseEntity();
    }

    // ==================== STATISTICS ====================

    /**
     * Tenant bo'yicha staff statistikasi
     * GET /api/staff/statistics/{tenantId}
     */
    @GetMapping("/statistics")
    public ResponseEntity<ResponseDto<StaffStatisticsResponse>> getStaffStatistics(@PathVariable String slug) {
        StaffStatisticsResponse statistics = staffService.getStaffStatistics();
        return ResponseDto.ok(statistics).toResponseEntity();
    }
}
