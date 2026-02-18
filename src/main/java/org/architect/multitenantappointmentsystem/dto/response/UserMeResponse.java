package org.architect.multitenantappointmentsystem.dto.response;

import java.util.List;

public record UserMeResponse(
        java.util.UUID id,
        String fullName,
        String phone,
        String email,
        List<String> roles
) {
}
