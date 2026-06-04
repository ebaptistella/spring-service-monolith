package dev.ebaptistella.monolith.modules.customer.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ebaptistella.monolith.modules.customer.diplomat.jpa.CustomerPersistence;
import dev.ebaptistella.monolith.modules.customer.diplomat.producer.CustomerEventProducer;
import dev.ebaptistella.monolith.modules.customer.logic.CustomerRegistrationResult;
import dev.ebaptistella.monolith.modules.customer.logic.CustomerRules;
import dev.ebaptistella.monolith.modules.customer.models.Customer;
import dev.ebaptistella.monolith.modules.customer.models.CustomerRegistrationInput;
import dev.ebaptistella.monolith.shared.idempotency.IdempotencyContext;
import dev.ebaptistella.monolith.shared.idempotency.IdempotencyKeys;
import dev.ebaptistella.monolith.shared.idempotency.IdempotencyPayloadFingerprint;
import dev.ebaptistella.monolith.shared.idempotency.IdempotencyReplay;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegisterCustomerController {

    private static final String SCOPE = "customer.register";

    private final CustomerPersistence jpa;
    private final CustomerEventProducer producer;
    private final ObjectMapper objectMapper;

    @Transactional
    public CustomerRegistrationResult register(CustomerRegistrationInput input) {
        UUID rootKey = IdempotencyContext.requireRootKey();
        UUID entityKey = IdempotencyKeys.derive(rootKey, SCOPE);
        String fingerprint = IdempotencyPayloadFingerprint.sha256(input, objectMapper);

        return IdempotencyReplay.resolve(
                jpa.findByIdempotencyKey(entityKey),
                fingerprint,
                Customer::requestFingerprint,
                CustomerRegistrationResult::replay,
                () -> registerNew(input, entityKey, rootKey, fingerprint));
    }

    private CustomerRegistrationResult registerNew(
            CustomerRegistrationInput input, UUID entityKey, UUID rootKey, String fingerprint) {
        boolean emailExists = jpa.existsByEmail(input.email());
        CustomerRegistrationResult result = CustomerRules.register(input, emailExists);
        if (result.rejected()) {
            return result;
        }

        Customer draft = result.customer().orElseThrow();
        Customer toSave = new Customer(
                draft.id(),
                draft.email(),
                draft.fullName(),
                draft.createdAt(),
                entityKey,
                rootKey,
                fingerprint);
        Customer saved = jpa.save(toSave);
        producer.publishCustomerCreated(saved);
        return CustomerRegistrationResult.success(saved);
    }
}
