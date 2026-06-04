package dev.ebaptistella.monolith.modules.inventory.diplomat.producer;

import dev.ebaptistella.monolith.modules.inventory.adapters.StockEventAdapter;
import dev.ebaptistella.monolith.shared.wire.in.events.StockReservationFailedEvent;
import dev.ebaptistella.monolith.shared.wire.in.events.StockReservedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InventoryEventProducer {

    private final ApplicationEventPublisher events;

    public void publishStockReserved(UUID idempotencyKey, UUID orderId, BigDecimal totalAmount) {
        StockReservedEvent wire = StockEventAdapter.toStockReserved(idempotencyKey, orderId, totalAmount);
        events.publishEvent(wire);
    }

    public void publishStockReservationFailed(UUID idempotencyKey, UUID orderId, String reason) {
        StockReservationFailedEvent wire =
                StockEventAdapter.toStockReservationFailed(idempotencyKey, orderId, reason);
        events.publishEvent(wire);
    }
}
