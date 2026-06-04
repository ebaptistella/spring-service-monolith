package dev.ebaptistella.monolith.modules.identity.diplomat.outbound;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class ExternalTokenException extends ResponseStatusException {

    public ExternalTokenException(int statusCode, String body) {
        super(HttpStatus.valueOf(statusCode), extractDetail(body));
    }

    private static String extractDetail(String body) {
        if (body == null || body.isBlank()) {
            return "External token request failed";
        }
        return body.length() > 500 ? body.substring(0, 500) : body;
    }
}
