package dev.ebaptistella.monolith.modules.catalog.diplomat.http_server;

import dev.ebaptistella.monolith.modules.catalog.adapters.CatalogAdapter;
import dev.ebaptistella.monolith.modules.catalog.controllers.CreateSkuController;
import dev.ebaptistella.monolith.modules.catalog.controllers.QueryCatalogController;
import dev.ebaptistella.monolith.modules.catalog.logic.CreateSkuResult;
import dev.ebaptistella.monolith.modules.catalog.models.Sku;
import dev.ebaptistella.monolith.modules.catalog.wire.in.CreateSkuRequest;
import dev.ebaptistella.monolith.modules.catalog.wire.out.SkuResponse;
import dev.ebaptistella.monolith.shared.idempotency.IdempotentHttpOutcome;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/catalog/skus")
@Tag(name = "Catalog", description = "Product catalog and SKU management")
@RequiredArgsConstructor
public class CatalogHttpServer {

    private final CreateSkuController createSkuController;
    private final QueryCatalogController queryCatalogController;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Observed(name = "http.server", contextualName = "catalog createSku")
    @Operation(summary = "Create SKU", description = "Creates a product and its SKU")
    public ResponseEntity<SkuResponse> createSku(@Valid @RequestBody CreateSkuRequest request) {
        CreateSkuResult result = createSkuController.create(
                CatalogAdapter.wireInToCreateSkuInput(request));

        if (result.rejected()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    result.error().orElse("SKU creation rejected"));
        }

        Sku sku = result.created()
                .orElseThrow()
                .sku();
        SkuResponse body = CatalogAdapter.modelToWireOut(sku);
        IdempotentHttpOutcome<SkuResponse> outcome = result.replay()
                ? IdempotentHttpOutcome.replay(body)
                : IdempotentHttpOutcome.created(body);
        return ResponseEntity.status(outcome.replay() ? HttpStatus.OK : HttpStatus.CREATED).body(outcome.body());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @Observed(name = "http.server", contextualName = "catalog getSku")
    @Operation(summary = "Get SKU by id", description = "Loads SKU from DB")
    public SkuResponse getSku(@PathVariable UUID id) {
        Sku sku = queryCatalogController.getById(id);
        return CatalogAdapter.modelToWireOut(sku);
    }
}
