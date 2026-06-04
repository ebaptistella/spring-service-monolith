package dev.ebaptistella.monolith.modules.email.diplomat.inmemory;

import dev.ebaptistella.monolith.modules.email.diplomat.EmailSender;
import dev.ebaptistella.monolith.modules.email.logic.SentEmailRecorder;
import dev.ebaptistella.monolith.modules.email.models.EmailDispatch;
import io.micrometer.observation.annotation.Observed;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@Profile({"test", "auth-e2e", "local-auth-e2e"})
public class InMemoryEmailSender implements EmailSender, SentEmailRecorder {

    private final List<SentEmail> sentEmails = Collections.synchronizedList(new ArrayList<>());

    @Override
    @Observed(name = "email.send", contextualName = "in-memory send")
    public void send(EmailDispatch dispatch) {
        sentEmails.add(new SentEmail(dispatch.to(), dispatch.subject(), dispatch.body()));
    }

    @Override
    public List<SentEmail> sentEmails() {
        return List.copyOf(sentEmails);
    }

    @Override
    public void clear() {
        sentEmails.clear();
    }
}
