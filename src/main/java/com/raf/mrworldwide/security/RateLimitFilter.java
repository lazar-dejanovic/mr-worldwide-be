package com.raf.mrworldwide.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RateLimitFilter extends GenericFilterBean {

    private final Cache<String, Bucket> generalCache;
    private final Cache<String, Bucket> aiCache;
    private final ObjectMapper objectMapper;
    private final RequestMatcher excludedMatcher;
    private final RequestMatcher aiMatcher;

    public RateLimitFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;

        this.generalCache = Caffeine.newBuilder()
                .expireAfterAccess(1, TimeUnit.HOURS)
                .maximumSize(10_000)
                .build();

        this.aiCache = Caffeine.newBuilder()
                .expireAfterAccess(1, TimeUnit.HOURS)
                .maximumSize(10_000)
                .build();

        PathPatternRequestMatcher.Builder builder = PathPatternRequestMatcher.withDefaults();
        this.excludedMatcher = new OrRequestMatcher(
                builder.matcher("/swagger-ui/**"),
                builder.matcher("/v3/api-docs/**"),
                builder.matcher("/actuator/**"),
                builder.matcher("/error")
        );

        this.aiMatcher = builder.matcher("/api/ai/**");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (excludedMatcher.matches(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }

        String key = resolveKey(httpRequest);
        Bucket bucket = resolveBucket(httpRequest, key);

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded for key={}, path={}", key, httpRequest.getRequestURI());
            sendRateLimitResponse(httpResponse, aiMatcher.matches(httpRequest));
        }
    }

    private Bucket resolveBucket(HttpServletRequest request, String key) {
        if (aiMatcher.matches(request)) {
            return aiCache.get(key, k -> createBucket(20, Duration.ofMinutes(1)));
        }
        return generalCache.get(key, k -> createBucket(100, Duration.ofMinutes(1)));
    }

    private Bucket createBucket(long capacity, Duration refillPeriod) {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(capacity)
                        .refillGreedy(capacity, refillPeriod)
                        .build())
                .build();
    }

    private String resolveKey(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String[] parts = authHeader.split("\\.");
            if (parts.length == 3) {
                return "user:" + parts[1];
            }
        }
        String ip = request.getHeader("X-Forwarded-For");
        return "ip:" + (ip != null ? ip.split(",")[0].trim() : request.getRemoteAddr());
    }

    private void sendRateLimitResponse(HttpServletResponse response, boolean isAi) throws IOException {
        String message = isAi
                ? "AI rate limit exceeded. Maximum 20 requests per minute."
                : "Rate limit exceeded. Maximum 100 requests per minute.";

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(Map.of("error", message)));
    }
}