package org.architect.multitenantappointmentsystem.dto.response;

public record UserResponse(
        java.util.UUID id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String status
) {
}
