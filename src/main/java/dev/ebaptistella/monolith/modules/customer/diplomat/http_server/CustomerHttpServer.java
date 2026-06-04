package dev.ebaptistella.monolith.modules.customer.diplomat.http_server;

import dev.ebaptistella.monolith.modules.customer.adapters.CustomerAdapter;
import dev.ebaptistella.monolith.modules.customer.controllers.GetCustomerController;
import dev.ebaptistella.monolith.modules.customer.controllers.RegisterCustomerController;
import dev.ebaptistella.monolith.modules.customer.logic.CustomerRegistrationResult;
import dev.ebaptistella.monolith.modules.customer.models.Customer;
import dev.ebaptistella.monolith.modules.customer.wire.in.CreateCustomerRequest;
import dev.ebaptistella.monolith.modules.customer.wire.out.CustomerResponse;
import dev.ebaptistella.monolith.shared.idempotency.IdempotentHttpOutcome;
import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@Tag(name = "Customers", description = "Customer registration")
@RequiredArgsConstructor
public class CustomerHttpServer {

    private final RegisterCustomerController registerCustomerController;
    private final GetCustomerController getCustomerController;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Observed(name = "http.server", contextualName = "customer create")
    @Operation(summary = "Register customer", description = "Creates a customer and triggers welcome notification via email")
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        CustomerRegistrationResult result = registerCustomerController.register(
                CustomerAdapter.wireInToRegistrationInput(request));

        if (result.rejected()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    result.error().orElse("Registration rejected"));
        }

        Customer customer = result.customer().orElseThrow();
        CustomerResponse body = CustomerAdapter.modelToWireOut(customer);
        IdempotentHttpOutcome<CustomerResponse> outcome = result.replay()
                ? IdempotentHttpOutcome.replay(body)
                : IdempotentHttpOutcome.created(body);
        return ResponseEntity.status(outcome.replay() ? HttpStatus.OK : HttpStatus.CREATED).body(outcome.body());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @Observed(name = "http.server", contextualName = "customer get")
    @Operation(summary = "Get customer by id", description = "Loads customer from DB; second call may hit Redis cache")
    public CustomerResponse getCustomer(@PathVariable UUID id) {
        Customer customer = getCustomerController.getById(id);
        return CustomerAdapter.modelToWireOut(customer);
    }
}
