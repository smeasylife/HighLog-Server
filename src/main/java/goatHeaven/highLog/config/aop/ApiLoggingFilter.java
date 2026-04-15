package goatHeaven.highLog.config.aop;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jboss.logging.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Component
@Slf4j
public class ApiLoggingFilter extends OncePerRequestFilter {

    private static final int CACHE_LIMIT = 1024 * 10;
    private static final int MAX_PAYLOAD_LENGTH = 1000;
    private static final Set<String> EXCLUDE_URIS = Set.of("/actuator/health", "/favicon.ico");

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        ContentCachingRequestWrapper req = new ContentCachingRequestWrapper(request, CACHE_LIMIT);
        ContentCachingResponseWrapper res = new ContentCachingResponseWrapper(response);

        // traceId 설정 - 이후 AOP 로그에도 자동으로 붙음
        MDC.put("traceId", UUID.randomUUID().toString().substring(0, 8));
        MDC.put("method", request.getMethod());
        MDC.put("uri", request.getRequestURI());

        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(req, res);
        } finally {
            long elapsed = System.currentTimeMillis() - startTime;
            int status = res.getStatus();

            logRequest(req);
            logResponse(res, status, elapsed);

            res.copyBodyToResponse();
            MDC.clear();
        }
    }

    private void logRequest(ContentCachingRequestWrapper req) {
        String body = parseBody(req.getContentAsByteArray(), req.getCharacterEncoding());

        if (req.getContentLength() > CACHE_LIMIT) {
            log.info("[REQUEST] ip={} | body={}...[{}bytes 중 {}bytes만 캐싱]",
                    getClientIp(req),
                    mask(truncate(body)),
                    req.getContentLength(),
                    CACHE_LIMIT
            );
        } else {
            log.info("[REQUEST] ip={} | body={}", getClientIp(req), mask(truncate(body)));
        }
    }

    private void logResponse(ContentCachingResponseWrapper res, int status, long elapsed) {
        String body = parseBody(res.getContentAsByteArray(), res.getCharacterEncoding());

        if (status >= 500) {
            log.error("[RESPONSE] status={} | elapsed={}ms | body={}", status, elapsed, truncate(body));
        } else if (status >= 400) {
            log.warn("[RESPONSE] status={} | elapsed={}ms | body={}", status, elapsed, truncate(body));
        } else {
            log.info("[RESPONSE] status={} | elapsed={}ms", status, elapsed);
        }
    }

    private String parseBody(byte[] content, String encoding) {
        if (content.length == 0) return "-";
        try {
            return new String(content, encoding);
        } catch (Exception e) {
            return "[parse error]";
        }
    }

    private String truncate(String body) {
        if (body == null || body.isBlank()) return "-";
        return body.length() > MAX_PAYLOAD_LENGTH
                ? body.substring(0, MAX_PAYLOAD_LENGTH) + "...[truncated]"
                : body;
    }

    private String mask(String body) {
        if (body == null) return "-";
        return body
                .replaceAll("(?i)(\"password\"\\s*:\\s*\")([^\"]+)(\")", "$1****$3")
                .replaceAll("(?i)(\"cardNumber\"\\s*:\\s*\")([^\"]+)(\")", "$1****$3");
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) ip = request.getRemoteAddr();
        return ip.split(",")[0].trim();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return EXCLUDE_URIS.contains(request.getRequestURI());
    }
}
