package dev.ebaptistella.monolith.modules.customer.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ebaptistella.monolith.modules.customer.diplomat.jpa.CustomerPersistence;
import dev.ebaptistella.monolith.modules.customer.diplomat.producer.CustomerEventProducer;
import dev.ebaptistella.monolith.modules.customer.models.CustomerRegistrationInput;
import dev.ebaptistella.monolith.modules.customer.logic.CustomerRegistrationResult;
import dev.ebaptistella.monolith.modules.customer.models.Customer;
import dev.ebaptistella.monolith.shared.exception.IdempotencyConflictException;
import dev.ebaptistella.monolith.shared.idempotency.IdempotencyContext;
import dev.ebaptistella.monolith.shared.idempotency.IdempotencyKeys;
import dev.ebaptistella.monolith.shared.idempotency.IdempotencyPayloadFingerprint;
import dev.ebaptistella.monolith.support.IdempotencyTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class RegisterCustomerControllerTest {

    @Mock
    private CustomerPersistence jpa;

    @Mock
    private CustomerEventProducer producer;

    private ObjectMapper objectMapper;
    private RegisterCustomerController controller;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        controller = new RegisterCustomerController(jpa, producer, objectMapper);
    }

    @Test
    void register_persistsCustomerAndPublishesEvent() {
        IdempotencyTestSupport.runWithRootKey(() -> {
            CustomerRegistrationInput input = new CustomerRegistrationInput("ana@example.com", "Ana Baptista");
            when(jpa.findByIdempotencyKey(any())).thenReturn(Optional.empty());
            when(jpa.existsByEmail("ana@example.com")).thenReturn(false);
            when(jpa.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

            CustomerRegistrationResult result = controller.register(input);

            assertThat(result.rejected()).isFalse();
            assertThat(result.replay()).isFalse();
            assertThat(result.customer()).map(Customer::email).contains("ana@example.com");
            verify(producer).publishCustomerCreated(any(Customer.class));
        });
    }

    @Test
    void register_rejectsDuplicateEmail() {
        IdempotencyTestSupport.runWithRootKey(() -> {
            CustomerRegistrationInput input = new CustomerRegistrationInput("ana@example.com", "Ana Baptista");
            when(jpa.findByIdempotencyKey(any())).thenReturn(Optional.empty());
            when(jpa.existsByEmail("ana@example.com")).thenReturn(true);

            CustomerRegistrationResult result = controller.register(input);

            assertThat(result.rejected()).isTrue();
            verify(jpa, never()).save(any());
            verify(producer, never()).publishCustomerCreated(any());
        });
    }

    @Test
    void register_replaysExistingCustomerWithoutPublishing() {
        UUID rootKey = UUID.randomUUID();
        IdempotencyContext.runWithRootKey(rootKey, () -> {
            UUID entityKey = IdempotencyKeys.derive(rootKey, "customer.register");
            CustomerRegistrationInput input = new CustomerRegistrationInput("ana@example.com", "Ana Baptista");
            String fingerprint = IdempotencyPayloadFingerprint.sha256(input, objectMapper);
            Customer existing = new Customer(
                    UUID.randomUUID(),
                    input.email(),
                    input.fullName(),
                    Instant.now(),
                    entityKey,
                    rootKey,
                    fingerprint);
            when(jpa.findByIdempotencyKey(entityKey)).thenReturn(Optional.of(existing));

            CustomerRegistrationResult result = controller.register(input);

            assertThat(result.replay()).isTrue();
            assertThat(result.customer()).contains(existing);
            verify(jpa, never()).save(any());
            verify(producer, never()).publishCustomerCreated(any());
        });
    }

    @Test
    void register_throwsWhenSameKeyWithDifferentPayload() {
        UUID rootKey = UUID.randomUUID();
        IdempotencyContext.runWithRootKey(rootKey, () -> {
            UUID entityKey = IdempotencyKeys.derive(rootKey, "customer.register");
            CustomerRegistrationInput input = new CustomerRegistrationInput("ana@example.com", "Ana Baptista");
            Customer existing = new Customer(
                    UUID.randomUUID(),
                    input.email(),
                    "Different Name",
                    Instant.now(),
                    entityKey,
                    rootKey,
                    "other-fingerprint");
            when(jpa.findByIdempotencyKey(entityKey)).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> controller.register(input))
                    .isInstanceOf(IdempotencyConflictException.class);
            verify(producer, never()).publishCustomerCreated(any());
        });
    }
}
