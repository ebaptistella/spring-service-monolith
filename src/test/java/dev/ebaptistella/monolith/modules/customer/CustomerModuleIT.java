package dev.ebaptistella.monolith.modules.customer;

import dev.ebaptistella.monolith.modules.customer.controllers.RegisterCustomerController;
import dev.ebaptistella.monolith.modules.customer.models.CustomerRegistrationInput;
import dev.ebaptistella.monolith.shared.idempotency.IdempotencyContext;
import dev.ebaptistella.monolith.shared.wire.in.events.CustomerCreatedEvent;
import dev.ebaptistella.monolith.support.TestProfileIntegrationTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.EnableScenarios;
import org.springframework.modulith.test.Scenario;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@EnableScenarios
class CustomerModuleIT extends TestProfileIntegrationTest {

    @Autowired
    private RegisterCustomerController registerCustomerController;

    @Test
    void registerPublishesCustomerCreatedEvent(Scenario scenario) {
        UUID rootKey = UUID.randomUUID();
        CustomerRegistrationInput input = new CustomerRegistrationInput("modulith@example.com", "Modulith User");

        scenario.stimulate(() -> IdempotencyContext.runWithRootKey(rootKey, () -> registerCustomerController.register(input)))
                .andWaitForEventOfType(CustomerCreatedEvent.class)
                .toArriveAndVerify(event -> assertThat(event.email()).isEqualTo("modulith@example.com"));
    }
}
