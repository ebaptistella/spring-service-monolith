package dev.ebaptistella.monolith.config.idempotency;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ebaptistella.monolith.shared.idempotency.IdempotencyHeaders;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class IdempotencyWebFilterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private IdempotencyWebFilter filter;
    private StringWriter responseBody;

    @BeforeEach
    void setUp() throws Exception {
        filter = new IdempotencyWebFilter(objectMapper);
        responseBody = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseBody));
    }

    @Test
    void rejectsMissingHeaderWithProblemDetail() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/customers");
        when(request.getMethod()).thenReturn("POST");

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(response).setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        verify(filterChain, never()).doFilter(any(), any());
        JsonNode body = objectMapper.readTree(responseBody.toString());
        assertThat(body.get("status").asInt()).isEqualTo(400);
        assertThat(body.get("title").asText()).isEqualTo("Bad Request");
        assertThat(body.get("detail").asText()).contains(IdempotencyHeaders.X_IDEMPOTENCY_KEY);
    }

    @Test
    void rejectsInvalidUuidWithProblemDetail() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/customers");
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader(IdempotencyHeaders.X_IDEMPOTENCY_KEY)).thenReturn("not-a-uuid");

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(filterChain, never()).doFilter(any(), any());
        JsonNode body = objectMapper.readTree(responseBody.toString());
        assertThat(body.get("detail").asText()).contains("UUID");
    }
}
