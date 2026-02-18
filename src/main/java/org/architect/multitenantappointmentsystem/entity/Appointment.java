package org.architect.multitenantappointmentsystem.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "appointments",
        indexes = {
                @Index(name = "idx_tenant_date", columnList = "tenant_id, appointment_date"),
                @Index(name = "idx_staff_date", columnList = "staff_id, appointment_date"),
                @Index(name = "idx_status", columnList = "status")
        })
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private java.util.UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private Employement employement;
    @Column(nullable = false)
    private String customerName;
    @Column(nullable = false)
    private String customerPhone;
    @Column(name = "customer_email")
    private String customerEmail;
    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status = AppointmentStatus.PENDING;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;
    @Column(columnDefinition = "TEXT")
    private String notes;
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (endTime == null && startTime != null && employement != null) {
            endTime = startTime.plusMinutes(employement.getDuration());
        }

        if (totalPrice == null && employement != null) {
            totalPrice = employement.getPrice();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (status == AppointmentStatus.CONFIRMED && confirmedAt == null) {
            confirmedAt = LocalDateTime.now();
        } else if (status == AppointmentStatus.COMPLETED && completedAt == null) {
            completedAt = LocalDateTime.now();
        } else if (status == AppointmentStatus.CANCELLED && cancelledAt == null) {
            cancelledAt = LocalDateTime.now();
        }
    }

    public void confirm() {
        this.status = AppointmentStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = AppointmentStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }

    public void complete() {
        this.status = AppointmentStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return status == AppointmentStatus.PENDING ||
                status == AppointmentStatus.CONFIRMED;
    }
    public boolean isFinal() {
        return status == AppointmentStatus.COMPLETED ||
                status == AppointmentStatus.CANCELLED ||
                status == AppointmentStatus.NO_SHOW;
    }

    public LocalDateTime getFullDateTime() {
        return LocalDateTime.of(appointmentDate, startTime);
    }

    public String getFormattedTime() {
        return startTime + " - " + endTime;
    }
}