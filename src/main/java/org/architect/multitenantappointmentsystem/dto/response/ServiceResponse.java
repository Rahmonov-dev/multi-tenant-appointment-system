package org.architect.multitenantappointmentsystem.dto.response;

import org.architect.multitenantappointmentsystem.entity.Employement;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ServiceResponse(
        Long id,
        Long tenantId,
        String tenantName,
        String name,
        String description,
        Integer duration,
        String formattedDuration,
        BigDecimal price,
        String formattedPrice,
        String imageUrl,
        Boolean isActive,
        Integer displayOrder,
        Integer totalStaff,
        Integer totalAppointments,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ServiceResponse fromEntity(Employement employement) {
        return new ServiceResponse(
                employement.getId(),
                employement.getTenant().getId(),
                employement.getTenant().getOrganizationName(),
                employement.getName(),
                employement.getDescription(),
                employement.getDuration(),
                employement.getFormattedDuration(),
                employement.getPrice(),
                employement.getFormattedPrice(),
                employement.getImageUrl(),
                employement.getIsActive(),
                employement.getDisplayOrder(),
                employement.getStaff() != null ? employement.getStaff().size() : 0,
                employement.getAppointments() != null ? employement.getAppointments().size() : 0,
                employement.getCreatedAt(),
                employement.getUpdatedAt()
        );
    }
}