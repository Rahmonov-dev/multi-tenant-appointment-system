package org.architect.multitenantappointmentsystem.dto.response;

import org.architect.multitenantappointmentsystem.entity.Appointment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.text.DecimalFormat;

public record AppointmentResponse(
        Long id,
        Long tenantId,
        String tenantName,
        Long staffId,
        String staffName,
        String staffPosition,
        Long serviceId,
        String serviceName,
        Integer serviceDuration,
        String customerName,
        String customerPhone,
        String customerEmail,
        LocalDate appointmentDate,
        LocalTime startTime,
        LocalTime endTime,
        String formattedTime,
        String status,
        String statusDisplayName,
        String statusIcon,
        BigDecimal totalPrice,
        String formattedPrice,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime confirmedAt,
        LocalDateTime completedAt,
        LocalDateTime cancelledAt,
        LocalDateTime updatedAt
) {
    
    private static final DecimalFormat PRICE_FORMATTER = new DecimalFormat("#,###.00");
    
    public static AppointmentResponse fromEntity(Appointment appointment) {
        if (appointment == null) {
            return null;
        }
        
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getTenant() != null ? appointment.getTenant().getId() : null,
                appointment.getTenant() != null ? appointment.getTenant().getOrganizationName() : null,
                appointment.getStaff() != null ? appointment.getStaff().getId() : null,
                appointment.getStaff() != null ? appointment.getStaff().getDisplayName(): null,
                appointment.getStaff() != null ? appointment.getStaff().getPosition() : null,
                appointment.getEmployement() != null ? appointment.getEmployement().getId() : null,
                appointment.getEmployement() != null ? appointment.getEmployement().getName() : null,
                appointment.getEmployement() != null ? appointment.getEmployement().getDuration() : null,
                appointment.getCustomerName(),
                appointment.getCustomerPhone(),
                appointment.getCustomerEmail(),
                appointment.getAppointmentDate(),
                appointment.getStartTime(),
                appointment.getEndTime(),
                appointment.getFormattedTime(),
                appointment.getStatus() != null ? appointment.getStatus().name() : null,
                appointment.getStatus() != null ? appointment.getStatus().getDisplayName() : null,
                appointment.getStatus() != null ? appointment.getStatus().getIcon() : null,
                appointment.getTotalPrice(),
                appointment.getTotalPrice() != null ? PRICE_FORMATTER.format(appointment.getTotalPrice()) : null,
                appointment.getNotes(),
                appointment.getCreatedAt(),
                appointment.getConfirmedAt(),
                appointment.getCompletedAt(),
                appointment.getCancelledAt(),
                appointment.getUpdatedAt()
        );
    }
}
