package org.architect.multitenantappointmentsystem.dto.response;

import java.util.List;

public record UserMeResponse(
        Long id,
        String fullName,
        String phone,
        String email,
        List<String> roles
) {
}
