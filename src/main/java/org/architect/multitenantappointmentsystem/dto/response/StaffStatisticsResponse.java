package org.architect.multitenantappointmentsystem.dto.response;

public record StaffStatisticsResponse(
        Long tenantId,
        Long totalStaff,
        Long activeStaff,
        Long inactiveStaff,
        Long totalSchedules,
        Long availableSchedules
) {}