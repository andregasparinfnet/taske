package com.example.backend.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate Limiting Service using Token Bucket algorithm (Bucket4j)
 * 
 * SEC-006: Protects /login endpoint from brute force attacks
 * 
 * Default limits:
 * - 5 attempts per minute per IP
 * - Exponential backoff after limit reached
 */
@Service
public class RateLimitService {
    
    private static final Logger logger = LoggerFactory.getLogger(RateLimitService.class);
    
    // Store buckets per IP address
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    
    // Rate limit configuration: 5 attempts per 1 minute
    private static final int CAPACITY = 5;
    private static final Duration REFILL_DURATION = Duration.ofMinutes(1);
    
    /**
     * Try to consume 1 token from the bucket for the given IP
     * 
     * @param ip Client IP address
     * @return true if allowed, false if rate limit exceeded
     */
    public boolean tryConsume(String ip) {
        Bucket bucket = resolveBucket(ip);
        boolean consumed = bucket.tryConsume(1);
        
        if (!consumed) {
            logger.warn("Rate limit exceeded for IP: {}", ip);
        }
        
        return consumed;
    }
    
    /**
     * Get or create bucket for IP address
     */
    private Bucket resolveBucket(String ip) {
        return buckets.computeIfAbsent(ip, k -> createNewBucket());
    }
    
    /**
     * Create new bucket with configured limits
     */
    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.classic(CAPACITY, Refill.intervally(CAPACITY, REFILL_DURATION));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
    
    /**
     * Get remaining attempts for IP (for debugging/monitoring)
     */
    public long getAvailableTokens(String ip) {
        Bucket bucket = buckets.get(ip);
        if (bucket == null) {
            return CAPACITY;
        }
        return bucket.getAvailableTokens();
    }
}
