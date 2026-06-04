package dev.ebaptistella.monolith.modules.order.api;

import dev.ebaptistella.monolith.modules.email.logic.SentEmailRecorder;
import dev.ebaptistella.monolith.modules.order.models.OrderStatus;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;

@Tag("e2e")
class OrderFlowE2ETest extends TestProfileIntegrationTest {

    @Autowired
    private SentEmailRecorder sentEmails;

    @Test
    void placeOrder_happyPath_confirmsOrderAndSendsEmail() {
        UUID customerId = registerCustomer("commerce@example.com", "Commerce User");
        UUID skuId = createSku("WIDGET-1", "150.00");
        adjustStock(skuId, 10);

        UUID orderId = placeOrder(customerId, skuId, 2);

        await().atMost(AsyncTestSupport.TIMEOUT).pollInterval(Duration.ofMillis(250)).untilAsserted(() ->
                RestAssured.given()
                        .when()
                        .get("/api/v1/orders/{id}", orderId)
                        .then()
                        .statusCode(200)
                        .body("status", equalTo(OrderStatus.CONFIRMED.name())));

        await().atMost(AsyncTestSupport.TIMEOUT).pollInterval(Duration.ofMillis(250)).untilAsserted(() ->
                assertThat(sentEmails.sentEmails())
                        .anySatisfy(email -> {
                            assertThat(email.to()).isEqualTo("commerce@example.com");
                            assertThat(email.subject()).isEqualTo("Your order was confirmed");
                            assertThat(email.body()).contains(orderId.toString());
                        }));
    }

    @Test
    void placeOrder_rejectsWhenStockIsInsufficient() {
        UUID customerId = registerCustomer("nostock@example.com", "No Stock User");
        UUID skuId = createSku("WIDGET-2", "50.00");

        RestAssured.given()
                .header(IdempotencyHeaders.X_IDEMPOTENCY_KEY, IdempotencyTestSupport.newKey())
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "customerId": "%s",
                          "lines": [
                            { "skuId": "%s", "quantity": 1 }
                          ]
                        }
                        """.formatted(customerId, skuId))
                .when()
                .post("/api/v1/orders")
                .then()
                .statusCode(409);
    }

    @Test
    void placeOrder_rejectsWhenSpendLimitWouldBeExceeded() {
        UUID customerId = registerCustomer("limit@example.com", "Limit User");
        UUID skuId = createSku("WIDGET-3", "600.00");
        adjustStock(skuId, 10);

        UUID firstOrderId = placeOrder(customerId, skuId, 1);
        await().atMost(AsyncTestSupport.TIMEOUT).pollInterval(Duration.ofMillis(250)).untilAsserted(() ->
                RestAssured.given()
                        .when()
                        .get("/api/v1/orders/{id}", firstOrderId)
                        .then()
                        .statusCode(200)
                        .body("status", equalTo(OrderStatus.CONFIRMED.name())));

        RestAssured.given()
                .header(IdempotencyHeaders.X_IDEMPOTENCY_KEY, IdempotencyTestSupport.newKey())
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "customerId": "%s",
                          "lines": [
                            { "skuId": "%s", "quantity": 1 }
                          ]
                        }
                        """.formatted(customerId, skuId))
                .when()
                .post("/api/v1/orders")
                .then()
                .statusCode(422);
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
