package dev.ebaptistella.monolith.modules.inventory.diplomat.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface StockMovementJpaRepository extends JpaRepository<StockMovementEntity, UUID> {

    Optional<StockMovementEntity> findByIdempotencyKey(UUID idempotencyKey);
}
