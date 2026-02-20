package org.architect.multitenantappointmentsystem.repository;

import org.architect.multitenantappointmentsystem.entity.StaffSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StaffScheduleRepository extends JpaRepository<StaffSchedule, java.util.UUID> {

    // Basic queries
    List<StaffSchedule> findByStaffId(UUID staffId);

    Optional<StaffSchedule> findByStaffIdAndDayOfWeek(UUID staffId, Integer dayOfWeek);

    List<StaffSchedule> findByStaffIdAndIsAvailable(UUID staffId, Boolean isAvailable);

    // Tenant-related queries
    @Query("SELECT ss FROM StaffSchedule ss WHERE ss.staff.tenant.id = :tenantId")
    List<StaffSchedule> findByTenantId(@Param("tenantId") UUID tenantId);

    @Query("SELECT ss FROM StaffSchedule ss WHERE ss.staff.tenant.id = :tenantId AND ss.dayOfWeek = :dayOfWeek")
    List<StaffSchedule> findByTenantIdAndDayOfWeek(@Param("tenantId") UUID tenantId, @Param("dayOfWeek") Integer dayOfWeek);

    // Available schedules
    @Query("SELECT ss FROM StaffSchedule ss WHERE ss.staff.id = :staffId AND ss.dayOfWeek = :dayOfWeek AND ss.isAvailable = true")
    Optional<StaffSchedule> findAvailableSchedule(@Param("staffId") UUID staffId, @Param("dayOfWeek") Integer dayOfWeek);

    @Query("SELECT ss FROM StaffSchedule ss WHERE ss.staff.tenant.id = :tenantId AND ss.isAvailable = true ORDER BY ss.dayOfWeek")
    List<StaffSchedule> findAllAvailableSchedulesByTenantId(@Param("tenantId") UUID tenantId);

    // Working time validation
    @Query("SELECT CASE WHEN COUNT(ss) > 0 THEN true ELSE false END FROM StaffSchedule ss " +
            "WHERE ss.staff.id = :staffId AND ss.dayOfWeek = :dayOfWeek AND ss.isAvailable = true " +
            "AND ss.startTime <= :time AND ss.endTime > :time")
    boolean isStaffWorkingAtTime(@Param("staffId") UUID staffId,
                                 @Param("dayOfWeek") Integer dayOfWeek,
                                 @Param("time") LocalTime time);

    // Delete/cleanup
    void deleteByStaffId(UUID staffId);

    boolean existsByStaffIdAndDayOfWeek(UUID staffId, Integer dayOfWeek);

    Optional<StaffSchedule> findByStaffIdAndDayOfWeekAndStaffTenantId(UUID staffId, Integer dayOfWeek, UUID tenantId);

    List<StaffSchedule> findByStaffTenantId(UUID tenantId);

    long countByStaffTenantIdAndIsAvailableTrue(UUID tenantId);

    long countByStaffTenantId(UUID tenantId);
    Optional<StaffSchedule> findByStaffIdAndDayOfWeekAndStaff_TenantId(
            UUID staffId,
            Integer dayOfWeek,
            UUID tenantId
    );

}