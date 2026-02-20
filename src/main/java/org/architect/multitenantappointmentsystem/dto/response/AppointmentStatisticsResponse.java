package org.architect.multitenantappointmentsystem.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record AppointmentStatisticsResponse(
        UUID tenantId,
        Long totalAppointments,
        Long pendingAppointments,
        Long confirmedAppointments,
        Long completedAppointments,
        Long cancelledAppointments,
        Long noShowAppointments,
        BigDecimal totalRevenue,
        BigDecimal pendingRevenue,
        BigDecimal completedRevenue,
        Double averageAppointmentsPerDay,
        Double completionRate,
        Double cancellationRate
) {
    
    public static AppointmentStatisticsResponse fromEntity(
            UUID tenantId,
            Long totalAppointments,
            Long pendingAppointments,
            Long confirmedAppointments,
            Long completedAppointments,
            Long cancelledAppointments,
            Long noShowAppointments,
            BigDecimal totalRevenue,
            BigDecimal pendingRevenue,
            BigDecimal completedRevenue,
            Double averageAppointmentsPerDay,
            Double completionRate,
            Double cancellationRate
    ) {
        return new AppointmentStatisticsResponse(
                tenantId,
                totalAppointments,
                pendingAppointments,
                confirmedAppointments,
                completedAppointments,
                cancelledAppointments,
                noShowAppointments,
                totalRevenue,
                pendingRevenue,
                completedRevenue,
                averageAppointmentsPerDay,
                completionRate,
                cancellationRate
        );
    }
}
