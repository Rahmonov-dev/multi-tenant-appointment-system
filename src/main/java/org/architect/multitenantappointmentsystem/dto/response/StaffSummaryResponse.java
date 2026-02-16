package org.architect.multitenantappointmentsystem.dto.response;

public record StaffSummaryResponse(
        Long id,
        String displayName,
        String position,
        Boolean isActive
) {}