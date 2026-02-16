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

/**
 * Appointment management service interface
 */
public interface AppointmentService {

    // ==================== APPOINTMENT CRUD ====================
    
    /**
     * Appointment yaratish (navbat olish)
     */
    AppointmentResponse createAppointment(CreateAppointmentRequest request);

    /**
     * Appointment ma'lumotlarini olish (ID bo'yicha)
     */
    AppointmentResponse getAppointmentById(Long id);

    /**
     * Appointment yangilash
     */
    AppointmentResponse updateAppointment(Long id, UpdateAppointmentRequest request);

    /**
     * Appointment vaqtini o'zgartirish
     */
    AppointmentResponse rescheduleAppointment(Long id, RescheduleAppointmentRequest request);

    /**
     * Appointment bekor qilish
     */
    AppointmentResponse cancelAppointment(Long id, CancelAppointmentRequest request);

    /**
     * Appointment tasdiqlash
     */
    AppointmentResponse confirmAppointment(Long id);

    /**
     * Appointment yakunlash
     */
    AppointmentResponse completeAppointment(Long id);

    /**
     * Appointment "No Show" qilish
     */
    AppointmentResponse markAsNoShow(Long id);

    // ==================== AVAILABILITY CHECKING ====================

    /**
     * Bo'sh vaqtlarni olish
     */
    List<AvailableSlotResponse> getAvailableSlots(Long staffId, LocalDate date, Long serviceId);

    /**
     * Vaqt bo'shligini tekshirish
     */
    boolean isSlotAvailable(Long staffId, LocalDate date, LocalTime time, Integer duration);

    // ==================== QUERIES ====================

    /**
     * Tenant bo'yicha appointmentlarni olish
     */
    List<AppointmentResponse> getAppointmentsByTenant( LocalDate date);

    /**
     * Staff bo'yicha appointmentlarni olish
     */
    List<AppointmentResponse> getAppointmentsByStaff(Long staffId, LocalDate date);

    /**
     * Employement bo'yicha appointmentlarni olish
     */
    List<AppointmentResponse> getAppointmentsByService(Long serviceId, LocalDate date);

    /**
     * Mijoz telefon raqami bo'yicha appointmentlarni olish
     */
    List<AppointmentResponse> getAppointmentsByCustomerPhone(String phone);

    /**
     * Mijoz email bo'yicha appointmentlarni olish
     */
    List<AppointmentResponse> getAppointmentsByCustomerEmail(String email);

    /**
     * Bugungi appointmentlar
     */
    List<AppointmentResponse> getTodayAppointments();

    /**
     * Kelajakdagi appointmentlar
     */
    List<AppointmentResponse> getUpcomingAppointments( Integer limit);

    /**
     * Kelajakdagi appointmentlar (mijoz telefoni bo'yicha)
     */
    List<AppointmentResponse> getUpcomingAppointmentsByPhone(String phone);

    /**
     * O'tmish appointmentlar (mijoz telefoni bo'yicha)
     */
    List<AppointmentResponse> getPastAppointmentsByPhone(String phone, Integer limit);

    /**
     * Status bo'yicha appointmentlar
     */
    List<AppointmentResponse> getAppointmentsByStatus( AppointmentStatus status);

    /**
     * Pagination bilan appointmentlar
     */
    Page<AppointmentResponse> getAppointmentsPaginated( Boolean activeOnly, Pageable pageable);

    /**
     * Sana oralig'i bo'yicha appointmentlar
     */
    List<AppointmentResponse> getAppointmentsByDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * Staff bo'yicha sana oralig'ida appointmentlar
     */
    List<AppointmentResponse> getStaffAppointmentsByDateRange(
            Long staffId, LocalDate startDate, LocalDate endDate);

    // ==================== CALENDAR ====================

    /**
     * Calendar view uchun ma'lumotlar
     */
    List<AppointmentCalendarResponse> getCalendarData(LocalDate startDate, LocalDate endDate);

    // ==================== STATISTICS ====================

    /**
     * Tenant bo'yicha statistika
     */
    AppointmentStatisticsResponse getStatistics();

    /**
     * Staff bo'yicha statistika
     */
    AppointmentStatisticsResponse getStaffStatistics(Long staffId);

    /**
     * Sana oralig'i bo'yicha statistika
     */
    AppointmentStatisticsResponse getStatisticsByDateRange(LocalDate startDate, LocalDate endDate);
}