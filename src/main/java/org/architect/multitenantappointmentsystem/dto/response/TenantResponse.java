package org.architect.multitenantappointmentsystem.dto.response;

import org.architect.multitenantappointmentsystem.entity.Tenant;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record TenantResponse(
        UUID id,
        String slug,
        String businessType,
        String organizationName,
        String email,
        String phone,
        String address,
        LocalTime workingHoursStart,
        LocalTime workingHoursEnd,
        Integer slotDuration,
        Integer advanceBookingDays,
        Boolean autoConfirmBooking,
        String timezone,
        Boolean isActive
) {
    public static TenantResponse from(Tenant tenant) {
        return new TenantResponse(
                tenant.getId(),
                tenant.getSlug(),
                tenant.getBusinessType().name(),
                tenant.getOrganizationName(),
                tenant.getEmail(),
                tenant.getPhone(),
                tenant.getAddress(),
                tenant.getWorkingHoursStart(),
                tenant.getWorkingHoursEnd(),
                tenant.getSlotDuration(),
                tenant.getAdvanceBookingDays(),
                tenant.getAutoConfirmBooking(),
                tenant.getTimezone(),
                tenant.getIsActive()
        );
    }
}
