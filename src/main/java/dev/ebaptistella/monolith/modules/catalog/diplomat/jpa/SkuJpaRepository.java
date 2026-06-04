package dev.ebaptistella.monolith.modules.catalog.diplomat.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface SkuJpaRepository extends JpaRepository<SkuEntity, UUID> {

    Optional<SkuEntity> findByIdempotencyKey(UUID idempotencyKey);
}
