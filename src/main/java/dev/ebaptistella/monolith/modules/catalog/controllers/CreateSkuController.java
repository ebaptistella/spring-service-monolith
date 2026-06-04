package dev.ebaptistella.monolith.modules.catalog.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ebaptistella.monolith.modules.catalog.diplomat.jpa.CatalogPersistence;
import dev.ebaptistella.monolith.modules.catalog.logic.CatalogRules;
import dev.ebaptistella.monolith.modules.catalog.logic.CreateSkuResult;
import dev.ebaptistella.monolith.modules.catalog.logic.CreatedSku;
import dev.ebaptistella.monolith.modules.catalog.models.CreateSkuInput;
import dev.ebaptistella.monolith.modules.catalog.models.Product;
import dev.ebaptistella.monolith.modules.catalog.models.Sku;
import dev.ebaptistella.monolith.shared.idempotency.IdempotencyContext;
import dev.ebaptistella.monolith.shared.idempotency.IdempotencyKeys;
import dev.ebaptistella.monolith.shared.idempotency.IdempotencyPayloadFingerprint;
import dev.ebaptistella.monolith.shared.idempotency.IdempotencyReplay;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateSkuController {

    private static final String PRODUCT_SCOPE = "catalog.product.create";
    private static final String SKU_SCOPE = "catalog.sku.create";

    private final CatalogPersistence jpa;
    private final ObjectMapper objectMapper;

    @Transactional
    public CreateSkuResult create(CreateSkuInput input) {
        UUID rootKey = IdempotencyContext.requireRootKey();
        UUID skuEntityKey = IdempotencyKeys.derive(rootKey, SKU_SCOPE);
        String fingerprint = IdempotencyPayloadFingerprint.sha256(input, objectMapper);

        return IdempotencyReplay.resolve(
                jpa.findSkuByIdempotencyKey(skuEntityKey),
                fingerprint,
                Sku::requestFingerprint,
                sku -> replaySku(sku),
                () -> createNew(input, rootKey, skuEntityKey, fingerprint));
    }

    private CreateSkuResult replaySku(Sku sku) {
        Product product = jpa.findProductById(sku.productId())
                .orElseThrow(() -> new IllegalStateException("Product not found for SKU: " + sku.id()));
        return CreateSkuResult.replay(new CreatedSku(product, sku));
    }

    private CreateSkuResult createNew(CreateSkuInput input, UUID rootKey, UUID skuEntityKey, String fingerprint) {
        CreateSkuResult result = CatalogRules.createSku(input);
        if (result.rejected()) {
            return result;
        }

        CreatedSku created = result.created().orElseThrow();
        UUID productEntityKey = IdempotencyKeys.derive(rootKey, PRODUCT_SCOPE);
        Product product = new Product(
                created.product().id(),
                created.product().name(),
                created.product().description(),
                created.product().active(),
                created.product().createdAt(),
                productEntityKey,
                rootKey,
                fingerprint);
        Sku sku = new Sku(
                created.sku().id(),
                created.sku().productId(),
                created.sku().code(),
                created.sku().listPrice(),
                created.sku().active(),
                created.sku().createdAt(),
                skuEntityKey,
                rootKey,
                fingerprint);
        Sku saved = jpa.saveProductAndSku(product, sku);
        return CreateSkuResult.success(new CreatedSku(product, saved));
    }
}
