package dev.ebaptistella.monolith.support;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class AuthE2eJwtSupport {

    public static final String ISSUER = "http://localhost:18081/realms/monolith";
    public static final String SUBJECT = "kc-e2e-subject";
    public static final String EMAIL = "demo-user@example.com";

    private static final KeyPair KEY_PAIR = generateRsaKey();
    private static final JWKSource<SecurityContext> JWK_SOURCE = buildJwkSource();
    private static final JwtEncoder JWT_ENCODER = new NimbusJwtEncoder(JWK_SOURCE);

    private AuthE2eJwtSupport() {
    }

    public static JWKSet jwkSet() {
        RSAPublicKey publicKey = (RSAPublicKey) KEY_PAIR.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) KEY_PAIR.getPrivate();
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID("auth-e2e-key")
                .build();
        return new JWKSet(rsaKey);
    }

    public static String accessToken() {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(SUBJECT)
                .claim("email", EMAIL)
                .claim("preferred_username", EMAIL)
                .claim("realm_access", Map.of("roles", List.of("USER")))
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .build();
        return JWT_ENCODER.encode(org.springframework.security.oauth2.jwt.JwtEncoderParameters.from(claims))
                .getTokenValue();
    }

    private static JWKSource<SecurityContext> buildJwkSource() {
        return new ImmutableJWKSet<>(jwkSet());
    }

    private static KeyPair generateRsaKey() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
