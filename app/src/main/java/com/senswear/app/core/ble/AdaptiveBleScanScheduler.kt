package com.senswear.app.core.ble

import kotlin.math.min
import kotlin.math.pow

/**
 * Adaptive BLE scan duty cycler with exponential backoff to minimize phone battery consumption
 * when a target wearable is disconnected or out of range.
 */
class AdaptiveBleScanScheduler(
    private val baseScanDurationMs: Long = 10_000L, // 10s active scan
    private val baseSleepDurationMs: Long = 30_000L, // 30s initial pause
    private val maxSleepDurationMs: Long = 5 * 60 * 1000L // Max 5 minutes pause
) {
    data class DutyCycleStep(
        val scanDurationMs: Long,
        val sleepDurationMs: Long,
        val attemptCount: Int
    )

    fun computeNextCycle(consecutiveFailures: Int): DutyCycleStep {
        val multiplier = 2.0.pow(consecutiveFailures.coerceAtMost(5).toDouble()).toLong()
        val nextSleepMs = min(maxSleepDurationMs, baseSleepDurationMs * multiplier)

        return DutyCycleStep(
            scanDurationMs = baseScanDurationMs,
            sleepDurationMs = nextSleepMs,
            attemptCount = consecutiveFailures
        )
    }
}
