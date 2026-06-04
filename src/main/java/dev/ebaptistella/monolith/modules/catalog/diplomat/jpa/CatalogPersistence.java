package dev.ebaptistella.monolith.modules.catalog.diplomat.jpa;

import dev.ebaptistella.monolith.modules.catalog.models.Product;
import dev.ebaptistella.monolith.modules.catalog.models.Sku;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CatalogPersistence {

    private final ProductJpaRepository productRepository;
    private final SkuJpaRepository skuRepository;

    public Sku saveProductAndSku(Product product, Sku sku) {
        productRepository.save(ProductEntity.fromModel(product));
        return skuRepository.save(SkuEntity.fromModel(sku)).toModel();
    }

    public Optional<Sku> findSkuById(UUID id) {
        return skuRepository.findById(id).map(SkuEntity::toModel);
    }

    public Optional<Sku> findSkuByIdempotencyKey(UUID idempotencyKey) {
        return skuRepository.findByIdempotencyKey(idempotencyKey).map(SkuEntity::toModel);
    }

    public Optional<Product> findProductById(UUID id) {
        return productRepository.findById(id).map(ProductEntity::toModel);
    }
}
