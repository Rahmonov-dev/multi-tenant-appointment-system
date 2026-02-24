package org.architect.multitenantappointmentsystem.repository;

import org.architect.multitenantappointmentsystem.entity.Tenant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    Optional<Tenant> findBySlug(String slug);
    boolean existsBySlug(String slug);

    @Query("SELECT t FROM Tenant t WHERE t.isActive = true AND " +
           "(LOWER(t.organizationName) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           " LOWER(t.address) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           " LOWER(CAST(t.businessType AS string)) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Tenant> searchActiveTenants(@Param("q") String q, Pageable pageable);
}
