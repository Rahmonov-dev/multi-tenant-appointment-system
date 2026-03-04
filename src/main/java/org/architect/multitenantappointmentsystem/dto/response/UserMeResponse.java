package org.architect.multitenantappointmentsystem.dto.response;

import java.util.List;
import java.util.UUID;

public record UserMeResponse(
        UUID id,
        String fullName,
        String phone,
        String email,
        List<String> roles,
        UUID tenantId,
        String tenantSlug,
        UUID staffId,
        UUID staffTenantId,
        String staffTenantSlug
) {
}
