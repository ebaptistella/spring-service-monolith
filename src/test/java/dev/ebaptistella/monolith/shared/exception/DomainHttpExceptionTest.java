package dev.ebaptistella.monolith.shared.exception;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
class DomainHttpExceptionTest {

    @Test
    void idempotencyConflict_hasConflictStatus() {
        DomainHttpException ex = new IdempotencyConflictException();
        assertThat(ex.statusCode()).isEqualTo(HttpStatus.CONFLICT.value());
    }

    @Test
    void authOperation_preservesStatusCode() {
        DomainHttpException ex = new AuthOperationException(HttpStatus.NOT_IMPLEMENTED.value(), "not implemented");
        assertThat(ex.statusCode()).isEqualTo(501);
    }

    @Test
    void emailDispatch_hasServiceUnavailableStatus() {
        DomainHttpException ex = new EmailDispatchException("failed", new RuntimeException("smtp"));
        assertThat(ex.statusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value());
    }
}
