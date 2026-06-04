package dev.ebaptistella.monolith.modules.email.diplomat;

import dev.ebaptistella.monolith.modules.email.models.EmailDispatch;
import dev.ebaptistella.monolith.shared.exception.EmailDispatchException;
import io.sentry.Sentry;
import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResilientEmailSender {

    private final EmailSender emailSender;

    @Retryable(retryFor = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 500))
    public void send(EmailDispatch dispatch) {
        emailSender.send(dispatch);
    }

    @Recover
    public void recoverSend(EmailDispatch dispatch, Exception ex) {
        Sentry.captureException(ex);
        throw new EmailDispatchException(
                "Failed to dispatch email " + dispatch.dispatchId() + " to " + dispatch.to(), ex);
    }
}
