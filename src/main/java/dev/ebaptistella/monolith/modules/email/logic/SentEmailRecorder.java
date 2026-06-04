package dev.ebaptistella.monolith.modules.email.logic;

import java.util.List;

/**
 * Test-only contract to assert outbound emails without coupling tests to infrastructure diplomats.
 */
public interface SentEmailRecorder {

    List<SentEmail> sentEmails();

    void clear();

    record SentEmail(String to, String subject, String body) {
    }
}
