package org.architect.multitenantappointmentsystem.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(name = "tenants")
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, updatable = false)
    private String tenantKey;

    @Column(unique = true, nullable = false, updatable = false)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BusinessType businessType;

    @Column(nullable = false)
    private String organizationName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private LocalTime workingHoursStart;

    @Column(nullable = false)
    private LocalTime workingHoursEnd;

    @Column(nullable = false)
    private Integer slotDuration;

    @Column(nullable = false)
    private Integer advanceBookingDays;

    @Column(nullable = false)
    private Boolean autoConfirmBooking = false;

    @Column(nullable = false)
    private String timezone = "Asia/Tashkent";

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Staff> staff = new ArrayList<>();

    @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Employement> employements = new ArrayList<>();

    @OneToMany(mappedBy = "tenant")
    private List<Appointment> appointments = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (tenantKey == null) {
            tenantKey = UUID.randomUUID().toString();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }


    public static String generateBaseSlug(String input) {
        if (input == null || input.isBlank()) {
            return "tenant";
        }

        return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "") // diacritics olib tashlash
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-") // faqat harflar va raqamlar
                .replaceAll("^-|-$", "") // boshi va oxiridagi - ni olib tashlash
                .substring(0, Math.min(input.length(), 50)); // max 50 ta belgi
    }
}