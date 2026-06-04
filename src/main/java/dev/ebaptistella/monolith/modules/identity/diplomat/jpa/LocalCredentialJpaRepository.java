package dev.ebaptistella.monolith.modules.identity.diplomat.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

interface LocalCredentialJpaRepository extends JpaRepository<LocalCredentialEntity, UUID> {

    @Query("""
            select c from LocalCredentialEntity c
            join AccountEntity a on a.id = c.accountId
            where a.email = :email
            """)
    Optional<LocalCredentialEntity> findByAccountEmail(String email);
}
