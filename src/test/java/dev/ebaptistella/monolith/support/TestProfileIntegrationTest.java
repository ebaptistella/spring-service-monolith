package dev.ebaptistella.monolith.support;

import dev.ebaptistella.monolith.MonolithApplication;
import dev.ebaptistella.monolith.modules.email.controllers.SendEmailController;
import dev.ebaptistella.monolith.modules.email.logic.SentEmailRecorder;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        classes = MonolithApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class TestProfileIntegrationTest extends IntegrationTestContainers {

    @BeforeEach
    void bindRestAssuredAndResetEmailState(
            @LocalServerPort int port,
            @Autowired SentEmailRecorder sentEmailRecorder,
            @Autowired SendEmailController sendEmailController) {
        RestAssured.port = port;
        sentEmailRecorder.clear();
        sendEmailController.resetProcessedDispatches();
    }
}
