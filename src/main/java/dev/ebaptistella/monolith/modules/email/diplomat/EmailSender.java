package dev.ebaptistella.monolith.modules.email.diplomat;

import dev.ebaptistella.monolith.modules.email.models.EmailDispatch;

public interface EmailSender {

    void send(EmailDispatch dispatch);
}
