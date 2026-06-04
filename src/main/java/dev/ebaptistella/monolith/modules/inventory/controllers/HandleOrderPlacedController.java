package dev.ebaptistella.monolith.modules.inventory.controllers;

import dev.ebaptistella.monolith.modules.inventory.diplomat.producer.InventoryEventProducer;
import dev.ebaptistella.monolith.modules.inventory.logic.StockReservationResult;
import dev.ebaptistella.monolith.modules.inventory.models.OrderPlacedInventoryInput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HandleOrderPlacedController {

    private final ReserveStockController reserveStockController;
    private final InventoryEventProducer producer;

    @Transactional
    public void handle(OrderPlacedInventoryInput input) {
        StockReservationResult result = reserveStockController.reserve(input.toReserveInput());

        if (result.failed()) {
            producer.publishStockReservationFailed(
                    input.eventIdempotencyKey(),
                    input.orderId(),
                    result.failureReason().orElse("Reservation failed"));
            return;
        }

        producer.publishStockReserved(input.eventIdempotencyKey(), input.orderId(), input.totalAmount());
    }
}
