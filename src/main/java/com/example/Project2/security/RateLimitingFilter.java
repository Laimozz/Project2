package com.example.Project2.security;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    private static final String LOGIN_PATH = "/api/auth/login";
    private static final String REGISTER_PATH = "/api/auth/register";
    private static final String KEY_PREFIX = "project2:rate-limit:";

    private final ProxyManager<String> proxyManager;

    public RateLimitingFilter(ProxyManager<String> proxyManager) {
        this.proxyManager = proxyManager;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return resolvePolicy(request) == null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        LimitPolicy policy = resolvePolicy(request);
        if (policy == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String clientIp = resolveClientIp(request);
            String key = KEY_PREFIX + policy.name() + ":" + sanitize(clientIp);

            Bucket bucket = proxyManager.getProxy(key, () -> BucketConfiguration.builder()
                    .addLimit(limit -> limit.capacity(policy.capacity())
                            .refillGreedy(policy.capacity(), policy.window()))
                    .build());

            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

            response.setHeader("X-Rate-Limit-Limit", String.valueOf(policy.capacity()));
            response.setHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));

            if (probe.isConsumed()) {
                filterChain.doFilter(request, response);
                return;
            }

            long retryAfterSeconds = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill());
            if (retryAfterSeconds <= 0) {
                retryAfterSeconds = 1;
            }

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));

            String body = """
                    {
                      "error": "Too many requests. Please try again later.",
                      "status": 429,
                      "retryAfterSeconds": %d
                    }
                    """.formatted(retryAfterSeconds);

            response.getWriter().write(body);
        } catch (Exception ex) {
            log.warn("Redis/Bucket4j unavailable, skip rate limiting", ex);
            filterChain.doFilter(request, response);
        }
    }

    private LimitPolicy resolvePolicy(HttpServletRequest request) {
        String method = request.getMethod();
        String path = resolvePath(request);

        if ("POST".equalsIgnoreCase(method) && LOGIN_PATH.equals(path)) {
            return new LimitPolicy("login", 5, Duration.ofMinutes(1));
        }

        if ("POST".equalsIgnoreCase(method) && REGISTER_PATH.equals(path)) {
            return new LimitPolicy("register", 3, Duration.ofMinutes(10));
        }

        return null;
    }

    private String resolvePath(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();

        if (contextPath != null && !contextPath.isBlank() && requestUri.startsWith(contextPath)) {
            return requestUri.substring(contextPath.length());
        }

        return requestUri;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        } else {
            ip = ip.split(",")[0].trim();
        }

        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            return "127.0.0.1";
        }

        return ip;
    }

    private String sanitize(String value) {
        return value.replace(":", "_");
    }

    private record LimitPolicy(String name, long capacity, Duration window) {
    }
}
