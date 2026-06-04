package dev.ebaptistella.monolith.config;

import dev.ebaptistella.monolith.support.TestProfileIntegrationTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.containsString;

@Tag("integration")
class ModulithActuatorIT extends TestProfileIntegrationTest {

    @Test
    void modulithEndpointExposesModules() {
        RestAssured.given()
                .when()
                .get("/actuator/modulith")
                .then()
                .statusCode(200)
                .body(containsString("customer"))
                .body(containsString("order"));
    }
}
