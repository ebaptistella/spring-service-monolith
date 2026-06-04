package dev.ebaptistella.monolith.modules.finance.diplomat.jpa;

import dev.ebaptistella.monolith.modules.finance.models.PaymentIntent;
import dev.ebaptistella.monolith.modules.finance.models.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_intents")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class PaymentIntentEntity {

    @Id
    private UUID id;

    @Column(name = "order_id", nullable = false, unique = true)
    private UUID orderId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PaymentStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private UUID idempotencyKey;

    @Column(name = "root_idempotency_key", nullable = false)
    private UUID rootIdempotencyKey;

    static PaymentIntentEntity fromModel(PaymentIntent intent) {
        PaymentIntentEntity entity = new PaymentIntentEntity();
        entity.id = intent.id();
        entity.orderId = intent.orderId();
        entity.amount = intent.amount();
        entity.currency = intent.currency();
        entity.status = intent.status();
        entity.createdAt = intent.createdAt();
        entity.updatedAt = intent.updatedAt();
        entity.idempotencyKey = intent.idempotencyKey();
        entity.rootIdempotencyKey = intent.rootIdempotencyKey();
        return entity;
    }

    PaymentIntent toModel() {
        return new PaymentIntent(
                id, orderId, amount, currency, status, createdAt, updatedAt, idempotencyKey, rootIdempotencyKey);
    }
}
