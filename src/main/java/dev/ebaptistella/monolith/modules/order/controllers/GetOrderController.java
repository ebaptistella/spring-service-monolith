package dev.ebaptistella.monolith.modules.order.controllers;

import dev.ebaptistella.monolith.modules.order.diplomat.jpa.OrderPersistence;
import dev.ebaptistella.monolith.modules.order.models.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetOrderController {

    private final OrderPersistence persistence;

    public Order getById(UUID id) {
        return persistence.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Order not found: " + id));
    }
}
