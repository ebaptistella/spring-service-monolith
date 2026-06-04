package dev.ebaptistella.monolith.modules.order.diplomat.jpa;

import dev.ebaptistella.monolith.modules.order.models.Order;
import dev.ebaptistella.monolith.modules.order.models.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderPersistence {

    private final OrderJpaRepository repository;

    public Order save(Order order) {
        return repository.save(OrderEntity.fromModel(order)).toModel();
    }

    public Optional<Order> findById(UUID id) {
        return repository.findById(id).map(OrderEntity::toModel);
    }

    public Optional<Order> findByIdempotencyKey(UUID idempotencyKey) {
        return repository.findByIdempotencyKey(idempotencyKey).map(OrderEntity::toModel);
    }

    public BigDecimal sumConfirmedTotal(UUID customerId) {
        return repository.sumTotalAmountByCustomerIdAndStatus(customerId, OrderStatus.CONFIRMED);
    }
}
