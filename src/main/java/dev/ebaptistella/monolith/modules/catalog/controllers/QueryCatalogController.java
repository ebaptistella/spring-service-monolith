package dev.ebaptistella.monolith.modules.catalog.controllers;

import dev.ebaptistella.monolith.modules.catalog.diplomat.jpa.CatalogPersistence;
import dev.ebaptistella.monolith.modules.catalog.models.Sku;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QueryCatalogController {

    private final CatalogPersistence jpa;

    public Optional<Sku> findById(UUID id) {
        return jpa.findSkuById(id);
    }

    public Sku getById(UUID id) {
        return findById(id)
                .orElseThrow(() -> new NoSuchElementException("SKU not found: " + id));
    }
}
