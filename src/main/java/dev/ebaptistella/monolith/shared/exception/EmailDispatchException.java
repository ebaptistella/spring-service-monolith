package dev.ebaptistella.monolith.shared.exception;

import org.springframework.http.HttpStatus;

public final class EmailDispatchException extends DomainHttpException {

    public EmailDispatchException(String message, Throwable cause) {
        super(HttpStatus.SERVICE_UNAVAILABLE.value(), message, cause);
    }
}
