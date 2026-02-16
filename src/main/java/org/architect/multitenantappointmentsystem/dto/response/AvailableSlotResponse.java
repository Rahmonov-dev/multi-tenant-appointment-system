package org.architect.multitenantappointmentsystem.dto.response;

import java.time.LocalTime;

public record AvailableSlotResponse(
        LocalTime time,
        Boolean available,
        String displayTime  // "09:00" formatda
) {}