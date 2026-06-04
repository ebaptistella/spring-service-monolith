package dev.ebaptistella.monolith.modules.order.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ebaptistella.monolith.modules.order.diplomat.jpa.OrderPersistence;
import dev.ebaptistella.monolith.modules.order.diplomat.outbound.CatalogGateway;
import dev.ebaptistella.monolith.modules.order.diplomat.outbound.CustomerGateway;
import dev.ebaptistella.monolith.modules.order.diplomat.outbound.InventoryGateway;
import dev.ebaptistella.monolith.modules.order.diplomat.producer.OrderEventProducer;
import dev.ebaptistella.monolith.modules.order.logic.OrderRules;
import dev.ebaptistella.monolith.modules.order.logic.PlaceOrderResult;
import dev.ebaptistella.monolith.modules.order.models.Order;
import dev.ebaptistella.monolith.modules.order.models.PlaceOrderInput;
import dev.ebaptistella.monolith.modules.order.models.PlaceOrderLineInput;
import dev.ebaptistella.monolith.modules.order.models.PlaceOrderResolvedLine;
import dev.ebaptistella.monolith.shared.contracts.inventory.StockLineRequest;
import dev.ebaptistella.monolith.shared.idempotency.IdempotencyContext;
import dev.ebaptistella.monolith.shared.idempotency.IdempotencyKeys;
import dev.ebaptistella.monolith.shared.idempotency.IdempotencyPayloadFingerprint;
import dev.ebaptistella.monolith.shared.idempotency.IdempotencyReplay;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlaceOrderController {

    private final OrderPersistence persistence;
    private final CustomerGateway customerGateway;
    private final CatalogGateway catalogGateway;
    private final InventoryGateway inventoryGateway;
    private final OrderEventProducer producer;
    private final ObjectMapper objectMapper;

    @Transactional
    public PlaceOrderResult place(PlaceOrderInput input) {
        UUID rootKey = IdempotencyContext.requireRootKey();
        UUID idempotencyKey = IdempotencyKeys.derive(rootKey, "order", "place");
        String fingerprint = IdempotencyPayloadFingerprint.sha256(input, objectMapper);

        Optional<Order> existing = persistence.findByIdempotencyKey(idempotencyKey);
        return IdempotencyReplay.resolve(
                existing,
                fingerprint,
                Order::requestFingerprint,
                PlaceOrderResult::replay,
                () -> placeNew(input, idempotencyKey, rootKey, fingerprint));
    }

    private PlaceOrderResult placeNew(
            PlaceOrderInput input, UUID idempotencyKey, UUID rootKey, String fingerprint) {
        BigDecimal confirmedTotal = persistence.sumConfirmedTotal(input.customerId());
        boolean customerExists = customerGateway.exists(input.customerId());

        List<PlaceOrderResolvedLine> resolvedLines = new ArrayList<>();
        if (input.lines() != null) {
            for (PlaceOrderLineInput lineInput : input.lines()) {
                if (!catalogGateway.isSkuActive(lineInput.skuId())) {
                    return PlaceOrderResult.rejected("SKU is not active: " + lineInput.skuId());
                }

                Optional<BigDecimal> listPrice = catalogGateway.getListPrice(lineInput.skuId());
                if (listPrice.isEmpty()) {
                    return PlaceOrderResult.rejected("SKU has no list price: " + lineInput.skuId());
                }

                resolvedLines.add(new PlaceOrderResolvedLine(
                        lineInput.skuId(), lineInput.quantity(), listPrice.orElseThrow()));
            }
        }

        List<StockLineRequest> stockLines = input.lines() == null
                ? List.of()
                : input.lines().stream()
                        .map(line -> new StockLineRequest(line.skuId(), line.quantity()))
                        .toList();
        boolean stockAvailable = stockLines.isEmpty() || inventoryGateway.hasAvailabilityForLines(stockLines);

        PlaceOrderResult result = OrderRules.validatePlaceOrder(
                input, confirmedTotal, customerExists, resolvedLines, stockAvailable);

        if (result.rejected()) {
            return result;
        }

        Order draft = result.order().orElseThrow();
        Order withKeys = new Order(
                draft.id(),
                draft.customerId(),
                draft.status(),
                draft.totalAmount(),
                draft.currency(),
                draft.lines(),
                draft.createdAt(),
                draft.updatedAt(),
                idempotencyKey,
                rootKey,
                fingerprint);

        Order saved = persistence.save(withKeys);
        producer.publishOrderPlaced(saved);
        return PlaceOrderResult.success(saved);
    }
}
