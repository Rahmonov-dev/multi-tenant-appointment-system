package org.architect.multitenantappointmentsystem.service.interfaces;

import org.architect.multitenantappointmentsystem.dto.request.UpdateStaffScheduleRequest;
import org.architect.multitenantappointmentsystem.dto.response.StaffDetailResponse;
import org.architect.multitenantappointmentsystem.dto.request.CreateStaffRequest;
import org.architect.multitenantappointmentsystem.dto.request.CreateStaffScheduleRequest;
import org.architect.multitenantappointmentsystem.dto.request.UpdateStaffRequest;
import org.architect.multitenantappointmentsystem.dto.response.StaffResponse;
import org.architect.multitenantappointmentsystem.dto.response.StaffScheduleResponse;
import org.architect.multitenantappointmentsystem.dto.response.StaffStatisticsResponse;
import org.architect.multitenantappointmentsystem.entity.StaffRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Staff management service interface
 */
public interface StaffService {


    // ==================== STAFF CRUD ====================

    /**
     * Staff yaratish
     */
    StaffResponse createStaff(CreateStaffRequest request);

    /**
     * Staff ma'lumotlarini olish (ID bo'yicha)
     */
    StaffResponse getStaffById(Long id);

    /**
     * Staff batafsil ma'lumotlarini olish (schedules va services bilan)
     */
    StaffDetailResponse getStaffDetailById(Long id);

    /**
     * Staff ma'lumotlarini yangilash
     */
    StaffResponse updateStaff(Long id, UpdateStaffRequest request);

    /**
     * Staff o'chirish (soft delete)
     */
    void deleteStaff(Long id);

    /**
     * Staff aktivlashtirish
     */
    StaffResponse activateStaff(Long id);

    /**
     * Staff deaktivlashtirish
     */
    StaffResponse deactivateStaff(Long id);

    // ==================== STAFF QUERIES ====================

    /**
     * Tenant bo'yicha barcha stafflarni olish
     */
    List<StaffResponse> getAllStaffByTenant();

    /**
     * Tenant bo'yicha aktiv stafflarni olish
     */
    List<StaffResponse> getActiveStaffByTenant();

    /**
     * Tenant va role bo'yicha stafflarni olish
     */
    List<StaffResponse> getStaffByTenantAndRole( StaffRole role);

    /**
     * Employement bo'yicha stafflarni olish
     */
    List<StaffResponse> getStaffByService(Long serviceId);

    /**
     * Tenant bo'yicha stafflarni pagination bilan olish
     */
    Page<StaffResponse> getStaffByTenantPaginated( Boolean activeOnly, Pageable pageable);

    // ==================== SCHEDULE MANAGEMENT ====================

    /**
     * Staff schedule yaratish yoki yangilash
     */
    StaffScheduleResponse createOrUpdateSchedule(Long staffId, CreateStaffScheduleRequest request);

    /**
     * Staff barcha schedules ni olish
     */
    List<StaffScheduleResponse> getStaffSchedules(Long staffId);

    /**
     * Staff bitta kunning schedule ni olish
     */
    StaffScheduleResponse getStaffScheduleByDay(Long staffId, Integer dayOfWeek);

    /**
     * Staff schedule yangilash
     */
    StaffScheduleResponse updateSchedule(Long staffId, Integer dayOfWeek, UpdateStaffScheduleRequest request);

    /**
     * Staff schedule o'chirish (isAvailable = false qilish)
     */
    void deleteSchedule(Long staffId, Integer dayOfWeek);

    /**
     * Tenant bo'yicha barcha staff schedules ni olish
     */
    List<StaffScheduleResponse> getAllSchedulesByTenant();

    // ==================== SERVICE ASSIGNMENT ====================

    /**
     * Staff ga service biriktirish
     */
    StaffResponse assignServiceToStaff(Long staffId, Long serviceId);

    /**
     * Staff dan service olib tashlash
     */
    StaffResponse removeServiceFromStaff(Long staffId, Long serviceId);

    /**
     * Staff ga bir nechta service biriktirish
     */
    StaffResponse assignServicesToStaff(Long staffId, List<Long> serviceIds);

    // ==================== STATISTICS ====================

    /**
     * Tenant bo'yicha staff statistikasi
     */
    StaffStatisticsResponse getStaffStatistics();
}