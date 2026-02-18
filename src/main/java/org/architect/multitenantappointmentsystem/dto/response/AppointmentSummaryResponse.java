package org.architect.multitenantappointmentsystem.dto.response;

import org.architect.multitenantappointmentsystem.entity.Appointment;

import java.time.LocalDate;
import java.time.LocalTime;

public record AppointmentSummaryResponse(
        java.util.UUID id,
        String customerName,
        String serviceName,
        String staffName,
        LocalDate appointmentDate,
        LocalTime startTime,
        String status,
        String statusIcon
) {
    
    public static AppointmentSummaryResponse fromEntity(Appointment appointment) {
        if (appointment == null) {
            return null;
        }
        
        return new AppointmentSummaryResponse(
                appointment.getId(),
                appointment.getCustomerName(),
                appointment.getEmployement() != null ? appointment.getEmployement().getName() : null,
                appointment.getStaff() != null ? appointment.getStaff().getDisplayName() : null,
                appointment.getAppointmentDate(),
                appointment.getStartTime(),
                appointment.getStatus() != null ? appointment.getStatus().name() : null,
                appointment.getStatus() != null ? appointment.getStatus().getIcon() : null
        );
    }
}