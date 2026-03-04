package org.architect.multitenantappointmentsystem.service.interfaces;

import org.architect.multitenantappointmentsystem.dto.request.CancelAppointmentRequest;
import org.architect.multitenantappointmentsystem.dto.request.CreateAppointmentRequest;
import org.architect.multitenantappointmentsystem.dto.request.RescheduleAppointmentRequest;
import org.architect.multitenantappointmentsystem.dto.request.UpdateAppointmentRequest;
import org.architect.multitenantappointmentsystem.dto.response.AppointmentCalendarResponse;
import org.architect.multitenantappointmentsystem.dto.response.AppointmentResponse;
import org.architect.multitenantappointmentsystem.dto.response.AppointmentStatisticsResponse;
import org.architect.multitenantappointmentsystem.dto.response.AvailableSlotResponse;
import org.architect.multitenantappointmentsystem.entity.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * Appointment management service interface
 */
public interface AppointmentService {

    // ==================== APPOINTMENT CRUD ====================

    /**
     * Appointment yaratish (navbat olish)
     */
    AppointmentResponse createAppointment(UUID tenantId, CreateAppointmentRequest request);

    /**
     * Appointment ma'lumotlarini olish (ID bo'yicha)
     */
    AppointmentResponse getAppointmentById(UUID tenantId, UUID id);

    /**
     * Appointment yangilash
     */
    AppointmentResponse updateAppointment(UUID tenantId, UUID id, UpdateAppointmentRequest request);

    /**
     * Appointment vaqtini o'zgartirish
     */
    AppointmentResponse rescheduleAppointment(UUID tenantId, UUID id, RescheduleAppointmentRequest request);

    /**
     * Appointment bekor qilish
     */
    AppointmentResponse cancelAppointment(UUID tenantId, UUID id, CancelAppointmentRequest request);

    /**
     * Appointment tasdiqlash
     */
    AppointmentResponse confirmAppointment(UUID tenantId, UUID id);

    /**
     * Appointment yakunlash
     */
    AppointmentResponse completeAppointment(UUID tenantId, UUID id);

    /**
     * Appointment "No Show" qilish
     */
    AppointmentResponse markAsNoShow(UUID tenantId, UUID id);

    // ==================== AVAILABILITY CHECKING ====================

    /**
     * Bo'sh vaqtlarni olish
     */
    List<AvailableSlotResponse> getAvailableSlots(UUID tenantId, UUID staffId, LocalDate date, UUID serviceId);

    /**
     * Vaqt bo'shligini tekshirish
     */
    boolean isSlotAvailable(UUID tenantId, UUID staffId, LocalDate date, LocalTime time, Integer duration);

    // ==================== QUERIES ====================

    /**
     * Tenant bo'yicha appointmentlarni olish
     */
    List<AppointmentResponse> getAppointmentsByTenant(UUID tenantId, LocalDate date);

    /**
     * Staff bo'yicha appointmentlarni olish
     */
    List<AppointmentResponse> getAppointmentsByStaff(UUID tenantId, UUID staffId, LocalDate date);

    /**
     * Employement bo'yicha appointmentlarni olish
     */
    List<AppointmentResponse> getAppointmentsByService(UUID tenantId, UUID serviceId, LocalDate date);

    /**
     * Mijoz telefon raqami bo'yicha appointmentlarni olish
     */
    List<AppointmentResponse> getAppointmentsByCustomerPhone(UUID tenantId, String phone);

    /**
     * Mijoz email bo'yicha appointmentlarni olish
     */
    List<AppointmentResponse> getAppointmentsByCustomerEmail(UUID tenantId, String email);

    /**
     * Bugungi appointmentlar
     */
    List<AppointmentResponse> getTodayAppointments(UUID tenantId);

    /**
     * Kelajakdagi appointmentlar
     */
    List<AppointmentResponse> getUpcomingAppointments(UUID tenantId, Integer limit);

    /**
     * Kelajakdagi appointmentlar (mijoz telefoni bo'yicha)
     */
    List<AppointmentResponse> getUpcomingAppointmentsByPhone(UUID tenantId, String phone);

    /**
     * O'tmish appointmentlar (mijoz telefoni bo'yicha)
     */
    List<AppointmentResponse> getPastAppointmentsByPhone(UUID tenantId, String phone, Integer limit);

    /**
     * Status bo'yicha appointmentlar
     */
    List<AppointmentResponse> getAppointmentsByStatus(UUID tenantId, AppointmentStatus status);

    /**
     * Pagination bilan appointmentlar
     */
    Page<AppointmentResponse> getAppointmentsPaginated(UUID tenantId, Boolean activeOnly, Pageable pageable);

    /**
     * Sana oralig'i bo'yicha appointmentlar
     */
    List<AppointmentResponse> getAppointmentsByDateRange(UUID tenantId, LocalDate startDate, LocalDate endDate);

    /**
     * Staff bo'yicha sana oralig'ida appointmentlar
     */
    List<AppointmentResponse> getStaffAppointmentsByDateRange(
            UUID tenantId, UUID staffId, LocalDate startDate, LocalDate endDate);

    Page<AppointmentResponse> getStaffAppointments(UUID tenantId, UUID staffId,AppointmentStatus status,Pageable pageable);

    // ==================== CALENDAR ====================

    /**
     * Calendar view uchun ma'lumotlar
     */
    List<AppointmentCalendarResponse> getCalendarData(UUID tenantId, LocalDate startDate, LocalDate endDate);

    // ==================== STATISTICS ====================

    /**
     * Tenant bo'yicha statistika
     */
    AppointmentStatisticsResponse getStatistics(UUID tenantId);

    /**
     * Staff bo'yicha statistika
     */
    AppointmentStatisticsResponse getStaffStatistics(UUID tenantId, UUID staffId);

    /**
     * Sana oralig'i bo'yicha statistika
     */
    AppointmentStatisticsResponse getStatisticsByDateRange(UUID tenantId, LocalDate startDate, LocalDate endDate);
}