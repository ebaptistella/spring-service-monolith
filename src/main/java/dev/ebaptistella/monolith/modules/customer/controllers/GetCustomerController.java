package dev.ebaptistella.monolith.modules.customer.controllers;

import dev.ebaptistella.monolith.modules.customer.diplomat.jpa.CustomerPersistence;
import dev.ebaptistella.monolith.modules.customer.models.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetCustomerController {

    private final CustomerPersistence jpa;

    public Customer getById(UUID id) {
        return jpa.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Customer not found: " + id));
    }
}
