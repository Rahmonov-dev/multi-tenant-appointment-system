package org.architect.multitenantappointmentsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.architect.multitenantappointmentsystem.dto.*;
import org.architect.multitenantappointmentsystem.dto.request.CancelAppointmentRequest;
import org.architect.multitenantappointmentsystem.dto.request.CreateAppointmentRequest;
import org.architect.multitenantappointmentsystem.dto.request.RescheduleAppointmentRequest;
import org.architect.multitenantappointmentsystem.dto.request.UpdateAppointmentRequest;
import org.architect.multitenantappointmentsystem.dto.response.AppointmentCalendarResponse;
import org.architect.multitenantappointmentsystem.dto.response.AppointmentResponse;
import org.architect.multitenantappointmentsystem.dto.response.AppointmentStatisticsResponse;
import org.architect.multitenantappointmentsystem.dto.response.AvailableSlotResponse;
import org.architect.multitenantappointmentsystem.entity.AppointmentStatus;
import org.architect.multitenantappointmentsystem.service.interfaces.AppointmentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * Appointment management controller
 */
@RestController
@RequestMapping("/api/{tenantId}/appointments")
@RequiredArgsConstructor
@org.springframework.validation.annotation.Validated
public class AppointmentController {

    private final AppointmentService appointmentService;

    // ==================== APPOINTMENT CRUD ====================

    /**
     * Appointment yaratish (navbat olish)
     * POST /api/appointments
     */
    @PostMapping
    public ResponseEntity<ResponseDto<AppointmentResponse>> createAppointment(
            @PathVariable UUID tenantId,
            @Valid @RequestBody CreateAppointmentRequest request) {
        AppointmentResponse response = appointmentService.createAppointment(tenantId, request);
        return ResponseDto.ok(response).toResponseEntity();
    }

    /**
     * Appointment ma'lumotlarini olish
     * GET /api/appointments/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto<AppointmentResponse>> getAppointment(
            @PathVariable UUID tenantId,
            @PathVariable UUID id) {
        AppointmentResponse response = appointmentService.getAppointmentById(tenantId, id);
        return ResponseDto.ok(response).toResponseEntity();
    }

    /**
     * Appointment yangilash
     * PUT /api/appointments/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ResponseDto<AppointmentResponse>> updateAppointment(
            @PathVariable UUID tenantId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAppointmentRequest request) {
        AppointmentResponse response = appointmentService.updateAppointment(tenantId, id, request);
        return ResponseDto.ok(response).toResponseEntity();
    }

    /**
     * Appointment vaqtini o'zgartirish
     * PUT /api/appointments/{id}/reschedule
     */
    @PutMapping("/{id}/reschedule")
    public ResponseEntity<ResponseDto<AppointmentResponse>> rescheduleAppointment(
            @PathVariable UUID tenantId,
            @PathVariable UUID id,
            @Valid @RequestBody RescheduleAppointmentRequest request) {
        AppointmentResponse response = appointmentService.rescheduleAppointment(tenantId, id, request);
        return ResponseDto.ok(response).toResponseEntity();
    }

    /**
     * Appointment tasdiqlash
     * PUT /api/appointments/{id}/confirm
     */
    @PutMapping("/{id}/confirm")
    public ResponseEntity<ResponseDto<AppointmentResponse>> confirmAppointment(
            @PathVariable UUID tenantId,
            @PathVariable UUID id) {
        AppointmentResponse response = appointmentService.confirmAppointment(tenantId, id);
        return ResponseDto.ok(response).toResponseEntity();
    }

    /**
     * Appointment bekor qilish
     * PUT /api/appointments/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ResponseDto<AppointmentResponse>> cancelAppointment(
            @PathVariable UUID tenantId,
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) CancelAppointmentRequest request) {
        AppointmentResponse response = appointmentService.cancelAppointment(tenantId, id, request);
        return ResponseDto.ok(response).toResponseEntity();
    }

    /**
     * Appointment yakunlash
     * PUT /api/appointments/{id}/complete
     */
    @PutMapping("/{id}/complete")
    public ResponseEntity<ResponseDto<AppointmentResponse>> completeAppointment(
            @PathVariable UUID tenantId,
            @PathVariable UUID id) {
        AppointmentResponse response = appointmentService.completeAppointment(tenantId, id);
        return ResponseDto.ok(response).toResponseEntity();
    }

    /**
     * Appointment "No Show" qilish
     * PUT /api/appointments/{id}/no-show
     */
    @PutMapping("/{id}/no-show")
    public ResponseEntity<ResponseDto<AppointmentResponse>> markAsNoShow(
            @PathVariable UUID tenantId,
            @PathVariable UUID id) {
        AppointmentResponse response = appointmentService.markAsNoShow(tenantId, id);
        return ResponseDto.ok(response).toResponseEntity();
    }

