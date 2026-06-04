package dev.ebaptistella.monolith.modules.customer.diplomat.cache;

import dev.ebaptistella.monolith.modules.customer.models.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class CustomerCacheReader {

    @Cacheable(cacheNames = CustomerCacheNames.CUSTOMERS, key = "#id")
    public Customer loadById(UUID id, Supplier<Customer> loader) {
        return loader.get();
    }
}
