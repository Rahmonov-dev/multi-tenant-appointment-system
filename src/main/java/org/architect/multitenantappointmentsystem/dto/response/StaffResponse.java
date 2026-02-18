package org.architect.multitenantappointmentsystem.dto.response;

import org.architect.multitenantappointmentsystem.entity.Staff;

import java.time.LocalDateTime;
import java.util.List;

public record StaffResponse(
        java.util.UUID id,
        Long tenantId,
        String tenantName,
        java.util.UUID userId,
        String userFullName,
        String role,
        String displayName,
        Integer totalServices,
        String position,
        Boolean isActive,
        LocalDateTime createdAt,
        List<StaffScheduleResponse> scheduleResponse
) {
    public static StaffResponse fromEntity(Staff staff) {
        return new StaffResponse(
                staff.getId(),
                staff.getTenant().getId(),
                staff.getTenant().getOrganizationName(),
                staff.getUser().getId(),
                staff.getUser().getFullName(),
                staff.getRole().name(),
                staff.getDisplayName(),
                staff.getTotalServices(),
                staff.getPosition(),
                staff.getIsActive(),
                staff.getCreatedAt(),
                StaffScheduleResponse.fromEntityList(staff.getSchedules())
        );
    }
}

