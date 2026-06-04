package dev.ebaptistella.monolith.modules.inventory.diplomat.jpa;

import dev.ebaptistella.monolith.modules.inventory.models.MovementType;
import dev.ebaptistella.monolith.modules.inventory.models.ReservationStatus;
import dev.ebaptistella.monolith.modules.inventory.models.StockLevel;
import dev.ebaptistella.monolith.modules.inventory.models.StockMovement;
import dev.ebaptistella.monolith.modules.inventory.models.StockReservation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InventoryPersistence {

    private final StockLevelJpaRepository stockLevelRepository;
    private final StockReservationJpaRepository reservationRepository;
    private final StockMovementJpaRepository movementRepository;

    public boolean hasReservationsForOrder(UUID orderId) {
        return reservationRepository.existsByOrderId(orderId);
    }

    public Optional<StockReservation> findReservationByIdempotencyKey(UUID idempotencyKey) {
        return reservationRepository.findByIdempotencyKey(idempotencyKey).map(StockReservationEntity::toModel);
    }

    public Optional<StockMovement> findMovementByIdempotencyKey(UUID idempotencyKey) {
        return movementRepository.findByIdempotencyKey(idempotencyKey).map(StockMovementEntity::toModel);
    }

    public StockLevel getOrCreateLevel(UUID skuId) {
        return stockLevelRepository
                .findById(skuId)
                .map(StockLevelEntity::toModel)
                .orElseGet(() -> new StockLevel(skuId, 0, 0, Instant.now()));
    }

    public Optional<StockLevel> findLevel(UUID skuId) {
        return stockLevelRepository.findById(skuId).map(StockLevelEntity::toModel);
    }

    public StockLevel saveLevel(StockLevel level) {
        return stockLevelRepository.save(StockLevelEntity.fromModel(level)).toModel();
    }

    public StockReservation saveReservation(StockReservation reservation) {
        return reservationRepository.save(StockReservationEntity.fromModel(reservation)).toModel();
    }

    public void saveMovement(StockMovement movement) {
        movementRepository.save(StockMovementEntity.fromModel(movement));
    }

    public List<StockReservation> findActiveReservationsByOrderId(UUID orderId) {
        return reservationRepository.findByOrderIdAndStatus(orderId, ReservationStatus.ACTIVE).stream()
                .map(StockReservationEntity::toModel)
                .toList();
    }
}
