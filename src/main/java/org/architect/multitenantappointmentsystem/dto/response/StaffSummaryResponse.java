package org.architect.multitenantappointmentsystem.dto.response;

public record StaffSummaryResponse(
        java.util.UUID id,
        String displayName,
        String position,
        Boolean isActive
) {}