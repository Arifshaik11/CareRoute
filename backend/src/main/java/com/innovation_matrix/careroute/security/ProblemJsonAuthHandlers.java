package com.innovation_matrix.careroute.security;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/** Writes 401/403 responses raised by the security filter chain as RFC 7807 JSON. */
@Component
public class ProblemJsonAuthHandlers {

    private final ObjectMapper objectMapper;

    public ProblemJsonAuthHandlers(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public AuthenticationEntryPoint entryPoint() {
        return (request, response, ex) ->
                write(response, HttpStatus.UNAUTHORIZED, "Unauthorized", "Authentication is required");
    }

    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, ex) ->
                write(response, HttpStatus.FORBIDDEN, "Forbidden", "You do not have access to this resource");
    }

    private void write(HttpServletResponse response, HttpStatus status, String title, String detail)
            throws IOException {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), pd);
    }
}
