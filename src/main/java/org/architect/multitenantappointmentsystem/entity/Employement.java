package org.architect.multitenantappointmentsystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "employements")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Employement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private java.util.UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer duration;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    private String imageUrl;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "is_active")
    private Boolean isActive;

    @ManyToMany(mappedBy = "employements")
    private List<Staff> staff = new ArrayList<>();

    @OneToMany(mappedBy = "employement", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Appointment> appointments = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper methods for formatting
    public String getFormattedDuration() {
        if (duration == null) return "";
        int hours = duration / 60;
        int minutes = duration % 60;
        if (hours > 0) {
            return hours + " soat " + minutes + " daqiqa";
        }
        return minutes + " daqiqa";
    }

    public String getFormattedPrice() {
        if (price == null) return "0 so'm";
        return price.toString() + " so'm";
    }
}
