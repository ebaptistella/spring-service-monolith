package dev.ebaptistella.monolith.modules.identity.diplomat.jpa;

import dev.ebaptistella.monolith.shared.models.auth.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface AccountIdentityLinkJpaRepository extends JpaRepository<AccountIdentityLinkEntity, UUID> {

    Optional<AccountIdentityLinkEntity> findByProviderAndExternalSubject(
            AuthProvider provider, String externalSubject);
}