    // ==================== AVAILABILITY ====================

    /**
     * Bo'sh vaqtlarni olish
     * GET /api/appointments/available-slots
     */
    @GetMapping("/available-slots")
    public ResponseEntity<ResponseDto<List<AvailableSlotResponse>>> getAvailableSlots(
            @PathVariable UUID tenantId,
            @RequestParam UUID staffId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) UUID serviceId) {
        List<AvailableSlotResponse> slots = appointmentService.getAvailableSlots(tenantId, staffId, date, serviceId);
        return ResponseDto.ok(slots).toResponseEntity();
    }

    /**
     * Vaqt bo'shligini tekshirish
     * GET /api/appointments/check-availability
     */
    @GetMapping("/check-availability")
    public ResponseEntity<ResponseDto<Boolean>> checkAvailability(
            @PathVariable UUID tenantId,
            @RequestParam UUID staffId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime time,
            @RequestParam Integer duration) {
        boolean available = appointmentService.isSlotAvailable(tenantId, staffId, date, time, duration);
        return ResponseDto.ok(available).toResponseEntity();
    }

    // ==================== QUERIES ====================

    /**
     * Tenant bo'yicha appointmentlar
     * GET /api/appointments/by-tenant/{tenantId}
     */
    @GetMapping("/by-tenant")
    public ResponseEntity<ResponseDto<List<AppointmentResponse>>> getAppointmentsByTenant(
            @PathVariable UUID tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<AppointmentResponse> appointments = appointmentService.getAppointmentsByTenant(tenantId, date);
        return ResponseDto.ok(appointments).toResponseEntity();
    }

    /**
     * Staff bo'yicha appointmentlar
     * GET /api/appointments/by-staff/{staffId}
     */
    @GetMapping("/by-staff/{staffId}")
    public ResponseEntity<ResponseDto<List<AppointmentResponse>>> getAppointmentsByStaff(
            @PathVariable UUID tenantId,
            @PathVariable UUID staffId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<AppointmentResponse> appointments = appointmentService.getAppointmentsByStaff(tenantId, staffId, date);
        return ResponseDto.ok(appointments).toResponseEntity();
    }

    /**
     * Employement bo'yicha appointmentlar
     * GET /api/appointments/by-service/{serviceId}
     */
    @GetMapping("/by-service/{serviceId}")
    public ResponseEntity<ResponseDto<List<AppointmentResponse>>> getAppointmentsByService(
            @PathVariable UUID tenantId,
            @PathVariable UUID serviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<AppointmentResponse> appointments = appointmentService.getAppointmentsByService(tenantId, serviceId, date);
        return ResponseDto.ok(appointments).toResponseEntity();
    }

    /**
     * Mijoz telefoni bo'yicha appointmentlar
     * GET /api/appointments/by-phone/{phone}
     */
    @GetMapping("/by-phone/{phone}")
    public ResponseEntity<ResponseDto<List<AppointmentResponse>>> getAppointmentsByPhone(
            @PathVariable UUID tenantId,
            @PathVariable String phone) {
        List<AppointmentResponse> appointments = appointmentService.getAppointmentsByCustomerPhone(tenantId, phone);
        return ResponseDto.ok(appointments).toResponseEntity();
    }

    /**
     * Bugungi appointmentlar
     * GET /api/appointments/today/{tenantId}
     */
    @GetMapping("/today")
    public ResponseEntity<ResponseDto<List<AppointmentResponse>>> getTodayAppointments(
            @PathVariable UUID tenantId) {
        List<AppointmentResponse> appointments = appointmentService.getTodayAppointments(tenantId);
        return ResponseDto.ok(appointments).toResponseEntity();
    }

    /**
     * Kelajakdagi appointmentlar
     * GET /api/appointments/upcoming/{tenantId}
     */
    @GetMapping("/upcoming")
    public ResponseEntity<ResponseDto<List<AppointmentResponse>>> getUpcomingAppointments(
            @PathVariable UUID tenantId,
            @RequestParam(defaultValue = "10") Integer limit) {
        List<AppointmentResponse> appointments = appointmentService.getUpcomingAppointments(tenantId, limit);
        return ResponseDto.ok(appointments).toResponseEntity();
    }

    /**
     * Mijoz telefoni bo'yicha kelajakdagi appointmentlar
     * GET /api/appointments/upcoming/by-phone/{phone}
     */
    @GetMapping("/upcoming/by-phone/{phone}")
    public ResponseEntity<ResponseDto<List<AppointmentResponse>>> getUpcomingAppointmentsByPhone(
            @PathVariable UUID tenantId,
            @PathVariable String phone) {
        List<AppointmentResponse> appointments = appointmentService.getUpcomingAppointmentsByPhone(tenantId, phone);
        return ResponseDto.ok(appointments).toResponseEntity();
    }

    /**
     * Mijoz telefoni bo'yicha o'tmish appointmentlar
     * GET /api/appointments/past/by-phone/{phone}
     */
    @GetMapping("/past/by-phone/{phone}")
    public ResponseEntity<ResponseDto<List<AppointmentResponse>>> getPastAppointmentsByPhone(
            @PathVariable UUID tenantId,
            @PathVariable String phone,
            @RequestParam(defaultValue = "10") Integer limit) {
        List<AppointmentResponse> appointments = appointmentService.getPastAppointmentsByPhone(tenantId, phone, limit);
        return ResponseDto.ok(appointments).toResponseEntity();
    }

    /**
     * Status bo'yicha appointmentlar
     * GET /api/appointments/by-status/{tenantId}
     */
    @GetMapping("/by-status")
    public ResponseEntity<ResponseDto<List<AppointmentResponse>>> getAppointmentsByStatus(
            @PathVariable UUID tenantId,
            @RequestParam AppointmentStatus status) {
        List<AppointmentResponse> appointments = appointmentService.getAppointmentsByStatus(tenantId, status);
        return ResponseDto.ok(appointments).toResponseEntity();
    }

    /**
     * Pagination bilan appointmentlar
     * GET /api/appointments/paginated/{tenantId}
     */
    @GetMapping("/paginated")
    public ResponseEntity<ResponseDto<List<AppointmentResponse>>> getAppointmentsPaginated(
            @PathVariable UUID tenantId,
            @RequestParam(defaultValue = "false") Boolean activeOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AppointmentResponse> appointmentPage = appointmentService.getAppointmentsPaginated(tenantId, activeOnly, pageable);
        return ResponseDto.ok(appointmentPage).toResponseEntity();
    }

    /**
     * Sana oralig'i bo'yicha appointmentlar
     * GET /api/appointments/date-range/{tenantId}
     */
    @GetMapping("/date-range")
    public ResponseEntity<ResponseDto<List<AppointmentResponse>>> getAppointmentsByDateRange(
            @PathVariable UUID tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<AppointmentResponse> appointments = appointmentService.getAppointmentsByDateRange(tenantId, startDate, endDate);
        return ResponseDto.ok(appointments).toResponseEntity();
    }

    /**
     * Staff bo'yicha sana oralig'ida appointmentlar
     * GET /api/appointments/staff-date-range/{staffId}
     */
    @GetMapping("/staff-date-range/{staffId}")
    public ResponseEntity<ResponseDto<List<AppointmentResponse>>> getStaffAppointmentsByDateRange(
            @PathVariable UUID tenantId,
            @PathVariable UUID staffId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<AppointmentResponse> appointments = appointmentService.getStaffAppointmentsByDateRange(tenantId, staffId, startDate,
                endDate);
        return ResponseDto.ok(appointments).toResponseEntity();
    }

    // ==================== CALENDAR ====================

    /**
     * Calendar ma'lumotlari
     * GET /api/appointments/calendar/{tenantId}
     */
    @GetMapping("/calendar")
    public ResponseEntity<ResponseDto<List<AppointmentCalendarResponse>>> getCalendarData(
            @PathVariable UUID tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<AppointmentCalendarResponse> calendar = appointmentService.getCalendarData(tenantId, startDate, endDate);
        return ResponseDto.ok(calendar).toResponseEntity();
    }

    // ==================== STATISTICS ====================

    /**
     * Tenant statistikasi
     * GET /api/appointments/statistics/{tenantId}
     */
    @GetMapping("/statistics")
    public ResponseEntity<ResponseDto<AppointmentStatisticsResponse>> getStatistics(
            @PathVariable UUID tenantId) {
        AppointmentStatisticsResponse statistics = appointmentService.getStatistics(tenantId);
        return ResponseDto.ok(statistics).toResponseEntity();
    }

    /**
     * Staff statistikasi
     * GET /api/appointments/statistics/staff/{staffId}
     */
    @GetMapping("/statistics/staff/{staffId}")
    public ResponseEntity<ResponseDto<AppointmentStatisticsResponse>> getStaffStatistics(
            @PathVariable UUID tenantId,
            @PathVariable UUID staffId) {
        AppointmentStatisticsResponse statistics = appointmentService.getStaffStatistics(tenantId, staffId);
        return ResponseDto.ok(statistics).toResponseEntity();
    }

    /**
     * Sana oralig'i bo'yicha statistika
     * GET /api/appointments/statistics/date-range/{tenantId}
     */
    @GetMapping("/statistics/date-range")
    public ResponseEntity<ResponseDto<AppointmentStatisticsResponse>> getStatisticsByDateRange(
            @PathVariable UUID tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        AppointmentStatisticsResponse statistics = appointmentService.getStatisticsByDateRange(tenantId, startDate, endDate);
        return ResponseDto.ok(statistics).toResponseEntity();
    }
}