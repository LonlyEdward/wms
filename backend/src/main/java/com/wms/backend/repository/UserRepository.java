package com.wms.backend.repository;

import com.wms.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByIdAndBusinessId(UUID id, UUID businessId);

    boolean existsByEmail(String email);
}