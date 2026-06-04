package dev.ebaptistella.monolith.modules.finance.diplomat.jpa;

import dev.ebaptistella.monolith.modules.finance.models.PaymentIntent;
import dev.ebaptistella.monolith.modules.finance.models.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FinancePersistence {

    private final PaymentIntentJpaRepository paymentIntentRepository;
    private final TransactionJpaRepository transactionRepository;

    public Optional<PaymentIntent> findIntentByOrderId(UUID orderId) {
        return paymentIntentRepository.findByOrderId(orderId).map(PaymentIntentEntity::toModel);
    }

    public Optional<PaymentIntent> findIntentByIdempotencyKey(UUID idempotencyKey) {
        return paymentIntentRepository.findByIdempotencyKey(idempotencyKey).map(PaymentIntentEntity::toModel);
    }

    public Optional<Transaction> findTransactionByIdempotencyKey(UUID idempotencyKey) {
        return transactionRepository.findByIdempotencyKey(idempotencyKey).map(TransactionEntity::toModel);
    }

    public PaymentIntent saveIntent(PaymentIntent intent) {
        return paymentIntentRepository.save(PaymentIntentEntity.fromModel(intent)).toModel();
    }

    public Transaction saveTransaction(Transaction transaction) {
        return transactionRepository.save(TransactionEntity.fromModel(transaction)).toModel();
    }
}
