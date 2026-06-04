package dev.ebaptistella.monolith.modules.inventory.diplomat.http_server;

import dev.ebaptistella.monolith.modules.inventory.adapters.InventoryAdapter;
import dev.ebaptistella.monolith.modules.inventory.controllers.AdjustStockController;
import dev.ebaptistella.monolith.modules.inventory.controllers.QueryStockController;
import dev.ebaptistella.monolith.modules.inventory.models.StockLevel;
import dev.ebaptistella.monolith.modules.inventory.wire.in.AdjustStockRequest;
import dev.ebaptistella.monolith.modules.inventory.wire.out.StockAvailabilityResponse;
import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory")
@Tag(name = "Inventory", description = "Stock levels and availability")
@RequiredArgsConstructor
public class InventoryHttpServer {

    private final AdjustStockController adjustStockController;
    private final QueryStockController queryStockController;

    @PostMapping("/adjustments")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('USER')")
    @Observed(name = "http.server", contextualName = "inventory adjustStock")
    @Operation(summary = "Adjust stock", description = "Applies a delta to on-hand quantity for a SKU")
    public void adjustStock(@Valid @RequestBody AdjustStockRequest request) {
        try {
            adjustStockController.adjust(InventoryAdapter.wireInToAdjustInput(request));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @GetMapping("/skus/{skuId}/availability")
    @PreAuthorize("hasRole('USER')")
    @Observed(name = "http.server", contextualName = "inventory getAvailability")
    @Operation(summary = "Get stock availability", description = "Returns on-hand, reserved, and available quantities")
    public StockAvailabilityResponse getAvailability(@PathVariable UUID skuId) {
        StockLevel level = queryStockController.getLevel(skuId);
        return InventoryAdapter.modelToWireOut(level);
    }
}
