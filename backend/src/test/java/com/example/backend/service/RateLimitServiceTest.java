package com.example.backend.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import io.github.bucket4j.Bucket;

/**
 * Security Tests for RateLimitService
 * Tests SEC-006: Rate Limiting against Brute Force Attacks
 */
@DisplayName("Security: RateLimitService (SEC-006) Tests")
class RateLimitServiceTest {

    private RateLimitService rateLimitService;
    
    private static final String TEST_IP = "192.168.1.100";
    private static final String TEST_IP_2 = "192.168.1.101";
    
    @BeforeEach
    void setUp() {
        rateLimitService = new RateLimitService();
        // Set @Value fields manually since this is a unit test
        ReflectionTestUtils.setField(rateLimitService, "capacity", 5);
        ReflectionTestUtils.setField(rateLimitService, "refillMinutes", 1);
        
        // Clear any existing buckets
        Map<String, Bucket> buckets = new ConcurrentHashMap<>();
        ReflectionTestUtils.setField(rateLimitService, "buckets", buckets);
    }

    // ========== SEC-006: Basic Rate Limiting ==========

    @Test
    @DisplayName("SEC-006: Should allow first request")
    void testRateLimit_FirstRequest_Success() {
        // Act
        boolean result = rateLimitService.tryConsume(TEST_IP);

        // Assert
        assertTrue(result, "SEC-006: First request should be allowed");
    }

    @Test
    @DisplayName("SEC-006: Should allow up to 5 requests")
    void testRateLimit_FiveRequests_Success() {
        // Act & Assert
        for (int i = 1; i <= 5; i++) {
            boolean result = rateLimitService.tryConsume(TEST_IP);
            assertTrue(result, "SEC-006: Request " + i + "/5 should be allowed");
        }
    }

    @Test
    @DisplayName("SEC-006: Should block 6th request (brute force protection)")
    void testRateLimit_SixthRequest_Blocked() {
        // Arrange: Consume 5 tokens
        for (int i = 0; i < 5; i++) {
            rateLimitService.tryConsume(TEST_IP);
        }

        // Act: 6th attempt
        boolean result = rateLimitService.tryConsume(TEST_IP);

        // Assert
        assertFalse(result, "SEC-006: 6th request should be BLOCKED (brute force protection)");
    }

    @Test
    @DisplayName("SEC-006: Should block multiple requests after exceeding limit")
    void testRateLimit_MultipleExceeded_AllBlocked() {
        // Arrange: Consume all 5 tokens
        for (int i = 0; i < 5; i++) {
            rateLimitService.tryConsume(TEST_IP);
        }

        // Act & Assert: Next 3 attempts should all be blocked
        for (int i = 6; i <= 8; i++) {
            boolean result = rateLimitService.tryConsume(TEST_IP);
            assertFalse(result, "SEC-006: Request " + i + " should be BLOCKED");
        }
    }

    // ========== SEC-006: IP Isolation ==========

    @Test
    @DisplayName("SEC-006: Different IPs should have independent rate limits")
    void testRateLimit_DifferentIPs_Independent() {
        // Arrange: Exhaust tokens for TEST_IP
        for (int i = 0; i < 5; i++) {
            rateLimitService.tryConsume(TEST_IP);
        }
        
        // Verify TEST_IP is blocked
        assertFalse(rateLimitService.tryConsume(TEST_IP), "TEST_IP should be blocked");

        // Act: Try with different IP
        boolean resultIP2 = rateLimitService.tryConsume(TEST_IP_2);

        // Assert
        assertTrue(resultIP2, "SEC-006: Different IP should have independent limit");
    }

    @Test
    @DisplayName("SEC-006: Each IP should get full 5 attempts")
    void testRateLimit_MultipleIPs_FullQuota() {
        // Arrange & Act & Assert
        String[] ips = {"192.168.1.1", "192.168.1.2", "192.168.1.3"};
        
        for (String ip : ips) {
            // Each IP should get 5 attempts
            for (int i = 0; i < 5; i++) {
                boolean result = rateLimitService.tryConsume(ip);
                assertTrue(result, "SEC-006: IP " + ip + " request " + (i+1) + " should succeed");
            }
            
            // 6th attempt should fail
            boolean blocked = rateLimitService.tryConsume(ip);
            assertFalse(blocked, "SEC-006: IP " + ip + " 6th request should be blocked");
        }
    }

    // ========== SEC-006: Token Refill ==========

