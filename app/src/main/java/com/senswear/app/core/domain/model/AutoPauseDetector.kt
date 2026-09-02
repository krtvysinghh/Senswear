package com.senswear.app.core.domain.model

class AutoPauseDetector(
    private val speedThresholdKmh: Double = 1.2,
    private val idleTimeoutMs: Long = 6000L
) {
    private var lastMovementTimeEpochMs = 0L
    var isAutoPaused: Boolean = false
        private set

    fun updateSpeed(currentSpeedKmh: Double, timestampEpochMs: Long): Boolean {
        val wasPaused = isAutoPaused
        if (currentSpeedKmh >= speedThresholdKmh) {
            lastMovementTimeEpochMs = timestampEpochMs
            isAutoPaused = false
        } else {
            if (lastMovementTimeEpochMs > 0 && (timestampEpochMs - lastMovementTimeEpochMs) >= idleTimeoutMs) {
                isAutoPaused = true
            }
        }
        return wasPaused != isAutoPaused
    }

    fun reset() {
        lastMovementTimeEpochMs = 0L
        isAutoPaused = false
    }
}
