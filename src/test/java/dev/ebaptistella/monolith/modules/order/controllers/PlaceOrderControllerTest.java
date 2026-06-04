package dev.ebaptistella.monolith.modules.order.controllers;

import dev.ebaptistella.monolith.modules.order.diplomat.jpa.OrderPersistence;
import dev.ebaptistella.monolith.modules.order.diplomat.outbound.CatalogGateway;
import dev.ebaptistella.monolith.modules.order.diplomat.outbound.CustomerGateway;
import dev.ebaptistella.monolith.modules.order.diplomat.outbound.InventoryGateway;
import dev.ebaptistella.monolith.modules.order.diplomat.producer.OrderEventProducer;
import dev.ebaptistella.monolith.modules.order.logic.PlaceOrderResult;
import dev.ebaptistella.monolith.modules.order.models.Order;
import dev.ebaptistella.monolith.modules.order.models.OrderStatus;
import dev.ebaptistella.monolith.modules.order.models.PlaceOrderInput;
import dev.ebaptistella.monolith.modules.order.models.PlaceOrderLineInput;
import dev.ebaptistella.monolith.shared.idempotency.IdempotencyContext;
import dev.ebaptistella.monolith.shared.idempotency.IdempotencyKeys;
import dev.ebaptistella.monolith.shared.idempotency.IdempotencyPayloadFingerprint;
import dev.ebaptistella.monolith.support.IdempotencyTestSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class PlaceOrderControllerTest {

    @Mock
    private OrderPersistence persistence;

    @Mock
    private CustomerGateway customerGateway;

    @Mock
    private CatalogGateway catalogGateway;

    @Mock
    private InventoryGateway inventoryGateway;

    @Mock
    private OrderEventProducer producer;

    private PlaceOrderController controller;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        controller = new PlaceOrderController(
                persistence, customerGateway, catalogGateway, inventoryGateway, producer, objectMapper);
    }

    @Test
    void place_rejectsInvalidOrderWithoutPersisting() {
        IdempotencyTestSupport.runWithRootKey(() -> {
            UUID customerId = UUID.randomUUID();
            PlaceOrderInput input = new PlaceOrderInput(
                    customerId, List.of(new PlaceOrderLineInput(UUID.randomUUID(), 1)));

            when(persistence.findByIdempotencyKey(any())).thenReturn(Optional.empty());
            when(persistence.sumConfirmedTotal(customerId)).thenReturn(BigDecimal.ZERO);
            when(customerGateway.exists(customerId)).thenReturn(false);

            PlaceOrderResult result = controller.place(input);

            assertThat(result.rejected()).isTrue();
            verify(persistence, never()).save(any());
            verify(producer, never()).publishOrderPlaced(any());
        });
    }

    @Test
    void place_persistsAndPublishesWhenValidationPasses() {
        IdempotencyTestSupport.runWithRootKey(() -> {
            UUID customerId = UUID.randomUUID();
            UUID skuId = UUID.randomUUID();
            PlaceOrderInput input = new PlaceOrderInput(
                    customerId, List.of(new PlaceOrderLineInput(skuId, 1)));

            when(persistence.findByIdempotencyKey(any())).thenReturn(Optional.empty());
            when(persistence.sumConfirmedTotal(customerId)).thenReturn(BigDecimal.ZERO);
            when(customerGateway.exists(customerId)).thenReturn(true);
            when(catalogGateway.isSkuActive(skuId)).thenReturn(true);
            when(catalogGateway.getListPrice(skuId)).thenReturn(Optional.of(new BigDecimal("25.00")));
            when(inventoryGateway.hasAvailabilityForLines(anyList())).thenReturn(true);
            when(persistence.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

            PlaceOrderResult result = controller.place(input);

            assertThat(result.rejected()).isFalse();
            assertThat(result.order()).map(Order::status).contains(OrderStatus.PLACED);
            verify(persistence).save(any(Order.class));
            verify(producer).publishOrderPlaced(any(Order.class));
        });
    }

    @Test
    void place_replaysExistingOrderWithoutPublishing() {
        UUID rootKey = UUID.randomUUID();
        IdempotencyContext.runWithRootKey(rootKey, () -> {
            UUID customerId = UUID.randomUUID();
            UUID skuId = UUID.randomUUID();
            PlaceOrderInput input = new PlaceOrderInput(
                    customerId, List.of(new PlaceOrderLineInput(skuId, 1)));
            UUID idempotencyKey = IdempotencyKeys.derive(rootKey, "order", "place");
            String fingerprint = IdempotencyPayloadFingerprint.sha256(input, objectMapper);
            Order existing = new Order(
                    UUID.randomUUID(),
                    customerId,
                    OrderStatus.PLACED,
                    new BigDecimal("25.00"),
                    "BRL",
                    List.of(),
                    Instant.now(),
                    Instant.now(),
                    idempotencyKey,
                    rootKey,
                    fingerprint);
            when(persistence.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.of(existing));

            PlaceOrderResult result = controller.place(input);

            assertThat(result.replay()).isTrue();
            assertThat(result.order()).contains(existing);
            verify(persistence, never()).save(any());
            verify(producer, never()).publishOrderPlaced(any());
        });
    }
}
