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

/**
 * Appointment management controller
 */
@RestController
@RequestMapping("/{slug}/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    // ==================== APPOINTMENT CRUD ====================

    /**
     * Appointment yaratish (navbat olish)
     * POST /api/appointments
     */
    @PostMapping
    public ResponseEntity<ResponseDto<AppointmentResponse>> createAppointment(
            @Valid @RequestBody CreateAppointmentRequest request, @PathVariable String slug) {
        AppointmentResponse response = appointmentService.createAppointment(request);
        return ResponseDto.ok(response).toResponseEntity();
    }

    /**
     * Appointment ma'lumotlarini olish
     * GET /api/appointments/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto<AppointmentResponse>> getAppointment(@PathVariable Long id, @PathVariable String slug) {
        AppointmentResponse response = appointmentService.getAppointmentById(id);
        return ResponseDto.ok(response).toResponseEntity();
    }

    /**
     * Appointment yangilash
     * PUT /api/appointments/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ResponseDto<AppointmentResponse>> updateAppointment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAppointmentRequest request, @PathVariable String slug) {
        AppointmentResponse response = appointmentService.updateAppointment(id, request);
        return ResponseDto.ok(response).toResponseEntity();
    }

    /**
     * Appointment vaqtini o'zgartirish
     * PUT /api/appointments/{id}/reschedule
     */
    @PutMapping("/{id}/reschedule")
    public ResponseEntity<ResponseDto<AppointmentResponse>> rescheduleAppointment(
            @PathVariable Long id,
            @Valid @RequestBody RescheduleAppointmentRequest request, @PathVariable String slug) {
        AppointmentResponse response = appointmentService.rescheduleAppointment(id, request);
        return ResponseDto.ok(response).toResponseEntity();
    }

    /**
     * Appointment tasdiqlash
     * PUT /api/appointments/{id}/confirm
     */
    @PutMapping("/{id}/confirm")
    public ResponseEntity<ResponseDto<AppointmentResponse>> confirmAppointment(@PathVariable Long id, @PathVariable String slug) {
        AppointmentResponse response = appointmentService.confirmAppointment(id);
        return ResponseDto.ok(response).toResponseEntity();
    }

    /**
     * Appointment bekor qilish
     * PUT /api/appointments/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ResponseDto<AppointmentResponse>> cancelAppointment(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) CancelAppointmentRequest request, @PathVariable String slug) {
        AppointmentResponse response = appointmentService.cancelAppointment(id, request);
        return ResponseDto.ok(response).toResponseEntity();
    }

    /**
     * Appointment yakunlash
     * PUT /api/appointments/{id}/complete
     */
    @PutMapping("/{id}/complete")
    public ResponseEntity<ResponseDto<AppointmentResponse>> completeAppointment(@PathVariable Long id, @PathVariable String slug) {
        AppointmentResponse response = appointmentService.completeAppointment(id);
        return ResponseDto.ok(response).toResponseEntity();
    }

    /**
     * Appointment "No Show" qilish
     * PUT /api/appointments/{id}/no-show
     */
    @PutMapping("/{id}/no-show")
    public ResponseEntity<ResponseDto<AppointmentResponse>> markAsNoShow(@PathVariable Long id, @PathVariable String slug) {
        AppointmentResponse response = appointmentService.markAsNoShow(id);
        return ResponseDto.ok(response).toResponseEntity();
    }

    // ==================== AVAILABILITY ====================

    /**
     * Bo'sh vaqtlarni olish
     * GET /api/appointments/available-slots
     */
    @GetMapping("/available-slots")
    public ResponseEntity<ResponseDto<List<AvailableSlotResponse>>> getAvailableSlots(
            @RequestParam Long staffId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long serviceId, @PathVariable String slug) {
        List<AvailableSlotResponse> slots = appointmentService.getAvailableSlots(staffId, date, serviceId);
        return ResponseDto.ok(slots).toResponseEntity();
    }

    /**
     * Vaqt bo'shligini tekshirish
     * GET /api/appointments/check-availability
     */
    @GetMapping("/check-availability")
    public ResponseEntity<ResponseDto<Boolean>> checkAvailability(
            @RequestParam Long staffId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime time,
            @RequestParam Integer duration, @PathVariable String slug) {
        boolean available = appointmentService.isSlotAvailable(staffId, date, time, duration);
        return ResponseDto.ok(available).toResponseEntity();
    }

    // ==================== QUERIES ====================

    /**
     * Tenant bo'yicha appointmentlar
     * GET /api/appointments/by-tenant/{tenantId}
     */
    @GetMapping("/by-tenant")
    public ResponseEntity<ResponseDto<List<AppointmentResponse>>> getAppointmentsByTenant(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, @PathVariable String slug) {
        List<AppointmentResponse> appointments = appointmentService.getAppointmentsByTenant(date);
        return ResponseDto.ok(appointments).toResponseEntity();
    }

    /**
     * Staff bo'yicha appointmentlar
     * GET /api/appointments/by-staff/{staffId}
     */
    @GetMapping("/by-staff/{staffId}")
    public ResponseEntity<ResponseDto<List<AppointmentResponse>>> getAppointmentsByStaff(
            @PathVariable Long staffId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, @PathVariable String slug) {
        List<AppointmentResponse> appointments = appointmentService.getAppointmentsByStaff(staffId, date);
        return ResponseDto.ok(appointments).toResponseEntity();
    }

    /**
     * Employement bo'yicha appointmentlar
     * GET /api/appointments/by-service/{serviceId}
     */
    @GetMapping("/by-service/{serviceId}")
    public ResponseEntity<ResponseDto<List<AppointmentResponse>>> getAppointmentsByService(
            @PathVariable Long serviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, @PathVariable String slug) {
        List<AppointmentResponse> appointments = appointmentService.getAppointmentsByService(serviceId, date);
        return ResponseDto.ok(appointments).toResponseEntity();
    }

    /**
     * Mijoz telefoni bo'yicha appointmentlar
     * GET /api/appointments/by-phone/{phone}
     */
    @GetMapping("/by-phone/{phone}")
    public ResponseEntity<ResponseDto<List<AppointmentResponse>>> getAppointmentsByPhone(
            @PathVariable String phone, @PathVariable String slug) {
        List<AppointmentResponse> appointments = appointmentService.getAppointmentsByCustomerPhone(phone);
        return ResponseDto.ok(appointments).toResponseEntity();
    }

    /**
     * Bugungi appointmentlar
     * GET /api/appointments/today/{tenantId}
     */
    @GetMapping("/today")
    public ResponseEntity<ResponseDto<List<AppointmentResponse>>> getTodayAppointments(
             @PathVariable String slug) {
        List<AppointmentResponse> appointments = appointmentService.getTodayAppointments();
        return ResponseDto.ok(appointments).toResponseEntity();
    }

    /**
     * Kelajakdagi appointmentlar
     * GET /api/appointments/upcoming/{tenantId}
     */
    @GetMapping("/upcoming")
    public ResponseEntity<ResponseDto<List<AppointmentResponse>>> getUpcomingAppointments(
            @RequestParam(defaultValue = "10") Integer limit, @PathVariable String slug) {
        List<AppointmentResponse> appointments = appointmentService.getUpcomingAppointments(limit);
        return ResponseDto.ok(appointments).toResponseEntity();
    }

    /**
     * Mijoz telefoni bo'yicha kelajakdagi appointmentlar
     * GET /api/appointments/upcoming/by-phone/{phone}
     */
    @GetMapping("/upcoming/by-phone/{phone}")
    public ResponseEntity<ResponseDto<List<AppointmentResponse>>> getUpcomingAppointmentsByPhone(
            @PathVariable String phone, @PathVariable String slug) {
        List<AppointmentResponse> appointments = appointmentService.getUpcomingAppointmentsByPhone(phone);
        return ResponseDto.ok(appointments).toResponseEntity();
    }

    /**
     * Mijoz telefoni bo'yicha o'tmish appointmentlar
     * GET /api/appointments/past/by-phone/{phone}
     */
    @GetMapping("/past/by-phone/{phone}")
    public ResponseEntity<ResponseDto<List<AppointmentResponse>>> getPastAppointmentsByPhone(
            @PathVariable String phone,
            @RequestParam(defaultValue = "10") Integer limit, @PathVariable String slug) {
        List<AppointmentResponse> appointments = appointmentService.getPastAppointmentsByPhone(phone, limit);
        return ResponseDto.ok(appointments).toResponseEntity();
    }

    /**
     * Status bo'yicha appointmentlar
     * GET /api/appointments/by-status/{tenantId}
     */
    @GetMapping("/by-status")
    public ResponseEntity<ResponseDto<List<AppointmentResponse>>> getAppointmentsByStatus(
            @RequestParam AppointmentStatus status, @PathVariable String slug) {
        List<AppointmentResponse> appointments = appointmentService.getAppointmentsByStatus( status);
        return ResponseDto.ok(appointments).toResponseEntity();
    }

    /**
     * Pagination bilan appointmentlar
     * GET /api/appointments/paginated/{tenantId}
     */
    @GetMapping("/paginated")
    public ResponseEntity<ResponseDto<List<AppointmentResponse>>> getAppointmentsPaginated(
            @RequestParam(defaultValue = "false") Boolean activeOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size, @PathVariable String slug) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AppointmentResponse> appointmentPage = 
                appointmentService.getAppointmentsPaginated( activeOnly, pageable);
        return ResponseDto.ok(appointmentPage).toResponseEntity();
    }

    /**
     * Sana oralig'i bo'yicha appointmentlar
     * GET /api/appointments/date-range/{tenantId}
     */
    @GetMapping("/date-range")
    public ResponseEntity<ResponseDto<List<AppointmentResponse>>> getAppointmentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate, @PathVariable String slug) {
        List<AppointmentResponse> appointments = 
                appointmentService.getAppointmentsByDateRange( startDate, endDate);
        return ResponseDto.ok(appointments).toResponseEntity();
    }

    /**
     * Staff bo'yicha sana oralig'ida appointmentlar
     * GET /api/appointments/staff-date-range/{staffId}
     */
    @GetMapping("/staff-date-range/{staffId}")
    public ResponseEntity<ResponseDto<List<AppointmentResponse>>> getStaffAppointmentsByDateRange(
            @PathVariable Long staffId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate, @PathVariable String slug) {
        List<AppointmentResponse> appointments = 
                appointmentService.getStaffAppointmentsByDateRange(staffId, startDate, endDate);
        return ResponseDto.ok(appointments).toResponseEntity();
    }

    // ==================== CALENDAR ====================

    /**
     * Calendar ma'lumotlari
     * GET /api/appointments/calendar/{tenantId}
     */
    @GetMapping("/calendar")
    public ResponseEntity<ResponseDto<List<AppointmentCalendarResponse>>> getCalendarData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate, @PathVariable String slug) {
        List<AppointmentCalendarResponse> calendar = 
                appointmentService.getCalendarData(startDate, endDate);
        return ResponseDto.ok(calendar).toResponseEntity();
    }

    // ==================== STATISTICS ====================

    /**
     * Tenant statistikasi
     * GET /api/appointments/statistics/{tenantId}
     */
    @GetMapping("/statistics")
    public ResponseEntity<ResponseDto<AppointmentStatisticsResponse>> getStatistics(
            @PathVariable String slug) {
        AppointmentStatisticsResponse statistics = appointmentService.getStatistics();
        return ResponseDto.ok(statistics).toResponseEntity();
    }

    /**
     * Staff statistikasi
     * GET /api/appointments/statistics/staff/{staffId}
     */
    @GetMapping("/statistics/staff/{staffId}")
    public ResponseEntity<ResponseDto<AppointmentStatisticsResponse>> getStaffStatistics(
            @PathVariable Long staffId, @PathVariable String slug) {
        AppointmentStatisticsResponse statistics = appointmentService.getStaffStatistics(staffId);
        return ResponseDto.ok(statistics).toResponseEntity();
    }

    /**
     * Sana oralig'i bo'yicha statistika
     * GET /api/appointments/statistics/date-range/{tenantId}
     */
    @GetMapping("/statistics/date-range")
    public ResponseEntity<ResponseDto<AppointmentStatisticsResponse>> getStatisticsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate, @PathVariable String slug) {
        AppointmentStatisticsResponse statistics = 
                appointmentService.getStatisticsByDateRange(startDate, endDate);
        return ResponseDto.ok(statistics).toResponseEntity();
    }
}