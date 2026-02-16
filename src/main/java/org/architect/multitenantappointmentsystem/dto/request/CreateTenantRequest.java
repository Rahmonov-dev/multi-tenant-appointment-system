package org.architect.multitenantappointmentsystem.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import org.architect.multitenantappointmentsystem.entity.BusinessType;

import java.time.LocalTime;

public record CreateTenantRequest(
        @NotNull(message = "Biznes turi tanlanishi kerak")
        BusinessType businessType,

        @NotBlank(message = "Organizatsiya nomi bo'sh bo'lmasligi kerak")
        @Size(max = 200)
        String organizationName,

        @NotBlank(message = "Email bo'sh bo'lmasligi kerak")
        @Email(message = "Email noto'g'ri formatda")
        String email,

        @NotBlank(message = "Telefon bo'sh bo'lmasligi kerak")
        @Pattern(regexp = "^+998[0-9]{9}$")
        String phone,

        @NotBlank(message = "Manzil bo'sh bo'lmasligi kerak")
        @Size(max = 500)
        String address,

        @NotNull(message = "Ish boshlanish vaqti kerak")
        @JsonFormat(pattern = "HH:mm")
        LocalTime workingHoursStart,

        @NotNull(message = "Ish tugash vaqti kerak")
        @JsonFormat(pattern = "HH:mm")
        LocalTime workingHoursEnd,

        @NotNull(message = "Slot davomiyligi kerak")
        @Min(value = 15, message = "Minimum 15 minut")
        @Max(value = 240, message = "Maximum 240 minut")
        Integer slotDuration,

        @NotNull(message = "Advance booking days kerak")
        @Min(value = 1, message = "Minimum 1 kun")
        @Max(value = 365, message = "Maximum 7 kun")
        Integer advanceBookingDays,

        Boolean autoConfirmBooking,

        String timezone
) {
}
