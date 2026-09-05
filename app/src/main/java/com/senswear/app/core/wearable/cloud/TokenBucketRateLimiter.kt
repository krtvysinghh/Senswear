package com.senswear.app.core.wearable.cloud

import kotlinx.coroutines.delay
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random

/**
 * Token-bucket rate limiter with jittered exponential backoff for Cloud APIs (Garmin / Whoop).
 */
class TokenBucketRateLimiter(
    private val capacity: Int = 30,
    private val refillTokensPerSecond: Double = 5.0
) {
    private var availableTokens = capacity.toDouble()
    private var lastRefillTimestampMs = System.currentTimeMillis()

    suspend fun acquire() {
        while (true) {
            val now = System.currentTimeMillis()
            val elapsedSec = (now - lastRefillTimestampMs) / 1000.0
            availableTokens = min(capacity.toDouble(), availableTokens + elapsedSec * refillTokensPerSecond)
            lastRefillTimestampMs = now

            if (availableTokens >= 1.0) {
                availableTokens -= 1.0
                return
            } else {
                delay(100)
            }
        }
    }

    /**
     * Calculates exponential backoff with jitter on HTTP 429 Too Many Requests.
     */
    fun calculateBackoffDelayMs(retryAttempt: Int, baseDelayMs: Long = 1000L): Long {
        val exponent = retryAttempt.coerceAtMost(6)
        val exponential = baseDelayMs * 2.0.pow(exponent.toDouble()).toLong()
        val jitter = Random.nextLong(100, 500)
        return exponential + jitter
    }
}
