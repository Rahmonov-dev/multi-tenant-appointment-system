package org.architect.multitenantappointmentsystem.repository;

import org.architect.multitenantappointmentsystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, java.util.UUID> {
    Optional<User> findByEmail(String email);

    Optional<User> findById(java.util.UUID userId);

    boolean existsByEmail(String email);
}
