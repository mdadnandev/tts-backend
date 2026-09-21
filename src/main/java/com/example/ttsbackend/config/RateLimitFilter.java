package com.example.ttsbackend.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple per-IP rate limiter for /api/tts.
 * Limit: 10 requests per minute per IP.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket newBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(10,
                        Refill.greedy(10, Duration.ofMinutes(1))))
                .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain)
            throws ServletException, IOException {

        String uri = req.getRequestURI();

        // Only rate-limit the expensive TTS generation endpoint
        if (!uri.startsWith("/api/tts")) {
            chain.doFilter(req, res);
            return;
        }

        // Prefer X-Forwarded-For when behind a proxy
        String ip = req.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = req.getRemoteAddr();
        } else {
            ip = ip.split(",")[0].trim();
        }

        Bucket bucket = buckets.computeIfAbsent(ip, k -> newBucket());

        if (bucket.tryConsume(1)) {
            chain.doFilter(req, res);
        } else {
            log.warn("Rate limit hit for IP: {}", ip);
            res.setStatus(429);
            res.setContentType("application/json");
            res.setCharacterEncoding("UTF-8");
            res.getWriter().write(
                    "{\"success\":false,\"status\":429,\"error\":\"RATE_LIMIT\"," +
                            "\"message\":\"Too many requests. Please try again in a minute.\"}"
            );
        }
    }
}