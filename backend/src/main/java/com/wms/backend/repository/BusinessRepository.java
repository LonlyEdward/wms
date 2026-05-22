package com.wms.backend.repository;

import com.wms.backend.entity.Business;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BusinessRepository extends JpaRepository<Business, UUID> {

    Optional<Business> findFirstByIsActiveTrue();

    Optional<Business> findBySlug(String slug);
}