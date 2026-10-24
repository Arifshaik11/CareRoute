package com.innovation_matrix.careroute.security;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

/** For SSE: EventSource cannot send headers, so the JWT arrives as ?token=<jwt>. */
public class SseTokenFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String token = request.getParameter("token");
        if (token != null && !token.isBlank()
                && request.getRequestURI().contains("/api/events")
                && request.getHeader("Authorization") == null) {
            request = new BearerWrappedRequest(request, token);
        }
        chain.doFilter(request, response);
    }

    private static class BearerWrappedRequest extends HttpServletRequestWrapper {
        private final String bearer;

        BearerWrappedRequest(HttpServletRequest request, String token) {
            super(request);
            this.bearer = "Bearer " + token;
        }

        @Override
        public String getHeader(String name) {
            if ("Authorization".equalsIgnoreCase(name)) return bearer;
            return super.getHeader(name);
        }
    }
}
