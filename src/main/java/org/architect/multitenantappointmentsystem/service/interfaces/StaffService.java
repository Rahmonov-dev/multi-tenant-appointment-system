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
    StaffResponse getStaffById(java.util.UUID id);

    /**
     * Staff batafsil ma'lumotlarini olish (schedules va services bilan)
     */
    StaffDetailResponse getStaffDetailById(java.util.UUID id);

    /**
     * Staff ma'lumotlarini yangilash
     */
    StaffResponse updateStaff(java.util.UUID id, UpdateStaffRequest request);

    /**
     * Staff o'chirish (soft delete)
     */
    void deleteStaff(java.util.UUID id);

    /**
     * Staff aktivlashtirish
     */
    StaffResponse activateStaff(java.util.UUID id);

    /**
     * Staff deaktivlashtirish
     */
    StaffResponse deactivateStaff(java.util.UUID id);

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
    List<StaffResponse> getStaffByService(java.util.UUID serviceId);

    /**
     * Tenant bo'yicha stafflarni pagination bilan olish
     */
    Page<StaffResponse> getStaffByTenantPaginated( Boolean activeOnly, Pageable pageable);

    // ==================== SCHEDULE MANAGEMENT ====================

    /**
     * Staff schedule yaratish yoki yangilash
     */
    StaffScheduleResponse createOrUpdateSchedule(java.util.UUID staffId, CreateStaffScheduleRequest request);

    /**
     * Staff barcha schedules ni olish
     */
    List<StaffScheduleResponse> getStaffSchedules(java.util.UUID staffId);

    /**
     * Staff bitta kunning schedule ni olish
     */
    StaffScheduleResponse getStaffScheduleByDay(java.util.UUID staffId, Integer dayOfWeek);

    /**
     * Staff schedule yangilash
     */
    StaffScheduleResponse updateSchedule(java.util.UUID staffId, Integer dayOfWeek, UpdateStaffScheduleRequest request);

    /**
     * Staff schedule o'chirish (isAvailable = false qilish)
     */
    void deleteSchedule(java.util.UUID staffId, Integer dayOfWeek);

    /**
     * Tenant bo'yicha barcha staff schedules ni olish
     */
    List<StaffScheduleResponse> getAllSchedulesByTenant();

    // ==================== SERVICE ASSIGNMENT ====================

    /**
     * Staff ga service biriktirish
     */
    StaffResponse assignServiceToStaff(java.util.UUID staffId, java.util.UUID serviceId);

    /**
     * Staff dan service olib tashlash
     */
    StaffResponse removeServiceFromStaff(java.util.UUID staffId, java.util.UUID serviceId);

    /**
     * Staff ga bir nechta service biriktirish
     */
    StaffResponse assignServicesToStaff(java.util.UUID staffId, List<java.util.UUID> serviceIds);

    // ==================== STATISTICS ====================

    /**
     * Tenant bo'yicha staff statistikasi
     */
    StaffStatisticsResponse getStaffStatistics();
}