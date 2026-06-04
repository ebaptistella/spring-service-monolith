package dev.ebaptistella.monolith.modules.inventory.controllers;

import dev.ebaptistella.monolith.modules.inventory.diplomat.jpa.InventoryPersistence;
import dev.ebaptistella.monolith.modules.inventory.logic.StockReservationResult;
import dev.ebaptistella.monolith.modules.inventory.models.ReservationStatus;
import dev.ebaptistella.monolith.modules.inventory.models.StockLevel;
import dev.ebaptistella.monolith.modules.inventory.models.StockReservation;
import dev.ebaptistella.monolith.modules.inventory.models.StockReserveInput;
import dev.ebaptistella.monolith.modules.inventory.models.StockReserveLine;
import dev.ebaptistella.monolith.shared.idempotency.IdempotencyKeys;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class ReserveStockControllerTest {

    @Mock
    private InventoryPersistence persistence;

    @InjectMocks
    private ReserveStockController controller;

    @Test
    void reserve_isIdempotentWhenReservationAlreadyExists() {
        UUID root = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID skuId = UUID.randomUUID();
        UUID reservationKey = IdempotencyKeys.derive(root, "inventory", "reserve", orderId.toString(), skuId.toString());
        when(persistence.findReservationByIdempotencyKey(reservationKey))
                .thenReturn(Optional.of(new StockReservation(
                        UUID.randomUUID(),
                        orderId,
                        skuId,
                        1,
                        ReservationStatus.ACTIVE,
                        Instant.now(),
                        reservationKey)));

        StockReservationResult result = controller.reserve(
                new StockReserveInput(root, orderId, List.of(new StockReserveLine(skuId, 1))));

        assertThat(result.idempotent()).isTrue();
        assertThat(result.failed()).isFalse();
        verify(persistence, never()).saveLevel(any());
        verify(persistence, never()).saveReservation(any());
    }

    @Test
    void reserve_failsWhenInsufficientStock() {
        UUID root = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID skuId = UUID.randomUUID();
        when(persistence.findReservationByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(persistence.getOrCreateLevel(skuId))
                .thenReturn(new StockLevel(skuId, 5, 0, Instant.now()));

        StockReservationResult result = controller.reserve(
                new StockReserveInput(root, orderId, List.of(new StockReserveLine(skuId, 10))));

        assertThat(result.failed()).isTrue();
        assertThat(result.failureReason()).hasValueSatisfying(reason -> assertThat(reason).contains("Insufficient stock"));
        verify(persistence, never()).saveReservation(any());
    }

    @Test
    void reserve_persistsReservationsWhenStockIsAvailable() {
        UUID root = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID skuId = UUID.randomUUID();
        StockLevel level = new StockLevel(skuId, 20, 0, Instant.now());
        when(persistence.findReservationByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(persistence.getOrCreateLevel(skuId)).thenReturn(level);
        when(persistence.saveLevel(any())).thenAnswer(invocation -> invocation.getArgument(0));

        StockReservationResult result = controller.reserve(
                new StockReserveInput(root, orderId, List.of(new StockReserveLine(skuId, 3))));

        assertThat(result.failed()).isFalse();
        assertThat(result.idempotent()).isFalse();
        verify(persistence).saveReservation(any());
        verify(persistence).saveMovement(any());
    }
}
