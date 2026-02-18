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

@Repository
public interface StaffScheduleRepository extends JpaRepository<StaffSchedule, java.util.UUID> {

    // Basic queries
    List<StaffSchedule> findByStaffId(java.util.UUID staffId);

    Optional<StaffSchedule> findByStaffIdAndDayOfWeek(java.util.UUID staffId, Integer dayOfWeek);

    List<StaffSchedule> findByStaffIdAndIsAvailable(java.util.UUID staffId, Boolean isAvailable);

    // Tenant-related queries
    @Query("SELECT ss FROM StaffSchedule ss WHERE ss.staff.tenant.id = :tenantId")
    List<StaffSchedule> findByTenantId(@Param("tenantId") Long tenantId);

    @Query("SELECT ss FROM StaffSchedule ss WHERE ss.staff.tenant.id = :tenantId AND ss.dayOfWeek = :dayOfWeek")
    List<StaffSchedule> findByTenantIdAndDayOfWeek(@Param("tenantId") Long tenantId, @Param("dayOfWeek") Integer dayOfWeek);

    // Available schedules
    @Query("SELECT ss FROM StaffSchedule ss WHERE ss.staff.id = :staffId AND ss.dayOfWeek = :dayOfWeek AND ss.isAvailable = true")
    Optional<StaffSchedule> findAvailableSchedule(@Param("staffId") java.util.UUID staffId, @Param("dayOfWeek") Integer dayOfWeek);

    @Query("SELECT ss FROM StaffSchedule ss WHERE ss.staff.tenant.id = :tenantId AND ss.isAvailable = true ORDER BY ss.dayOfWeek")
    List<StaffSchedule> findAllAvailableSchedulesByTenantId(@Param("tenantId") Long tenantId);

    // Working time validation
    @Query("SELECT CASE WHEN COUNT(ss) > 0 THEN true ELSE false END FROM StaffSchedule ss " +
            "WHERE ss.staff.id = :staffId AND ss.dayOfWeek = :dayOfWeek AND ss.isAvailable = true " +
            "AND ss.startTime <= :time AND ss.endTime > :time")
    boolean isStaffWorkingAtTime(@Param("staffId") java.util.UUID staffId,
                                 @Param("dayOfWeek") Integer dayOfWeek,
                                 @Param("time") LocalTime time);

    // Delete/cleanup
    void deleteByStaffId(java.util.UUID staffId);

    boolean existsByStaffIdAndDayOfWeek(java.util.UUID staffId, Integer dayOfWeek);

    Optional<StaffSchedule> findByStaffIdAndDayOfWeekAndStaffTenantId(java.util.UUID staffId, Integer dayOfWeek, Long tenantId);

    List<StaffSchedule> findByStaffTenantId(Long tenantId);

    long countByStaffTenantIdAndIsAvailableTrue(Long tenantId);

    long countByStaffTenantId(Long tenantId);
    Optional<StaffSchedule> findByStaffIdAndDayOfWeekAndStaff_TenantId(
            java.util.UUID staffId,
            Integer dayOfWeek,
            Long tenantId
    );

}