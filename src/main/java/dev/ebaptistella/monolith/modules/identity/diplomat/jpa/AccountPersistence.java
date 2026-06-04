package dev.ebaptistella.monolith.modules.identity.diplomat.jpa;

import dev.ebaptistella.monolith.modules.identity.models.Account;
import dev.ebaptistella.monolith.modules.identity.models.AccountIdentityLink;
import dev.ebaptistella.monolith.modules.identity.models.LocalCredential;
import dev.ebaptistella.monolith.shared.models.auth.AuthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountPersistence {

    private final AccountJpaRepository accountRepository;
    private final AccountIdentityLinkJpaRepository identityLinkRepository;
    private final LocalCredentialJpaRepository localCredentialRepository;

    public Account save(Account account) {
        return accountRepository.save(AccountEntity.fromModel(account)).toModel();
    }

    public Optional<Account> findById(UUID id) {
        return accountRepository.findById(id).map(AccountEntity::toModel);
    }

    public Optional<Account> findByEmail(String email) {
        return accountRepository.findByEmail(email).map(AccountEntity::toModel);
    }

    public Optional<Account> findByIdempotencyKey(UUID idempotencyKey) {
        return accountRepository.findByIdempotencyKey(idempotencyKey).map(AccountEntity::toModel);
    }

    public Optional<Account> findByProviderAndSubject(AuthProvider provider, String externalSubject) {
        return identityLinkRepository
                .findByProviderAndExternalSubject(provider, externalSubject)
                .flatMap(link -> findById(link.getAccountId()));
    }

    public AccountIdentityLink saveIdentityLink(AccountIdentityLink link) {
        return identityLinkRepository
                .save(AccountIdentityLinkEntity.fromModel(link))
                .toModel();
    }

    public void saveLocalCredential(LocalCredential credential) {
        localCredentialRepository.save(LocalCredentialEntity.fromModel(credential));
    }

    public Optional<LocalCredential> findLocalCredentialByEmail(String email) {
        return localCredentialRepository.findByAccountEmail(email).map(LocalCredentialEntity::toModel);
    }
}
