package dev.ebaptistella.monolith.modules.customer.api;

import dev.ebaptistella.monolith.config.security.AuthE2eJwtDecoderConfiguration;
import dev.ebaptistella.monolith.MonolithApplication;
import dev.ebaptistella.monolith.modules.email.controllers.SendEmailController;
import dev.ebaptistella.monolith.modules.email.logic.SentEmailRecorder;
import dev.ebaptistella.monolith.support.AuthE2eJwtSupport;
import dev.ebaptistella.monolith.support.AsyncTestSupport;
import dev.ebaptistella.monolith.support.IdempotencyTestSupport;
import dev.ebaptistella.monolith.support.IntegrationTestContainers;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;

@Tag("e2e")
@SpringBootTest(
        classes = MonolithApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.security.oauth2.resourceserver.jwt.issuer-uri=" + AuthE2eJwtSupport.ISSUER
        })
@ActiveProfiles("auth-e2e")
@Import(AuthE2eJwtDecoderConfiguration.class)
class ZCustomerJwtAuthE2ETest extends IntegrationTestContainers {

    @LocalServerPort
    private int port;

    @Autowired
    private SentEmailRecorder sentEmails;

    @Autowired
    private SendEmailController sendEmailController;

    @DynamicPropertySource
    static void authProperties(DynamicPropertyRegistry registry) {
        registry.add("app.security.strategies.jwt-external", () -> "true");
        registry.add("app.security.oauth2-enabled", () -> "true");
        registry.add("app.security.jwt.keycloak-issuer", () -> AuthE2eJwtSupport.ISSUER);
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> AuthE2eJwtSupport.ISSUER);
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        sentEmails.clear();
        sendEmailController.resetProcessedDispatches();
    }

    @Test
    void registerCustomer_withoutToken_returns401() {
        IdempotencyTestSupport.withIdempotencyKey(RestAssured.given())
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "email": "unauth@example.com",
                          "fullName": "Unauthorized User"
                        }
                        """)
                .when()
                .post("/api/v1/customers")
                .then()
                .statusCode(401);
    }

    @Test
    void registerCustomer_withExternalJwt_returns201() {
        IdempotencyTestSupport.withIdempotencyKey(RestAssured.given())
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + AuthE2eJwtSupport.accessToken())
                .body("""
                        {
                          "email": "auth@example.com",
                          "fullName": "Authenticated User"
                        }
                        """)
                .when()
                .post("/api/v1/customers")
                .then()
                .statusCode(201)
                .body("email", equalTo("auth@example.com"))
                .body("fullName", equalTo("Authenticated User"));

        await().atMost(AsyncTestSupport.TIMEOUT).pollInterval(Duration.ofMillis(250)).untilAsserted(() ->
                assertThat(sentEmails.sentEmails())
                        .anySatisfy(email -> assertThat(email.to()).isEqualTo("auth@example.com")));
    }
}
