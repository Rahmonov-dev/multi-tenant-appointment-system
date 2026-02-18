package org.architect.multitenantappointmentsystem.dto.response;

import org.architect.multitenantappointmentsystem.entity.Employement;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ServiceDetailResponse(
        java.util.UUID id,
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
        List<StaffSummaryResponse> availableStaff,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ServiceDetailResponse fromEntity(Employement employement) {
        List<StaffSummaryResponse> staffResponses = employement.getStaff()
                .stream()
                .map(staff -> new StaffSummaryResponse(
                        staff.getId(),
                        staff.getDisplayName(),
                        staff.getPosition(),
                        staff.getIsActive()
                ))
                .toList();

        return new ServiceDetailResponse(
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
                staffResponses,
                employement.getCreatedAt(),
                employement.getUpdatedAt()
        );
    }
}