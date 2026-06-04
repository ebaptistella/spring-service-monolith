package dev.ebaptistella.monolith.modules.order;

import dev.ebaptistella.monolith.modules.catalog.controllers.CreateSkuController;
import dev.ebaptistella.monolith.modules.catalog.models.CreateSkuInput;
import dev.ebaptistella.monolith.modules.customer.controllers.RegisterCustomerController;
import dev.ebaptistella.monolith.modules.customer.models.CustomerRegistrationInput;
import dev.ebaptistella.monolith.modules.order.controllers.PlaceOrderController;
import dev.ebaptistella.monolith.modules.order.diplomat.outbound.CatalogGateway;
import dev.ebaptistella.monolith.modules.order.diplomat.outbound.CustomerGateway;
import dev.ebaptistella.monolith.modules.order.diplomat.outbound.InventoryGateway;
import dev.ebaptistella.monolith.modules.order.models.PlaceOrderInput;
import dev.ebaptistella.monolith.modules.order.models.PlaceOrderLineInput;
import dev.ebaptistella.monolith.shared.idempotency.IdempotencyContext;
import dev.ebaptistella.monolith.shared.wire.in.events.OrderPlacedEvent;
import dev.ebaptistella.monolith.support.TestProfileIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.modulith.test.EnableScenarios;
import org.springframework.modulith.test.Scenario;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Tag("integration")
@EnableScenarios
class OrderModuleIT extends TestProfileIntegrationTest {

    @Autowired
    private PlaceOrderController placeOrderController;

    @Autowired
    private RegisterCustomerController registerCustomerController;

    @Autowired
    private CreateSkuController createSkuController;

    @MockBean
    private CustomerGateway customerGateway;

    @MockBean
    private CatalogGateway catalogGateway;

    @MockBean
    private InventoryGateway inventoryGateway;

    @BeforeEach
    void setUpGateways() {
        when(customerGateway.exists(any(UUID.class))).thenReturn(true);
        when(catalogGateway.isSkuActive(any(UUID.class))).thenReturn(true);
        when(catalogGateway.getListPrice(any(UUID.class))).thenReturn(Optional.of(new BigDecimal("25.00")));
        when(inventoryGateway.hasAvailabilityForLines(any())).thenReturn(true);
        when(inventoryGateway.available(any(UUID.class))).thenReturn(100);
    }

    @Test
    void placePublishesOrderPlacedEvent(Scenario scenario) {
        UUID customerId = IdempotencyContext.runWithRootKey(UUID.randomUUID(), () -> registerCustomerController
                .register(new CustomerRegistrationInput("order-modulith@example.com", "Order Modulith User"))
                .customer()
                .orElseThrow()
                .id());
        UUID skuId = IdempotencyContext.runWithRootKey(UUID.randomUUID(), () -> createSkuController
                .create(new CreateSkuInput("Modulith Product", "Test", "MOD-SKU-1", new BigDecimal("25.00")))
                .created()
                .orElseThrow()
                .sku()
                .id());
        PlaceOrderInput input = new PlaceOrderInput(customerId, List.of(new PlaceOrderLineInput(skuId, 2)));

        scenario.stimulate(() -> IdempotencyContext.runWithRootKey(
                        UUID.randomUUID(), () -> placeOrderController.place(input)))
                .andWaitForEventOfType(OrderPlacedEvent.class)
                .matchingMappedValue(OrderPlacedEvent::customerId, customerId)
                .toArriveAndVerify(event -> assertThat(event.totalAmount()).isEqualByComparingTo("50.00"));
    }
}
