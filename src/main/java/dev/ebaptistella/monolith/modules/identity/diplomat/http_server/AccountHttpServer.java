package dev.ebaptistella.monolith.modules.identity.diplomat.http_server;

import dev.ebaptistella.monolith.modules.identity.adapters.AccountAdapter;
import dev.ebaptistella.monolith.modules.identity.controllers.GetAccountController;
import dev.ebaptistella.monolith.modules.identity.controllers.UpdateAccountStatusController;
import dev.ebaptistella.monolith.modules.identity.wire.in.UpdateAccountStatusRequest;
import dev.ebaptistella.monolith.modules.identity.wire.out.AccountResponse;
import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@Tag(name = "Accounts", description = "Account administration")
@RequiredArgsConstructor
public class AccountHttpServer {

    private final GetAccountController getAccountController;
    private final UpdateAccountStatusController updateAccountStatusController;

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Observed(name = "http.server", contextualName = "account get")
    @Operation(summary = "Get account by id")
    public AccountResponse getById(@PathVariable UUID id) {
        return AccountAdapter.modelToWireOut(getAccountController.getById(id));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Observed(name = "http.server", contextualName = "account updateStatus")
    @Operation(summary = "Update account status")
    public AccountResponse updateStatus(
            @PathVariable UUID id, @Valid @RequestBody UpdateAccountStatusRequest request) {
        return AccountAdapter.modelToWireOut(
                updateAccountStatusController.updateStatus(id, request.status()));
    }
}
