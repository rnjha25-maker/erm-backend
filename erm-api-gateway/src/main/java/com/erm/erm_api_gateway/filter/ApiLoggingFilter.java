package com.erm.erm_api_gateway.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.*;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Component
public class ApiLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(ApiLoggingFilter.class);

    private static final String[] EXCLUDED_PATHS = {
            "/actuator", "/health", "/swagger-ui", "/v3/api-docs"
    };

    private static final int MAX_PAYLOAD_LENGTH = 4096;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if (isExcluded(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        response.setHeader("X-Trace-Id", traceId);

        long start = System.currentTimeMillis();

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);

            long duration = System.currentTimeMillis() - start;

            logRequest(wrappedRequest);
            logResponse(wrappedRequest, wrappedResponse, duration);

        } catch (Exception ex) {
            logger.error("API ERROR method={} uri={} traceId={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    traceId,
                    ex);
            throw ex;
        } finally {
            wrappedResponse.copyBodyToResponse();
            MDC.clear();
        }
    }

    private boolean isExcluded(String uri) {
        if (uri == null) return true;
        return Arrays.stream(EXCLUDED_PATHS).anyMatch(uri::startsWith);
    }

    private void logRequest(ContentCachingRequestWrapper request) {

        String method = request.getMethod();
        String uri = request.getRequestURI();
        String query = request.getQueryString();

        Map<String, String> headers = getHeaders(request);

        String body = getPayload(request.getContentAsByteArray(), request.getCharacterEncoding());

        // Log only important methods with body
        if (isBodyAllowed(method)) {
            logger.info("REQ method={} uri={} query={} headers={} body={}",
                    method, uri, query, headers, body);
        } else {
            logger.info("REQ method={} uri={} query={} headers={}",
                    method, uri, query, headers);
        }
    }

    private void logResponse(ContentCachingRequestWrapper request,
                             ContentCachingResponseWrapper response,
                             long durationMs) {

        String method = request.getMethod();
        String uri = request.getRequestURI();
        int status = response.getStatus();

        String responseBody = null;

        if (isJsonResponse(response)) {
            responseBody = getPayload(response.getContentAsByteArray(), response.getCharacterEncoding());
        }

        if (status >= 400) {
            logger.warn("RES method={} uri={} status={} duration={}ms body={}",
                    method, uri, status, durationMs, responseBody);
        } else {
            logger.info("RES method={} uri={} status={} duration={}ms body={}",
                    method, uri, status, durationMs, responseBody);
        }
    }

    private boolean isBodyAllowed(String method) {
        return "POST".equalsIgnoreCase(method)
                || "PUT".equalsIgnoreCase(method)
                || "PATCH".equalsIgnoreCase(method);
    }

    private boolean isJsonResponse(ContentCachingResponseWrapper response) {
        String contentType = response.getContentType();
        return contentType != null && contentType.contains("application/json");
    }

    private String getPayload(byte[] content, String encoding) {
        if (content == null || content.length == 0) return null;

        int length = Math.min(content.length, MAX_PAYLOAD_LENGTH);

        try {
            String payload = new String(content, 0, length,
                    encoding != null ? encoding : StandardCharsets.UTF_8.name());

            return maskSensitive(payload);

        } catch (Exception e) {
            return "[unreadable]";
        }
    }

    private String maskSensitive(String body) {
        if (body == null) return null;

        return body
                .replaceAll("(?i)\"password\"\\s*:\\s*\".*?\"", "\"password\":\"***\"")
                .replaceAll("(?i)\"token\"\\s*:\\s*\".*?\"", "\"token\":\"***\"");
    }

    private Map<String, String> getHeaders(HttpServletRequest request) {

        Enumeration<String> headerNames = request.getHeaderNames();

        if (headerNames == null) {
            return Collections.emptyMap();
        }

        return Collections.list(headerNames).stream()
                .collect(Collectors.toMap(
                        h -> h,
                        h -> isSensitiveHeader(h) ? "[MASKED]" : request.getHeader(h)
                ));
    }

    private boolean isSensitiveHeader(String header) {
        String h = header.toLowerCase();
        return h.contains("authorization")
                || h.contains("cookie")
                || h.contains("token");
    }
}