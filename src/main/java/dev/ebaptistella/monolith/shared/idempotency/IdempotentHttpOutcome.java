package dev.ebaptistella.monolith.shared.idempotency;

public record IdempotentHttpOutcome<T>(T body, boolean replay) {

    public static <T> IdempotentHttpOutcome<T> created(T body) {
        return new IdempotentHttpOutcome<>(body, false);
    }

    public static <T> IdempotentHttpOutcome<T> replay(T body) {
        return new IdempotentHttpOutcome<>(body, true);
    }
}
