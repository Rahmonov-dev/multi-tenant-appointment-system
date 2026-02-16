package org.architect.multitenantappointmentsystem.dto.response;

public record ServiceShortResponse(
        Long id,
        String name,
        String description,
        Integer durationMinutes,
        Boolean isActive
) {}

