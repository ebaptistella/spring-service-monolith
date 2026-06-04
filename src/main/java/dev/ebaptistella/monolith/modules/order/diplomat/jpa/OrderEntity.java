package dev.ebaptistella.monolith.modules.order.diplomat.jpa;

import dev.ebaptistella.monolith.modules.order.models.Order;
import dev.ebaptistella.monolith.modules.order.models.OrderStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.annotations.BatchSize;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class OrderEntity {

    @Id
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private OrderStatus status;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private UUID idempotencyKey;

    @Column(name = "root_idempotency_key", nullable = false)
    private UUID rootIdempotencyKey;

    @Column(name = "request_fingerprint", nullable = false, length = 64)
    private String requestFingerprint;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @BatchSize(size = 16)
    private List<OrderLineEntity> lines = new ArrayList<>();

    static OrderEntity fromModel(Order order) {
        OrderEntity entity = new OrderEntity();
        entity.id = order.id();
        entity.customerId = order.customerId();
        entity.status = order.status();
        entity.totalAmount = order.totalAmount();
        entity.currency = order.currency();
        entity.createdAt = order.createdAt();
        entity.updatedAt = order.updatedAt();
        entity.idempotencyKey = order.idempotencyKey();
        entity.rootIdempotencyKey = order.rootIdempotencyKey();
        entity.requestFingerprint = order.requestFingerprint();
        entity.lines.clear();
        for (var line : order.lines()) {
            entity.lines.add(OrderLineEntity.fromModel(line, entity));
        }
        return entity;
    }

    Order toModel() {
        return new Order(
                id,
                customerId,
                status,
                totalAmount,
                currency,
                lines.stream().map(OrderLineEntity::toModel).toList(),
                createdAt,
                updatedAt,
                idempotencyKey,
                rootIdempotencyKey,
                requestFingerprint);
    }
}
