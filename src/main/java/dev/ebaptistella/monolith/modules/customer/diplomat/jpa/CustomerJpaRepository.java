package dev.ebaptistella.monolith.modules.customer.diplomat.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface CustomerJpaRepository extends JpaRepository<CustomerEntity, UUID> {

    boolean existsByEmail(String email);

    Optional<CustomerEntity> findByIdempotencyKey(UUID idempotencyKey);
}
