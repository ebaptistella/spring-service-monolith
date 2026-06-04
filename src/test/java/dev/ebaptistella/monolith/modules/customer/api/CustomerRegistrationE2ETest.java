package dev.ebaptistella.monolith.modules.customer.api;

import dev.ebaptistella.monolith.modules.email.logic.SentEmailRecorder;
import dev.ebaptistella.monolith.shared.idempotency.IdempotencyHeaders;
import dev.ebaptistella.monolith.support.AsyncTestSupport;
import dev.ebaptistella.monolith.support.IdempotencyTestSupport;
import dev.ebaptistella.monolith.support.TestProfileIntegrationTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;

@Tag("e2e")
class CustomerRegistrationE2ETest extends TestProfileIntegrationTest {

    @Autowired
    private SentEmailRecorder sentEmails;

    @Test
    void registerCustomer_sendsWelcomeEmail() {
        IdempotencyTestSupport.withIdempotencyKey(RestAssured.given())
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "email": "ana@example.com",
                          "fullName": "Ana Baptista"
                        }
                        """)
                .when()
                .post("/api/v1/customers")
                .then()
                .statusCode(201)
                .body("email", equalTo("ana@example.com"))
                .body("fullName", equalTo("Ana Baptista"));

        await().atMost(AsyncTestSupport.TIMEOUT).pollInterval(Duration.ofMillis(250)).untilAsserted(() ->
                assertThat(sentEmails.sentEmails())
                        .anySatisfy(email -> {
                            assertThat(email.to()).isEqualTo("ana@example.com");
                            assertThat(email.subject()).isEqualTo("Your account was created");
                            assertThat(email.body()).contains("Ana Baptista");
                        })
        );
    }

    @Test
    void registerCustomer_replaysSameIdempotencyKeyWithoutDuplicateEmail() {
        String idempotencyKey = IdempotencyTestSupport.newKey();
        String body = """
                {
                  "email": "replay@example.com",
                  "fullName": "Replay User"
                }
                """;

        RestAssured.given()
                .header(IdempotencyHeaders.X_IDEMPOTENCY_KEY, idempotencyKey)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/api/v1/customers")
                .then()
                .statusCode(201);

        RestAssured.given()
                .header(IdempotencyHeaders.X_IDEMPOTENCY_KEY, idempotencyKey)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/api/v1/customers")
                .then()
                .statusCode(200)
                .body("email", equalTo("replay@example.com"));

        await().atMost(AsyncTestSupport.TIMEOUT).pollInterval(Duration.ofMillis(250)).untilAsserted(() ->
                assertThat(sentEmails.sentEmails())
                        .filteredOn(email -> email.to().equals("replay@example.com"))
                        .hasSize(1));
    }
}
