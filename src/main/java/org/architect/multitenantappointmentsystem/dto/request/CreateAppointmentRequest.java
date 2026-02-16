package org.architect.multitenantappointmentsystem.dto.request;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;


public record CreateAppointmentRequest(
        @NotNull(message = "Tenant ID bo'sh bo'lmasligi kerak")
        Long tenantId,

        @NotNull(message = "Staff ID bo'sh bo'lmasligi kerak")
        Long staffId,

        @NotNull(message = "Employement ID bo'sh bo'lmasligi kerak")
        Long serviceId,

        @NotBlank(message = "Mijoz ismi bo'sh bo'lmasligi kerak")
        @Size(max = 200, message = "Mijoz ismi 200 ta belgidan oshmasligi kerak")
        String customerName,

        @NotBlank(message = "Telefon raqami bo'sh bo'lmasligi kerak")
        @Pattern(regexp = "^\\+998[0-9]{9}$", 
                message = "Telefon raqami +998XXXXXXXXX formatida bo'lishi kerak")
        String customerPhone,

        @Email(message = "Email formati noto'g'ri")
        String customerEmail,

        @NotNull(message = "Sana bo'sh bo'lmasligi kerak")
        @Future(message = "Sana kelajakda bo'lishi kerak")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate appointmentDate,

        @NotNull(message = "Boshlanish vaqti bo'sh bo'lmasligi kerak")
        LocalTime startTime,

        @Size(max = 1000, message = "Izoh 1000 ta belgidan oshmasligi kerak")
        String notes
) {}