package dev.ebaptistella.monolith.modules.identity.controllers;

import dev.ebaptistella.monolith.modules.identity.diplomat.jpa.AccountPersistence;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocalUserDetailsService implements UserDetailsService {

    private final AccountPersistence jpa;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var account = jpa.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Account not found: " + email));
        jpa.findLocalCredentialByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Local credential not found: " + email));

        var authorities = account.roles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(java.util.stream.Collectors.toUnmodifiableSet());

        return User.withUsername(email).password("{noop}").authorities(authorities).build();
    }
}
