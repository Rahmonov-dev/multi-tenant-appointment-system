package org.architect.multitenantappointmentsystem.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Ism bo'sh bo'lmasligi kerak")
        @Size(max = 100)
        String firstName,

        @NotBlank(message = "Familiya bo'sh bo'lmasligi kerak")
        @Size(max = 100)
        String lastName,

        @NotBlank(message = "Email bo'sh bo'lmasligi kerak")
        @Email(message = "Email noto'g'ri formatda")
        String email,

        @NotBlank(message = "Password bo'sh bo'lmasligi kerak")
        @Size(min = 6, message = "Password kamida 6 ta belgi bo'lishi kerak")
        String password,

        @NotBlank(message = "Telefon bo'sh bo'lmasligi kerak")
        @Pattern(regexp = "^+998[0-9]{9}$", message = "Telefon: +998XXXXXXXXX")
        String phone
) {
}
