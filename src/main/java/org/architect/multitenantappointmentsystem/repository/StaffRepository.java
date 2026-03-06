package org.architect.multitenantappointmentsystem.repository;

import org.architect.multitenantappointmentsystem.entity.Staff;
import org.architect.multitenantappointmentsystem.entity.StaffRole;
import org.architect.multitenantappointmentsystem.security.AuthUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StaffRepository extends JpaRepository<Staff, java.util.UUID> {

    // Basic queries
    @EntityGraph(attributePaths = {"user", "tenant", "employements", "schedules"})
    List<Staff> findByTenantId(UUID tenantId);

    @EntityGraph(attributePaths = {"user", "tenant", "employements", "schedules"})
    List<Staff> findByTenantIdAndIsActive(UUID tenantId, Boolean isActive);

    // Page metodlarda "schedules" qo'shilmaydi — kolleksiya bilan Page Hibernate da in-memory pagination qiladi
    @EntityGraph(attributePaths = {"user", "tenant", "employements"})
    Page<Staff> findByTenantId(UUID tenantId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "tenant", "employements"})
    Page<Staff> findByTenantIdAndIsActive(UUID tenantId, Boolean isActive, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "tenant", "employements", "schedules"})
    Optional<Staff> findByIdAndTenantId(UUID id, UUID tenantId);

    // Role-based queries
    @EntityGraph(attributePaths = {"user", "tenant", "employements", "schedules"})
    List<Staff> findByTenantIdAndRole(UUID tenantId, StaffRole role);

    List<Staff> findByTenantIdAndRoleAndIsActive(UUID tenantId, StaffRole role, Boolean isActive);

    // User-related queries
    Optional<Staff> findByUserIdAndTenantId(UUID userId, UUID tenantId);

    boolean existsByUserIdAndTenantId(UUID userId, UUID tenantId);

    // Employement-related queries
    @Query("SELECT s FROM Staff s JOIN s.employements srv WHERE srv.id = :serviceId AND s.isActive = true")
    List<Staff> findActiveStaffByServiceId(@Param("serviceId") UUID serviceId);

    @Query("SELECT s FROM Staff s JOIN s.employements srv WHERE srv.id = :serviceId AND s.tenant.id = :tenantId AND s.isActive = true")
    List<Staff> findActiveStaffByServiceIdAndTenantId(@Param("serviceId") UUID serviceId,
                                                      @Param("tenantId") UUID tenantId);

    // Schedule-related queries
    @Query("SELECT DISTINCT s FROM Staff s LEFT JOIN FETCH s.schedules WHERE s.tenant.id = :tenantId AND s.isActive = true")
    List<Staff> findActiveStaffWithSchedulesByTenantId(@Param("tenantId") UUID tenantId);

    // Count queries
    long countByTenantId(UUID tenantId);

    long countByTenantIdAndIsActive(UUID tenantId, Boolean isActive);

    long countByTenantIdAndRole(UUID tenantId, StaffRole role);

    List<Staff> findByUserId(UUID id);

    @EntityGraph(attributePaths = {"user", "tenant", "employements", "schedules"})
    Optional<Staff> findByTenantIdAndUserId(UUID tenantId, UUID userId);

}