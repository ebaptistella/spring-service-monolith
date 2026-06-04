package dev.ebaptistella.monolith.modules.finance.diplomat.jpa;

import dev.ebaptistella.monolith.modules.finance.models.PaymentStatus;
import dev.ebaptistella.monolith.modules.finance.models.Transaction;
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

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class TransactionEntity {

    @Id
    private UUID id;

    @Column(name = "payment_intent_id", nullable = false)
    private UUID paymentIntentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PaymentStatus status;

    @Column(name = "gateway_reference", length = 128)
    private String gatewayReference;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private UUID idempotencyKey;

    static TransactionEntity fromModel(Transaction transaction) {
        TransactionEntity entity = new TransactionEntity();
        entity.id = transaction.id();
        entity.paymentIntentId = transaction.paymentIntentId();
        entity.status = transaction.status();
        entity.gatewayReference = transaction.gatewayReference();
        entity.createdAt = transaction.createdAt();
        entity.idempotencyKey = transaction.idempotencyKey();
        return entity;
    }

    Transaction toModel() {
        return new Transaction(id, paymentIntentId, status, gatewayReference, createdAt, idempotencyKey);
    }
}
