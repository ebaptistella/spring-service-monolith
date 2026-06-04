package dev.ebaptistella.monolith.modules.identity.api;

import com.sun.net.httpserver.HttpServer;
import dev.ebaptistella.monolith.MonolithApplication;
import dev.ebaptistella.monolith.support.IdempotencyTestSupport;
import dev.ebaptistella.monolith.support.IntegrationTestContainers;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.equalTo;

@Tag("e2e")
@SpringBootTest(
        classes = MonolithApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("auth-e2e")
class ExternalAuthProxyE2ETest extends IntegrationTestContainers {

    private static final HttpServer mockKeycloak;
    private static final String mockTokenUri;

    static {
        try {
            mockKeycloak = HttpServer.create(new InetSocketAddress(0), 0);
            mockKeycloak.createContext("/realms/monolith/protocol/openid-connect/token", exchange -> {
                String body = """
                        {
                          "access_token": "mock-keycloak-token",
                          "token_type": "Bearer",
                          "expires_in": 3600
                        }
                        """;
                byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream outputStream = exchange.getResponseBody()) {
                    outputStream.write(bytes);
                }
            });
            mockKeycloak.start();
            mockTokenUri = "http://localhost:"
                    + mockKeycloak.getAddress().getPort()
                    + "/realms/monolith/protocol/openid-connect/token";
        } catch (IOException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    @LocalServerPort
    private int port;

    @AfterAll
    static void stopMockKeycloak() {
        mockKeycloak.stop(0);
    }

    @DynamicPropertySource
    static void externalAuthProperties(DynamicPropertyRegistry registry) {
        registry.add("app.security.strategies.jwt-external", () -> "true");
        registry.add("app.security.strategies.local", () -> "false");
        registry.add("app.security.oauth2-enabled", () -> "true");
        registry.add("app.security.external.token-uri", () -> mockTokenUri);
        registry.add("app.security.jwt.keycloak-issuer", () -> "http://localhost:8091/realms/monolith");
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> "http://localhost:8091/realms/monolith");
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void config_exposesExternalStrategy() {
        RestAssured.given()
                .when()
                .get("/api/v1/auth/config")
                .then()
                .statusCode(200)
                .body("strategies[0]", equalTo("jwt-external"))
                .body("registrationEnabled", equalTo(false))
                .body("registrationMode", equalTo("none"))
                .body("defaultLoginProvider", equalTo("external"))
                .body("external.provider", equalTo("keycloak"))
                .body("external.loginProxyEndpoint", equalTo("/api/v1/auth/login"))
                .body("loginEnabled", equalTo(true));
    }

    @Test
    void login_proxiesToExternalProvider() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "email": "demo-user@example.com",
                          "password": "demo-password"
                        }
                        """)
                .when()
                .post("/api/v1/auth/login")
                .then()
                .statusCode(200)
                .body("accessToken", equalTo("mock-keycloak-token"));
    }

    @Test
    void token_proxiesOAuth2FormRequest() {
        RestAssured.given()
                .contentType(ContentType.URLENC)
                .formParam("grant_type", "password")
                .formParam("username", "demo-user@example.com")
                .formParam("password", "demo-password")
                .when()
                .post("/api/v1/auth/token")
                .then()
                .statusCode(200)
                .body("access_token", equalTo("mock-keycloak-token"));
    }

    @Test
    void register_whenExternalOnly_returnsNotImplemented() {
        IdempotencyTestSupport.withIdempotencyKey(RestAssured.given())
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "email": "new@example.com",
                          "password": "password123"
                        }
                        """)
                .when()
                .post("/api/v1/auth/register")
                .then()
                .statusCode(501);
    }
}
