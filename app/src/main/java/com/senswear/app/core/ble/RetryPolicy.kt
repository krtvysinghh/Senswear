package com.senswear.app.core.ble

import kotlinx.coroutines.delay
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random

data class RetryConfig(
    val initialDelayMs: Long = 1000L,
    val maxDelayMs: Long = 30000L,
    val factor: Double = 2.0,
    val maxAttempts: Int = 5,
    val jitter: Boolean = true
)

class ExponentialBackoffRetry(private val config: RetryConfig = RetryConfig()) {
    private var currentAttempt = 0

    val hasRemainingAttempts: Boolean
        get() = currentAttempt < config.maxAttempts

    suspend fun waitNextRetry(): Boolean {
        if (!hasRemainingAttempts) return false
        val baseDelay = config.initialDelayMs * config.factor.pow(currentAttempt.toDouble()).toLong()
        val cappedDelay = min(baseDelay, config.maxDelayMs)
        val finalDelay = if (config.jitter) {
            (cappedDelay * (0.8 + Random.nextDouble() * 0.4)).toLong()
        } else {
            cappedDelay
        }
        currentAttempt++
        delay(finalDelay)
        return true
    }

    fun reset() {
        currentAttempt = 0
    }
}
