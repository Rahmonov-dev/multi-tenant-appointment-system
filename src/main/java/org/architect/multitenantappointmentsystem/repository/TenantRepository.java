package org.architect.multitenantappointmentsystem.repository;

import org.architect.multitenantappointmentsystem.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant,Long> {
    Optional<Tenant> findByTenantKey(String tenantKey);
    Optional<Tenant> findBySlug(String slug);
    boolean existsBySlug(String slug);
    boolean existsByTenantKey(String tenantKey);
}
