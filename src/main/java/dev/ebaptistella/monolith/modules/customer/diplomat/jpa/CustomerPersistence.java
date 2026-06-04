package dev.ebaptistella.monolith.modules.customer.diplomat.jpa;

import dev.ebaptistella.monolith.modules.customer.diplomat.cache.CustomerCacheReader;
import dev.ebaptistella.monolith.modules.customer.diplomat.cache.CustomerCacheNames;
import dev.ebaptistella.monolith.modules.customer.models.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CustomerPersistence {

    private final CustomerJpaRepository repository;
    private final CustomerCacheReader cacheReader;

    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }

    @CachePut(cacheNames = CustomerCacheNames.CUSTOMERS, key = "#customer.id()")
    public Customer save(Customer customer) {
        return repository.save(CustomerEntity.fromModel(customer)).toModel();
    }

    public Optional<Customer> findByIdempotencyKey(UUID idempotencyKey) {
        return repository.findByIdempotencyKey(idempotencyKey).map(CustomerEntity::toModel);
    }

    public Optional<Customer> findById(UUID id) {
        if (!repository.existsById(id)) {
            return Optional.empty();
        }
        return Optional.of(cacheReader.loadById(id, () -> loadUncachedById(id)));
    }

    private Customer loadUncachedById(UUID id) {
        return repository.findById(id)
                .map(CustomerEntity::toModel)
                .orElseThrow(() -> new NoSuchElementException("Customer not found: " + id));
    }
}
