package dev.ebaptistella.monolith.modules.inventory.diplomat.jpa;

import dev.ebaptistella.monolith.modules.inventory.models.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface StockReservationJpaRepository extends JpaRepository<StockReservationEntity, UUID> {

    Optional<StockReservationEntity> findByIdempotencyKey(UUID idempotencyKey);

    boolean existsByOrderId(UUID orderId);

    List<StockReservationEntity> findByOrderIdAndStatus(UUID orderId, ReservationStatus status);
}
