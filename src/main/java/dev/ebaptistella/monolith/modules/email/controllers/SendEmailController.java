package dev.ebaptistella.monolith.modules.email.controllers;

import dev.ebaptistella.monolith.modules.email.diplomat.ResilientEmailSender;
import dev.ebaptistella.monolith.modules.email.logic.EmailDispatchRules;
import dev.ebaptistella.monolith.modules.email.logic.EmailDispatchValidationResult;
import dev.ebaptistella.monolith.modules.email.models.EmailDispatch;
import dev.ebaptistella.monolith.shared.exception.EmailDispatchException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendEmailController {

    private final ResilientEmailSender resilientEmailSender;
    private final Set<UUID> processedDispatchIds = ConcurrentHashMap.newKeySet();

    public EmailDispatchValidationResult dispatch(EmailDispatch dispatch) {
        EmailDispatchValidationResult validation = EmailDispatchRules.validate(dispatch);
        if (validation.rejected()) {
            log.warn(
                    "Email dispatch rejected dispatchId={} reason={}",
                    dispatch.dispatchId(),
                    validation.error().orElse("unknown"));
            return validation;
        }

        EmailDispatch validDispatch = validation.dispatch().orElseThrow();
        if (!processedDispatchIds.add(validDispatch.dispatchId())) {
            log.info(
                    "Email dispatch skipped (already processed) dispatchId={} to={}",
                    validDispatch.dispatchId(),
                    validDispatch.to());
            return validation;
        }

        try {
            resilientEmailSender.send(validDispatch);
        } catch (EmailDispatchException ex) {
            processedDispatchIds.remove(validDispatch.dispatchId());
            log.error(
                    "Email dispatch failed dispatchId={} to={}",
                    validDispatch.dispatchId(),
                    validDispatch.to(),
                    ex);
            throw ex;
        }
        log.info("Email dispatched dispatchId={} to={}", validDispatch.dispatchId(), validDispatch.to());
        return validation;
    }

    public void resetProcessedDispatches() {
        processedDispatchIds.clear();
    }
}
