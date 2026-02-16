package org.architect.multitenantappointmentsystem.dto.response;

import java.time.LocalDate;

public record AppointmentCalendarResponse(
        LocalDate date,
        String dayName,
        Integer totalAppointments,
        Integer confirmedAppointments,
        Integer pendingAppointments,
        Integer completedAppointments
) {
    
    public static AppointmentCalendarResponse fromEntity(
            LocalDate date,
            String dayName,
            Integer totalAppointments,
            Integer confirmedAppointments,
            Integer pendingAppointments,
            Integer completedAppointments
    ) {
        return new AppointmentCalendarResponse(
                date,
                dayName,
                totalAppointments,
                confirmedAppointments,
                pendingAppointments,
                completedAppointments
        );
    }
}