package org.architect.multitenantappointmentsystem.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Email bo'sh bo'lmasligi kerak")
        @Email(message = "Email noto'g'ri formatda")
        String email,
        @NotBlank(message = "Password bo'sh bo'lmasligi kerak")
        @Size(min = 6, message = "Password kamida 6 ta belgi bo'lishi kerak")
        String password
) {
}
