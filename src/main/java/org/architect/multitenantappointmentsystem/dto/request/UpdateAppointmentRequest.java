package org.architect.multitenantappointmentsystem.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateAppointmentRequest(
        @Size(max = 200, message = "Mijoz ismi 200 ta belgidan oshmasligi kerak")
        String customerName,

        @Size(max = 1000, message = "Izoh 1000 ta belgidan oshmasligi kerak")
        String notes
) {}