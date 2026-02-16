package org.architect.multitenantappointmentsystem.dto.response;

import org.architect.multitenantappointmentsystem.entity.Employement;

import java.math.BigDecimal;

public record ServiceSummaryResponse(
        Long id,
        String name,
        Integer duration,
        String formattedDuration,
        BigDecimal price,
        String formattedPrice,
        Boolean isActive
) {
    public static ServiceSummaryResponse fromEntity(Employement employement) {
        return new ServiceSummaryResponse(
                employement.getId(),
                employement.getName(),
                employement.getDuration(),
                employement.getFormattedDuration(),
                employement.getPrice(),
                employement.getFormattedPrice(),
                employement.getIsActive()
        );
    }
}