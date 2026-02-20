package org.architect.multitenantappointmentsystem.dto.response;

import java.util.UUID;

public record StaffStatisticsResponse(
        UUID tenantId,
        Long totalStaff,
        Long activeStaff,
        Long inactiveStaff,
        Long totalSchedules,
        Long availableSchedules
) {}