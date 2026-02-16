package org.architect.multitenantappointmentsystem.dto.request;

import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record AvailableSlotsRequest(
        @NotNull(message = "Staff ID bo'sh bo'lmasligi kerak")
        Long staffId,

        @NotNull(message = "Sana bo'sh bo'lmasligi kerak")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate date,

        Long serviceId  // Optional - agar berilsa service duration bo'yicha filter qiladi
) {}