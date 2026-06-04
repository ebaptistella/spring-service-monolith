package dev.ebaptistella.monolith.modules.order.diplomat.http_server;

import dev.ebaptistella.monolith.modules.order.adapters.OrderAdapter;
import dev.ebaptistella.monolith.modules.order.controllers.GetOrderController;
import dev.ebaptistella.monolith.modules.order.controllers.PlaceOrderController;
import dev.ebaptistella.monolith.modules.order.logic.PlaceOrderResult;
import dev.ebaptistella.monolith.modules.order.models.Order;
import dev.ebaptistella.monolith.modules.order.wire.in.PlaceOrderRequest;
import dev.ebaptistella.monolith.modules.order.wire.out.OrderResponse;
import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Order placement and lookup")
@RequiredArgsConstructor
public class OrderHttpServer {

    private final PlaceOrderController placeOrderController;
    private final GetOrderController getOrderController;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Observed(name = "http.server", contextualName = "order place")
    @Operation(summary = "Place order", description = "Validates catalog, inventory, and spend limit, then places the order")
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
        PlaceOrderResult result = placeOrderController.place(OrderAdapter.wireInToPlaceOrderInput(request));

        if (result.rejected()) {
            String error = result.error().orElse("Order rejected");
            HttpStatus status = HttpStatus.BAD_REQUEST;
            if (error.startsWith("Insufficient stock")) {
                status = HttpStatus.CONFLICT;
            } else if (error.contains("spend limit")) {
                status = HttpStatus.UNPROCESSABLE_ENTITY;
            }
            throw new ResponseStatusException(status, error);
        }

        Order order = result.order().orElseThrow();
        OrderResponse body = OrderAdapter.modelToWireOut(order);
        if (result.replay()) {
            return ResponseEntity.ok(body);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @Observed(name = "http.server", contextualName = "order get")
    @Operation(summary = "Get order by id", description = "Returns order details including line items")
    public OrderResponse getOrder(@PathVariable UUID id) {
        try {
            Order order = getOrderController.getById(id);
            return OrderAdapter.modelToWireOut(order);
        } catch (NoSuchElementException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }
}
