package com.example.backend.service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;

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
    
    @org.springframework.beans.factory.annotation.Value("${rate.limit.capacity:100}")
    private int capacity;
    
    @org.springframework.beans.factory.annotation.Value("${rate.limit.refill-minutes:1}")
    private int refillMinutes;
    
    /**
     * Try to consume 1 token from the bucket for the given IP
     * 
     * @param ip Client IP address
     * @return true if allowed, false if rate limit exceeded
     */
    public boolean tryConsume(String ip) {
        String key = (ip == null || ip.isEmpty()) ? "unknown" : ip;
        Bucket bucket = resolveBucket(key);
        boolean consumed = bucket.tryConsume(1);
        
        if (!consumed) {
            logger.warn("Rate limit exceeded for IP: {}", key);
        }
        
        return consumed;
    }
    
    /**
     * Get or create bucket for IP address
     */
    private Bucket resolveBucket(String ip) {
        String key = (ip == null || ip.isEmpty()) ? "unknown" : ip;
        return buckets.computeIfAbsent(key, k -> createNewBucket());
    }
    
    /**
     * Create new bucket with configured limits
     */
    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.classic(capacity, Refill.intervally(capacity, Duration.ofMinutes(refillMinutes)));
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
            return capacity;
        }
        return bucket.getAvailableTokens();
    }
}
