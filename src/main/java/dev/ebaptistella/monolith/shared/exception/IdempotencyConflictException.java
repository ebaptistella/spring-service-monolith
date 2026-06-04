package dev.ebaptistella.monolith.shared.exception;

import org.springframework.http.HttpStatus;

public final class IdempotencyConflictException extends DomainHttpException {

    public IdempotencyConflictException() {
        super(HttpStatus.CONFLICT.value(), "Idempotency key reused with a different request payload");
    }
}
