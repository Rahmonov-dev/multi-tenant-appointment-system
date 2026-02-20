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
import java.util.UUID;

/**
 * Staff management service interface
 */
public interface StaffService {


    // ==================== STAFF CRUD ====================

    StaffResponse createStaff(UUID tenantId, CreateStaffRequest request);

    StaffResponse getStaffById(UUID tenantId, UUID id);

    StaffDetailResponse getStaffDetailById(UUID tenantId, UUID id);

    StaffResponse updateStaff(UUID tenantId, UUID id, UpdateStaffRequest request);

    void deleteStaff(UUID tenantId, UUID id);

    StaffResponse activateStaff(UUID tenantId, UUID id);

    StaffResponse deactivateStaff(UUID tenantId, UUID id);

    // ==================== STAFF QUERIES ====================

    List<StaffResponse> getAllStaffByTenant(UUID tenantId);

    List<StaffResponse> getActiveStaffByTenant(UUID tenantId);

    List<StaffResponse> getStaffByTenantAndRole(UUID tenantId, StaffRole role);

    List<StaffResponse> getStaffByService(UUID tenantId, UUID serviceId);

    Page<StaffResponse> getStaffByTenantPaginated(UUID tenantId, Boolean activeOnly, Pageable pageable);

    // ==================== SCHEDULE MANAGEMENT ====================

    StaffScheduleResponse createOrUpdateSchedule(UUID tenantId, UUID staffId, CreateStaffScheduleRequest request);

    List<StaffScheduleResponse> getStaffSchedules(UUID tenantId, UUID staffId);

    StaffScheduleResponse getStaffScheduleByDay(UUID tenantId, UUID staffId, Integer dayOfWeek);

    StaffScheduleResponse updateSchedule(UUID tenantId, UUID staffId, Integer dayOfWeek, UpdateStaffScheduleRequest request);

    void deleteSchedule(UUID tenantId, UUID staffId, Integer dayOfWeek);

    List<StaffScheduleResponse> getAllSchedulesByTenant(UUID tenantId);

    // ==================== SERVICE ASSIGNMENT ====================

    StaffResponse assignServiceToStaff(UUID tenantId, UUID staffId, UUID serviceId);

    StaffResponse removeServiceFromStaff(UUID tenantId, UUID staffId, UUID serviceId);

    StaffResponse assignServicesToStaff(UUID tenantId, UUID staffId, List<UUID> serviceIds);

    // ==================== STATISTICS ====================

    StaffStatisticsResponse getStaffStatistics(UUID tenantId);
}