package org.architect.multitenantappointmentsystem.dto.response;

public record ServiceShortResponse(
        java.util.UUID id,
        String name,
        String description,
        Integer durationMinutes,
        Boolean isActive
) {}

