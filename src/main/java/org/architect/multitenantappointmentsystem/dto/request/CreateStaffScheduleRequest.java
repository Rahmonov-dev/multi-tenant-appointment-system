package org.architect.multitenantappointmentsystem.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalTime;

public record CreateStaffScheduleRequest(
                @NotNull(message = "Day of week bo'sh bo'lmasligi kerak") @Min(value = 1, message = "Day of week 1 dan kichik bo'lmasligi kerak (1=Dushanba)") @Max(value = 7, message = "Day of week 7 dan katta bo'lmasligi kerak (7=Yakshanba)") Integer dayOfWeek,

                @NotNull(message = "Start time bo'sh bo'lmasligi kerak") LocalTime startTime,

                @NotNull(message = "End time bo'sh bo'lmasligi kerak") LocalTime endTime,

                @NotNull(message = "Is available bo'sh bo'lmasligi kerak") Boolean isAvailable) {
}