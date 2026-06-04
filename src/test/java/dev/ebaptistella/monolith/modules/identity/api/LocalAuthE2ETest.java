package dev.ebaptistella.monolith.modules.identity.api;

import dev.ebaptistella.monolith.MonolithApplication;
import dev.ebaptistella.monolith.modules.email.controllers.SendEmailController;
import dev.ebaptistella.monolith.modules.email.logic.SentEmailRecorder;
import dev.ebaptistella.monolith.support.AsyncTestSupport;
import dev.ebaptistella.monolith.support.IdempotencyTestSupport;
import dev.ebaptistella.monolith.support.IntegrationTestContainers;
import dev.ebaptistella.monolith.support.LocalAuthE2eContextInitializer;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Tag("e2e")
@SpringBootTest(
        classes = MonolithApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("local-auth-e2e")
@ContextConfiguration(initializers = LocalAuthE2eContextInitializer.class)
class LocalAuthE2ETest extends IntegrationTestContainers {

    @Autowired
    private SentEmailRecorder sentEmails;

    @Autowired
    private SendEmailController sendEmailController;

    @BeforeEach
    void setUp() {
        RestAssured.port = LocalAuthE2eContextInitializer.PORT;
        sentEmails.clear();
        sendEmailController.resetProcessedDispatches();
    }

    @Test
    void registerLoginAndCreateCustomer() {
        String email = "local-user@example.com";

        IdempotencyTestSupport.withIdempotencyKey(RestAssured.given())
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "email": "%s",
                          "password": "password123"
                        }
                        """.formatted(email))
                .post("/api/v1/auth/register")
                .then()
                .statusCode(201)
                .body("email", equalTo(email));

        await().atMost(AsyncTestSupport.TIMEOUT).untilAsserted(() ->
                assertThat(sentEmails.sentEmails())
                        .anySatisfy(mail -> {
                            assertThat(mail.to()).isEqualTo(email);
                            assertThat(mail.subject()).isEqualTo("Your account was created");
                        }));

        String token = RestAssured.given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "email": "%s",
                          "password": "password123"
                        }
                        """.formatted(email))
                .post("/api/v1/auth/login")
                .then()
                .statusCode(200)
                .body("accessToken", notNullValue())
                .extract()
                .path("accessToken");

        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .header("X-Idempotency-Key", IdempotencyTestSupport.newKey())
                .body("""
                        {
                          "email": "customer-local@example.com",
                          "fullName": "Local Auth Customer"
                        }
                        """)
                .post("/api/v1/customers")
                .then()
                .statusCode(201)
                .body("fullName", equalTo("Local Auth Customer"));

        await().atMost(AsyncTestSupport.TIMEOUT).untilAsserted(() ->
                assertThat(sentEmails.sentEmails())
                        .anySatisfy(mail -> assertThat(mail.to()).isEqualTo("customer-local@example.com")));
    }
}
