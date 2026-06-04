package dev.ebaptistella.monolith.config.idempotency;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ebaptistella.monolith.shared.idempotency.IdempotencyContext;
import dev.ebaptistella.monolith.shared.idempotency.IdempotencyHeaders;
import dev.ebaptistella.monolith.shared.web.ProblemDetailSupport;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
@RequiredArgsConstructor
public class IdempotencyWebFilter extends OncePerRequestFilter {

    private static final Set<String> MUTATING_METHODS = Set.of("POST", "PUT", "PATCH");

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!requiresIdempotencyKey(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader(IdempotencyHeaders.X_IDEMPOTENCY_KEY);
        if (!StringUtils.hasText(header)) {
            writeBadRequest(response, request.getRequestURI(), "Missing required header " + IdempotencyHeaders.X_IDEMPOTENCY_KEY);
            return;
        }

        UUID rootKey;
        try {
            rootKey = UUID.fromString(header.trim());
        } catch (IllegalArgumentException ex) {
            writeBadRequest(
                    response,
                    request.getRequestURI(),
                    "Invalid " + IdempotencyHeaders.X_IDEMPOTENCY_KEY + ": must be a UUID");
            return;
        }

        try {
            IdempotencyContext.callWithRootKey(rootKey, () -> {
                MDC.put("idempotencyKey", rootKey.toString());
                try {
                    filterChain.doFilter(request, response);
                } finally {
                    MDC.remove("idempotencyKey");
                }
                return null;
            });
        } catch (ServletException ex) {
            throw ex;
        } catch (IOException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ServletException("Idempotency filter failed", ex);
        }
    }

    private static boolean requiresIdempotencyKey(HttpServletRequest request) {
        if (!request.getRequestURI().startsWith("/api/")) {
            return false;
        }
        if (!MUTATING_METHODS.contains(request.getMethod())) {
            return false;
        }
        return !request.getRequestURI().startsWith("/api/v1/auth/login")
                && !request.getRequestURI().startsWith("/api/v1/auth/token");
    }

    private void writeBadRequest(HttpServletResponse response, String path, String message) throws IOException {
        ProblemDetail problem = ProblemDetailSupport.badRequest(message, path);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), problem);
    }
}
