package org.architect.multitenantappointmentsystem.dto.request;

import java.time.LocalTime;

public record UpdateTenantRequest(
        String organizationName,
        String email,
        String phone,
        String address,
        LocalTime workingHoursStart,
        LocalTime workingHoursEnd,
        Integer slotDuration,
        Integer advanceBookingDays,
        Boolean autoConfirmBooking
) {
}
