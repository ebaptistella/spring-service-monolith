package dev.ebaptistella.monolith.modules.finance.diplomat.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface PaymentIntentJpaRepository extends JpaRepository<PaymentIntentEntity, UUID> {

    Optional<PaymentIntentEntity> findByOrderId(UUID orderId);

    Optional<PaymentIntentEntity> findByIdempotencyKey(UUID idempotencyKey);
}
