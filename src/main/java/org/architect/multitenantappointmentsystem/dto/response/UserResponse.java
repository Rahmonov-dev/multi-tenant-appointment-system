package org.architect.multitenantappointmentsystem.dto.response;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String status
) {
}
