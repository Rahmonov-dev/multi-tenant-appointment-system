package org.architect.multitenantappointmentsystem.repository;

import org.architect.multitenantappointmentsystem.entity.Staff;
import org.architect.multitenantappointmentsystem.entity.StaffRole;
import org.architect.multitenantappointmentsystem.security.AuthUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {

    // Basic queries
    List<Staff> findByTenantId(Long tenantId);

    List<Staff> findByTenantIdAndIsActive(Long tenantId, Boolean isActive);

    Page<Staff> findByTenantId(Long tenantId, Pageable pageable);

    Page<Staff> findByTenantIdAndIsActive(Long tenantId, Boolean isActive, Pageable pageable);

    Optional<Staff> findByIdAndTenantId(Long id, Long tenantId);

    // Role-based queries
    List<Staff> findByTenantIdAndRole(Long tenantId, StaffRole role);

    List<Staff> findByTenantIdAndRoleAndIsActive(Long tenantId, StaffRole role, Boolean isActive);

    // User-related queries
    Optional<Staff> findByUserIdAndTenantId(Long userId, Long tenantId);

    boolean existsByUserIdAndTenantId(Long userId, Long tenantId);

    // Employement-related queries
    @Query("SELECT s FROM Staff s JOIN s.employements srv WHERE srv.id = :serviceId AND s.isActive = true")
    List<Staff> findActiveStaffByServiceId(@Param("serviceId") Long serviceId);

    @Query("SELECT s FROM Staff s JOIN s.employements srv WHERE srv.id = :serviceId AND s.tenant.id = :tenantId AND s.isActive = true")
    List<Staff> findActiveStaffByServiceIdAndTenantId(@Param("serviceId") Long serviceId, @Param("tenantId") Long tenantId);

    // Schedule-related queries
    @Query("SELECT DISTINCT s FROM Staff s LEFT JOIN FETCH s.schedules WHERE s.tenant.id = :tenantId AND s.isActive = true")
    List<Staff> findActiveStaffWithSchedulesByTenantId(@Param("tenantId") Long tenantId);

    // Count queries
    long countByTenantId(Long tenantId);

    long countByTenantIdAndIsActive(Long tenantId, Boolean isActive);

    long countByTenantIdAndRole(Long tenantId, StaffRole role);

    List<Staff> findByUserId(Long id);

    Optional<Object> findByTenantIdAndUserId(Long tenantId, Long currentUserId);
}