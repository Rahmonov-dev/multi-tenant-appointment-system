package org.architect.multitenantappointmentsystem.repository;

import org.architect.multitenantappointmentsystem.entity.Appointment;
import org.architect.multitenantappointmentsystem.entity.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, java.util.UUID> {

        // ==================== DATE-BASED QUERIES ====================

        List<Appointment> findByTenantIdAndAppointmentDate(Long tenantId, LocalDate date);

        List<Appointment> findByStaffIdAndAppointmentDate(java.util.UUID staffId, LocalDate date);

        List<Appointment> findByEmployementIdAndAppointmentDate(java.util.UUID serviceId, LocalDate date);

        @Query("SELECT a FROM Appointment a WHERE a.tenant.id = :tenantId " +
                        "AND a.appointmentDate BETWEEN :startDate AND :endDate " +
                        "ORDER BY a.appointmentDate, a.startTime")
        List<Appointment> findByTenantIdAndDateRange(@Param("tenantId") Long tenantId,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        @Query("SELECT a FROM Appointment a WHERE a.staff.id = :staffId " +
                        "AND a.appointmentDate BETWEEN :startDate AND :endDate " +
                        "ORDER BY a.appointmentDate, a.startTime")
        List<Appointment> findByStaffIdAndDateRange(@Param("staffId") java.util.UUID staffId,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        // ==================== STATUS-BASED QUERIES ====================

        List<Appointment> findByTenantIdAndStatus(Long tenantId, AppointmentStatus status);

        List<Appointment> findByStaffIdAndStatus(java.util.UUID staffId, AppointmentStatus status);

        @Query("SELECT a FROM Appointment a WHERE a.tenant.id = :tenantId " +
                        "AND a.appointmentDate = :date AND a.status IN :statuses " +
                        "ORDER BY a.startTime")
        List<Appointment> findByTenantIdAndDateAndStatuses(@Param("tenantId") Long tenantId,
                        @Param("date") LocalDate date,
                        @Param("statuses") List<AppointmentStatus> statuses);

        @Query("SELECT a FROM Appointment a WHERE a.staff.id = :staffId " +
                        "AND a.appointmentDate = :date AND a.status IN :statuses " +
                        "ORDER BY a.startTime")
        List<Appointment> findActiveAppointments(@Param("staffId") java.util.UUID staffId,
                        @Param("date") LocalDate date,
                        @Param("statuses") List<AppointmentStatus> statuses);

        // ==================== CUSTOMER QUERIES ====================

        List<Appointment> findByCustomerPhone(String customerPhone);

        List<Appointment> findByCustomerPhoneOrderByAppointmentDateDesc(String customerPhone);

        @Query("SELECT a FROM Appointment a WHERE a.customerPhone = :phone " +
                        "AND a.appointmentDate >= :date " +
                        "ORDER BY a.appointmentDate, a.startTime")
        List<Appointment> findUpcomingAppointmentsByPhone(@Param("phone") String customerPhone,
                        @Param("date") LocalDate date);

        @Query("SELECT a FROM Appointment a WHERE a.customerEmail = :email " +
                        "AND a.appointmentDate >= :date " +
                        "ORDER BY a.appointmentDate, a.startTime")
        List<Appointment> findUpcomingAppointmentsByEmail(@Param("email") String email,
                        @Param("date") LocalDate date);

        // ==================== TIME CONFLICT CHECKING ====================

        boolean existsByStaffIdAndAppointmentDateAndStartTime(java.util.UUID staffId,
                        LocalDate date,
                        LocalTime time);

        @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Appointment a " +
                        "WHERE a.staff.id = :staffId AND a.appointmentDate = :date " +
                        "AND a.status IN ('PENDING', 'CONFIRMED') " +
                        "AND ((a.startTime <= :startTime AND a.endTime > :startTime) " +
                        "OR (a.startTime < :endTime AND a.endTime >= :endTime) " +
                        "OR (a.startTime >= :startTime AND a.endTime <= :endTime))")
        boolean hasTimeConflict(@Param("staffId") java.util.UUID staffId,
                        @Param("date") LocalDate date,
                        @Param("startTime") LocalTime startTime,
                        @Param("endTime") LocalTime endTime);

        // ==================== PAGINATION ====================

        Page<Appointment> findByTenantId(Long tenantId, Pageable pageable);

        Page<Appointment> findByStaffId(java.util.UUID staffId, Pageable pageable);

        Page<Appointment> findByTenantIdAndStatus(Long tenantId, AppointmentStatus status, Pageable pageable);

        // ==================== STATISTICS ====================

        long countByTenantId(Long tenantId);

        long countByTenantIdAndStatus(Long tenantId, AppointmentStatus status);

        long countByStaffId(java.util.UUID staffId);

        long countByStaffIdAndStatus(java.util.UUID staffId, AppointmentStatus status);

        @Query("SELECT COUNT(a) FROM Appointment a WHERE a.tenant.id = :tenantId " +
                        "AND a.appointmentDate = :date")
        long countByTenantIdAndDate(@Param("tenantId") Long tenantId, @Param("date") LocalDate date);

        @Query("SELECT COUNT(a) FROM Appointment a WHERE a.staff.id = :staffId " +
                        "AND a.appointmentDate BETWEEN :startDate AND :endDate")
        long countByStaffIdAndDateRange(@Param("staffId") java.util.UUID staffId,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        // ==================== TODAY'S APPOINTMENTS ====================

        @Query("SELECT a FROM Appointment a WHERE a.tenant.id = :tenantId " +
                        "AND a.appointmentDate = CURRENT_DATE " +
                        "AND a.status IN ('PENDING', 'CONFIRMED') " +
                        "ORDER BY a.startTime")
        List<Appointment> findTodayAppointments(@Param("tenantId") Long tenantId);

        @Query("SELECT a FROM Appointment a WHERE a.staff.id = :staffId " +
                        "AND a.appointmentDate = CURRENT_DATE " +
                        "AND a.status IN ('PENDING', 'CONFIRMED') " +
                        "ORDER BY a.startTime")
        List<Appointment> findTodayAppointmentsByStaff(@Param("staffId") java.util.UUID staffId);

        // ==================== UPCOMING APPOINTMENTS ====================

        @Query("SELECT a FROM Appointment a WHERE a.tenant.id = :tenantId " +
                        "AND a.appointmentDate >= CURRENT_DATE " +
                        "AND a.status IN ('PENDING', 'CONFIRMED') " +
                        "ORDER BY a.appointmentDate, a.startTime")
        List<Appointment> findUpcomingAppointments(@Param("tenantId") Long tenantId, Pageable pageable);

        // ==================== PAST APPOINTMENTS ====================

        @Query("SELECT a FROM Appointment a WHERE a.customerPhone = :phone " +
                        "AND a.appointmentDate < CURRENT_DATE " +
                        "ORDER BY a.appointmentDate DESC, a.startTime DESC")
        List<Appointment> findPastAppointmentsByPhone(@Param("phone") String customerPhone, Pageable pageable);

        @Query("SELECT a FROM Appointment a WHERE a.customerPhone = :phone " +
                        "AND a.appointmentDate < CURRENT_DATE AND a.tenant.id = :tenantId " +
                        "ORDER BY a.appointmentDate DESC, a.startTime DESC")
        List<Appointment> findPastAppointmentsByPhoneAndTenantId(@Param("phone") String customerPhone,
                        @Param("tenantId") Long tenantId,
                        Pageable pageable);

        // ==================== TENANT VALIDATION METHODS ====================

        @EntityGraph(attributePaths = { "staff", "tenant", "employement",
                        "staff.user" })
        Optional<Appointment> findByIdAndTenantId(java.util.UUID id, Long tenantId);

        @EntityGraph(attributePaths = { "staff", "tenant", "employement",
                        "staff.user" })
        List<Appointment> findByCustomerPhoneAndTenantId(String customerPhone, Long tenantId);

        @Query("SELECT a FROM Appointment a WHERE a.customerPhone = :phone " +
                        "AND a.appointmentDate >= :date AND a.tenant.id = :tenantId " +
                        "ORDER BY a.appointmentDate, a.startTime")
        @EntityGraph(attributePaths = { "staff", "tenant", "employement",
                        "staff.user" })
        List<Appointment> findUpcomingAppointmentsByPhoneAndTenantId(@Param("phone") String customerPhone,
                        @Param("date") LocalDate date,
                        @Param("tenantId") Long tenantId);
}