package dev.ebaptistella.monolith.shared.web;

import dev.ebaptistella.monolith.shared.exception.AuthOperationException;
import dev.ebaptistella.monolith.shared.exception.DomainHttpException;
import dev.ebaptistella.monolith.shared.exception.EmailDispatchException;
import dev.ebaptistella.monolith.shared.exception.IdempotencyConflictException;
import dev.ebaptistella.monolith.shared.web.ProblemDetailSupport;
import io.sentry.Sentry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        capture(ex);
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        return validationProblem(HttpStatus.BAD_REQUEST, "Invalid request payload", request.getRequestURI(), details);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraint(ConstraintViolationException ex, HttpServletRequest request) {
        capture(ex);
        List<String> details = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .toList();
        return validationProblem(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI(), details);
    }

    @ExceptionHandler(DomainHttpException.class)
    public ProblemDetail handleDomainHttp(DomainHttpException ex) {
        HttpStatus status = switch (ex) {
            case IdempotencyConflictException ignored -> HttpStatus.CONFLICT;
            case EmailDispatchException ignored -> HttpStatus.SERVICE_UNAVAILABLE;
            case AuthOperationException auth -> {
                HttpStatus resolved = HttpStatus.resolve(auth.statusCode());
                yield resolved != null ? resolved : HttpStatus.BAD_REQUEST;
            }
            default -> {
                HttpStatus resolved = HttpStatus.resolve(ex.statusCode());
                yield resolved != null ? resolved : HttpStatus.BAD_REQUEST;
            }
        };
        if (ex instanceof EmailDispatchException) {
            capture(ex);
            return ProblemDetail.forStatusAndDetail(status, "Email dispatch temporarily unavailable");
        }
        return ProblemDetail.forStatusAndDetail(status, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        capture(ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(BadCredentialsException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ProblemDetail handleNotFound(NoSuchElementException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail handleResponseStatus(ResponseStatusException ex) {
        String detail = ex.getReason() != null ? ex.getReason() : ex.getStatusCode().toString();
        return ProblemDetail.forStatusAndDetail(ex.getStatusCode(), detail);
    }

    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ProblemDetail handleNoResourceFound(
            org.springframework.web.servlet.resource.NoResourceFoundException ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setInstance(URI.create(request.getRequestURI()));
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnhandled(Exception ex, HttpServletRequest request) {
        capture(ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error");
        problem.setInstance(URI.create(request.getRequestURI()));
        return problem;
    }

    private static void capture(Throwable ex) {
        Sentry.captureException(ex);
    }

    private static ProblemDetail validationProblem(
            HttpStatus status, String detail, String path, List<String> errors) {
        return ProblemDetailSupport.validationProblem(status, detail, path, errors);
    }
}
