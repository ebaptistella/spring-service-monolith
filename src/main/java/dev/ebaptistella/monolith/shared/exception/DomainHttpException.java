package dev.ebaptistella.monolith.shared.exception;

public sealed class DomainHttpException extends RuntimeException
        permits AuthOperationException, IdempotencyConflictException, EmailDispatchException {

    private final int statusCode;

    protected DomainHttpException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    protected DomainHttpException(int statusCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int statusCode() {
        return statusCode;
    }
}
