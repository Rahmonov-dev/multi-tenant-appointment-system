package org.architect.multitenantappointmentsystem.repository;

import org.architect.multitenantappointmentsystem.entity.Employement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<Employement, Long> {

    boolean existsByNameAndTenantId(String name, Long tenantId);
    boolean existsByNameAndTenantIdAndIdNot(String name, Long tenantId, Long id);

    List<Employement> findByTenantId(Long tenantId);
    List<Employement> findByTenantIdAndIsActive(Long tenantId, Boolean isActive);
    Page<Employement> findByTenantId(Long tenantId, Pageable pageable);
    Page<Employement> findByTenantIdAndIsActive(Long tenantId, Boolean isActive, Pageable pageable);

    List<Employement> findByTenantIdOrderByDisplayOrder(Long tenantId);
    List<Employement> findByTenantIdAndIsActiveOrderByDisplayOrder(Long tenantId, Boolean isActive);

    @Query("SELECT s FROM Employement s WHERE s.tenant.id = :tenantId AND s.price BETWEEN :minPrice AND :maxPrice")
    List<Employement> findByPriceRange(@Param("tenantId") Long tenantId, @Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);
    @Query("SELECT s FROM Employement s WHERE s.tenant.id = :tenantId AND s.duration <= :maxDuration")
    List<Employement> findByMaxDuration(@Param("tenantId") Long tenantId, @Param("maxDuration") Integer maxDuration);

    List<Employement> findActiveServicesByStaffId(Long staffId);

    @Query("SELECT s FROM Employement s WHERE s.tenant.id = :tenantId AND " +
           "(LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Employement> searchByKeyword(@Param("tenantId") Long tenantId, @Param("keyword") String keyword);

    @Query("SELECT s FROM Employement s WHERE s.tenant.id = :tenantId AND s.isActive = true AND " +
           "(LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Employement> searchActiveByKeyword(@Param("tenantId") Long tenantId, @Param("keyword") String keyword);

    @Query(value = "SELECT s.* FROM services s " +
           "INNER JOIN appointments a ON s.id = a.service_id " +
           "WHERE s.tenant_id = :tenantId " +
           "GROUP BY s.id " +
           "ORDER BY COUNT(a.id) DESC " +
           "LIMIT :limit", nativeQuery = true)
    List<Employement> findPopularServices(@Param("tenantId") Long tenantId, Pageable pageable);

    List<Employement> findByIdInAndTenantId(List<Long> ids, Long tenantId);
    Optional<Employement> findByIdAndTenantId(Long id, Long tenantId);
}
