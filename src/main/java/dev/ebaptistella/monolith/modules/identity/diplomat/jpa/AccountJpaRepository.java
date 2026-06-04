package dev.ebaptistella.monolith.modules.identity.diplomat.jpa;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface AccountJpaRepository extends JpaRepository<AccountEntity, UUID> {

    @EntityGraph(attributePaths = "roles")
    Optional<AccountEntity> findByEmail(String email);

    @EntityGraph(attributePaths = "roles")
    Optional<AccountEntity> findById(UUID id);

    @EntityGraph(attributePaths = "roles")
    Optional<AccountEntity> findByIdempotencyKey(UUID idempotencyKey);
}
