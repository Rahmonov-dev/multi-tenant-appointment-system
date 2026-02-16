package org.architect.multitenantappointmentsystem.dto.request;

import jakarta.validation.constraints.Size;

public record CancelAppointmentRequest(
        @Size(max = 500, message = "Sabab 500 ta belgidan oshmasligi kerak")
        String reason
) {}