package dev.ebaptistella.monolith.shared.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.util.List;

public final class ProblemDetailSupport {

    private ProblemDetailSupport() {}

    public static ProblemDetail badRequest(String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setTitle("Bad Request");
        return problem;
    }

    public static ProblemDetail badRequest(String detail, String path) {
        ProblemDetail problem = badRequest(detail);
        if (path != null) {
            problem.setInstance(URI.create(path));
        }
        return problem;
    }

    public static ProblemDetail validationProblem(
            HttpStatus status, String detail, String path, List<String> errors) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle("Validation failed");
        if (path != null) {
            problem.setInstance(URI.create(path));
        }
        problem.setProperty("errors", errors);
        return problem;
    }
}
