package dev.ebaptistella.monolith.shared.exception;

public final class AuthOperationException extends DomainHttpException {

    public AuthOperationException(int statusCode, String message) {
        super(statusCode, message);
    }

    public AuthOperationException(int statusCode, String message, Throwable cause) {
        super(statusCode, message, cause);
    }
}
