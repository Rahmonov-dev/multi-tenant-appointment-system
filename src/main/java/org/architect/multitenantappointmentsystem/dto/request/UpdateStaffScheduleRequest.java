package org.architect.multitenantappointmentsystem.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;


public record UpdateStaffScheduleRequest(

        @NotNull(message = "Start time majburiy")
        LocalTime startTime,

        @NotNull(message = "End time majburiy")
        LocalTime endTime,

        @NotNull(message = "Availability majburiy")
        Boolean isAvailable

) {}