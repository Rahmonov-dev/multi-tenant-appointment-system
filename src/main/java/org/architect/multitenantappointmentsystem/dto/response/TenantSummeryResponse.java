package org.architect.multitenantappointmentsystem.dto.response;

public record TenantSummeryResponse(
        Long id,
        String tenantKey,
        String slug,
        String businessType,
        String organizationName,
        Boolean isActive
) {
}