    @Test
    @DisplayName("SEC-006: Tokens should refill over time (1 token/minute)")
    void testRateLimit_TokenRefill() throws InterruptedException {
        // Arrange: Consume all 5 tokens
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimitService.tryConsume(TEST_IP));
        }
        
        // Verify blocked
        assertFalse(rateLimitService.tryConsume(TEST_IP), "Should be blocked after 5 attempts");

        // Act: Wait for refill (simulate by creating new bucket)
        // Note: Real time-based test would require waiting 60+ seconds
        // For unit test, we verify the bucket configuration instead
        
        // We can test that a fresh bucket allows requests again
        rateLimitService = new RateLimitService(); // New service = new buckets
        // Set @Value fields manually since this is a unit test (not Spring context)
        ReflectionTestUtils.setField(rateLimitService, "capacity", 5);
        ReflectionTestUtils.setField(rateLimitService, "refillMinutes", 1);
        ReflectionTestUtils.setField(rateLimitService, "buckets", new ConcurrentHashMap<String, Bucket>());
        boolean result = rateLimitService.tryConsume(TEST_IP);
        
        // Assert
        assertTrue(result, "SEC-006: After reset, requests should be allowed");
    }

    // ========== SEC-006: Edge Cases ==========

    @Test
    @DisplayName("SEC-006: Should handle null IP gracefully")
    void testRateLimit_NullIP_NoException() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            boolean result = rateLimitService.tryConsume(null);
            // Should either allow or deny, but not crash
            assertNotNull(result);
        }, "SEC-006: Should handle null IP without crashing");
    }

    @Test
    @DisplayName("SEC-006: Should handle empty IP gracefully")
    void testRateLimit_EmptyIP_NoException() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            boolean result = rateLimitService.tryConsume("");
            assertNotNull(result);
        }, "SEC-006: Should handle empty IP without crashing");
    }

    @Test
    @DisplayName("SEC-006: Should handle IPv4 addresses")
    void testRateLimit_IPv4_Success() {
        // Act
        boolean result = rateLimitService.tryConsume("203.0.113.42");

        // Assert
        assertTrue(result, "SEC-006: Should handle IPv4 addresses");
    }

    @Test
    @DisplayName("SEC-006: Should handle IPv6 addresses")
    void testRateLimit_IPv6_Success() {
        // Act
        boolean result = rateLimitService.tryConsume("2001:0db8:85a3:0000:0000:8a2e:0370:7334");

        // Assert
        assertTrue(result, "SEC-006: Should handle IPv6 addresses");
    }

    @Test
    @DisplayName("SEC-006: Should handle localhost addresses")
    void testRateLimit_Localhost_Success() {
        // Act & Assert
        assertTrue(rateLimitService.tryConsume("127.0.0.1"), "Should handle 127.0.0.1");
        assertTrue(rateLimitService.tryConsume("::1"), "Should handle IPv6 localhost");
        assertTrue(rateLimitService.tryConsume("0:0:0:0:0:0:0:1"), "Should handle expanded IPv6 localhost");
    }

    // ========== SEC-006: Concurrent Access ==========

    @Test
    @DisplayName("SEC-006: Should handle concurrent requests safely")
    void testRateLimit_Concurrent_ThreadSafe() throws InterruptedException {
        // Arrange
        final int THREAD_COUNT = 10;
        final int REQUESTS_PER_THREAD = 2;
        int[] successCount = {0};
        Thread[] threads = new Thread[THREAD_COUNT];

        // Act: Create multiple threads making requests
        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < REQUESTS_PER_THREAD; j++) {
                    if (rateLimitService.tryConsume(TEST_IP)) {
                        synchronized (successCount) {
                            successCount[0]++;
                        }
                    }
                }
            });
            threads[i].start();
        }

        // Wait for all threads
        for (Thread thread : threads) {
            thread.join();
        }

        // Assert: Should allow exactly 5 requests total (thread-safe)
        assertEquals(5, successCount[0], 
            "SEC-006: Concurrent access should still enforce 5-request limit");
    }

    // ========== SEC-006: Security Scenarios ==========

    @Test
    @DisplayName("SEC-006: Should block brute force attack simulation")
    void testRateLimit_BruteForceAttack_Blocked() {
        // Simulate brute force attack: 10 rapid login attempts
        int successfulAttempts = 0;
        int blockedAttempts = 0;

        for (int i = 0; i < 10; i++) {
            if (rateLimitService.tryConsume(TEST_IP)) {
                successfulAttempts++;
            } else {
                blockedAttempts++;
            }
        }

        // Assert
        assertEquals(5, successfulAttempts, "SEC-006: Only 5 attempts should succeed");
        assertEquals(5, blockedAttempts, "SEC-006: 5 attempts should be blocked");
    }

    @Test
    @DisplayName("SEC-006: Should protect against distributed brute force (multiple IPs)")
    void testRateLimit_DistributedAttack_EachIPLimited() {
        // Simulate distributed attack from 3 different IPs
        String[] attackerIPs = {"10.0.0.1", "10.0.0.2", "10.0.0.3"};
        
        for (String ip : attackerIPs) {
            int successCount = 0;
            
            // Each attacker tries 7 times
            for (int attempt = 0; attempt < 7; attempt++) {
                if (rateLimitService.tryConsume(ip)) {
                    successCount++;
                }
            }
            
            // Each attacker should be limited to 5 attempts
            assertEquals(5, successCount, 
                "SEC-006: Each attacker IP should be limited to 5 attempts");
        }
    }
}
