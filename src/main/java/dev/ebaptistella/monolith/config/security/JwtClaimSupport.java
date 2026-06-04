package dev.ebaptistella.monolith.config.security;

import dev.ebaptistella.monolith.shared.models.auth.Role;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class JwtClaimSupport {

    private JwtClaimSupport() {
    }

    public static String extractEmail(Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        if (email != null && !email.isBlank()) {
            return email;
        }
        return jwt.getClaimAsString("preferred_username");
    }

    public static Set<Role> extractRoles(Jwt jwt) {
        Set<Role> roles = new LinkedHashSet<>();
        addRolesFromClaim(roles, jwt.getClaimAsStringList("roles"));
        addRolesFromClaim(roles, extractKeycloakRealmRoles(jwt));
        addRolesFromClaim(roles, extractKeycloakClientRoles(jwt));
        addRolesFromScope(roles, jwt.getClaimAsString("scope"));
        if (roles.isEmpty()) {
            roles.add(Role.USER);
        }
        return Set.copyOf(roles);
    }

    @SuppressWarnings("unchecked")
    private static List<String> extractKeycloakClientRoles(Jwt jwt) {
        Object resourceAccess = jwt.getClaim("resource_access");
        if (!(resourceAccess instanceof Map<?, ?> clients)) {
            return List.of();
        }
        java.util.List<String> roles = new java.util.ArrayList<>();
        for (Object clientEntry : clients.values()) {
            if (!(clientEntry instanceof Map<?, ?> clientMap)) {
                continue;
            }
            Object clientRoles = clientMap.get("roles");
            if (clientRoles instanceof Collection<?> collection) {
                collection.stream().map(Object::toString).forEach(roles::add);
            }
        }
        return roles;
    }

    @SuppressWarnings("unchecked")
    private static List<String> extractKeycloakRealmRoles(Jwt jwt) {
        Object realmAccess = jwt.getClaim("realm_access");
        if (!(realmAccess instanceof Map<?, ?> map)) {
            return List.of();
        }
        Object roles = map.get("roles");
        if (!(roles instanceof Collection<?> collection)) {
            return List.of();
        }
        return collection.stream().map(Object::toString).toList();
    }

    private static void addRolesFromClaim(Set<Role> roles, List<String> claimRoles) {
        if (claimRoles == null) {
            return;
        }
        for (String role : claimRoles) {
            mapRole(role).ifPresent(roles::add);
        }
    }

    private static void addRolesFromScope(Set<Role> roles, String scope) {
        if (scope == null || scope.isBlank()) {
            return;
        }
        for (String value : scope.split(" ")) {
            mapRole(value).ifPresent(roles::add);
        }
    }

    private static java.util.Optional<Role> mapRole(String value) {
        if (value == null || value.isBlank()) {
            return java.util.Optional.empty();
        }
        String normalized = value.toUpperCase();
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring("ROLE_".length());
        }
        try {
            return java.util.Optional.of(Role.valueOf(normalized));
        } catch (IllegalArgumentException ex) {
            return java.util.Optional.empty();
        }
    }
}
