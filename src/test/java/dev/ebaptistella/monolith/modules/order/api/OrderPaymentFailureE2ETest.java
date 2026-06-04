package dev.ebaptistella.monolith.modules.order.api;

import dev.ebaptistella.monolith.MonolithApplication;
import dev.ebaptistella.monolith.modules.email.controllers.SendEmailController;
import dev.ebaptistella.monolith.modules.email.logic.SentEmailRecorder;
import dev.ebaptistella.monolith.modules.order.models.OrderStatus;
import dev.ebaptistella.monolith.shared.idempotency.IdempotencyHeaders;
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
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;

@Tag("e2e")
@SpringBootTest(
        classes = MonolithApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "app.finance.payment-gateway.always-fail=true")
@ActiveProfiles("test")
class OrderPaymentFailureE2ETest extends IntegrationTestContainers {

    @LocalServerPort
    private int port;

    @Autowired
    private SentEmailRecorder sentEmails;

    @Autowired
    private SendEmailController sendEmailController;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        sentEmails.clear();
        sendEmailController.resetProcessedDispatches();
    }

    @Test
    void placeOrder_paymentFailure_cancelsOrderAndSendsCancellationEmail() {
        UUID customerId = registerCustomer("payfail@example.com", "Pay Fail User");
        UUID skuId = createSku("WIDGET-FAIL", "80.00");
        adjustStock(skuId, 10);

        UUID orderId = placeOrder(customerId, skuId, 1);

        await().atMost(AsyncTestSupport.TIMEOUT).pollInterval(Duration.ofMillis(250)).untilAsserted(() ->
                RestAssured.given()
                        .when()
                        .get("/api/v1/orders/{id}", orderId)
                        .then()
                        .statusCode(200)
                        .body("status", equalTo(OrderStatus.CANCELLED.name())));

        await().atMost(AsyncTestSupport.TIMEOUT).pollInterval(Duration.ofMillis(250)).untilAsserted(() ->
                assertThat(sentEmails.sentEmails())
                        .anySatisfy(email -> {
                            assertThat(email.to()).isEqualTo("payfail@example.com");
                            assertThat(email.subject()).contains("cancelled");
                        }));
    }

    private static UUID registerCustomer(String email, String fullName) {
        return parseId(RestAssured.given()
                .header(IdempotencyHeaders.X_IDEMPOTENCY_KEY, IdempotencyTestSupport.newKey())
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "email": "%s",
                          "fullName": "%s"
                        }
                        """.formatted(email, fullName))
                .when()
                .post("/api/v1/customers")
                .then()
                .statusCode(201)
                .extract()
                .path("id")
                .toString());
    }

    private static UUID parseId(Object id) {
        return UUID.fromString(id.toString());
    }

    private static UUID createSku(String code, String listPrice) {
        return parseId(RestAssured.given()
                .header(IdempotencyHeaders.X_IDEMPOTENCY_KEY, IdempotencyTestSupport.newKey())
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "productName": "Product %s",
                          "productDescription": "Test product",
                          "code": "%s",
                          "listPrice": %s
                        }
                        """.formatted(code, code, listPrice))
                .when()
                .post("/api/v1/catalog/skus")
                .then()
                .statusCode(201)
                .extract()
                .path("id")
                .toString());
    }

    private static void adjustStock(UUID skuId, int quantityDelta) {
        RestAssured.given()
                .header(IdempotencyHeaders.X_IDEMPOTENCY_KEY, IdempotencyTestSupport.newKey())
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "skuId": "%s",
                          "quantityDelta": %d
                        }
                        """.formatted(skuId, quantityDelta))
                .when()
                .post("/api/v1/inventory/adjustments")
                .then()
                .statusCode(204);
    }

    private static UUID placeOrder(UUID customerId, UUID skuId, int quantity) {
        return parseId(RestAssured.given()
                .header(IdempotencyHeaders.X_IDEMPOTENCY_KEY, IdempotencyTestSupport.newKey())
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "customerId": "%s",
                          "lines": [
                            { "skuId": "%s", "quantity": %d }
                          ]
                        }
                        """.formatted(customerId, skuId, quantity))
                .when()
                .post("/api/v1/orders")
                .then()
                .statusCode(201)
                .extract()
                .path("id")
                .toString());
    }
}
