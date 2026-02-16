package org.architect.multitenantappointmentsystem.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

public record RescheduleAppointmentRequest(
        @NotNull(message = "Yangi sana bo'sh bo'lmasligi kerak")
        @Future(message = "Yangi sana kelajakda bo'lishi kerak")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate newDate,

        @NotNull(message = "Yangi vaqt bo'sh bo'lmasligi kerak")
        LocalTime newTime,

        @Size(max = 500, message = "Sabab 500 ta belgidan oshmasligi kerak")
        String reason
) {}