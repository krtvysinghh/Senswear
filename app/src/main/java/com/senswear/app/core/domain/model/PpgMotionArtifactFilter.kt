package com.senswear.app.core.domain.model

import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Correlates 3-axis accelerometer motion intensity with raw optical PPG pulse streams
 * to reject cadence harmonics and movement artifacts.
 */
class PpgMotionArtifactFilter(
    private val motionThresholdG: Double = 0.45,
    private val maxAllowedHeartRateDeltaBpm: Double = 18.0
) {
    data class MotionVector(
        val x: Double,
        val y: Double,
        val z: Double
    ) {
        val magnitudeG: Double
            get() = sqrt(x * x + y * y + z * z)
    }

    data class FilteredPulseResult(
        val rawBpm: Int,
        val filteredBpm: Int,
        val isArtifactRejected: Boolean,
        val confidenceScore: Float
    )

    private var lastValidBpm: Int? = null

    fun filterPulse(rawBpm: Int, motion: MotionVector): FilteredPulseResult {
        val motionIntensity = abs(motion.magnitudeG - 1.0) // Deviation from 1G earth gravity
        val previous = lastValidBpm

        if (previous == null) {
            lastValidBpm = rawBpm
            return FilteredPulseResult(
                rawBpm = rawBpm,
                filteredBpm = rawBpm,
                isArtifactRejected = false,
                confidenceScore = 1.0f
            )
        }

        val delta = abs(rawBpm - previous).toDouble()

        // If strong motion is detected and heart rate jumps abruptly, reject artifact
        return if (motionIntensity > motionThresholdG && delta > maxAllowedHeartRateDeltaBpm) {
            // Apply exponential dampening towards last known valid physiological state
            val smoothedBpm = (previous * 0.85 + rawBpm * 0.15).toInt()
            FilteredPulseResult(
                rawBpm = rawBpm,
                filteredBpm = smoothedBpm,
                isArtifactRejected = true,
                confidenceScore = 0.35f
            )
        } else {
            lastValidBpm = rawBpm
            FilteredPulseResult(
                rawBpm = rawBpm,
                filteredBpm = rawBpm,
                isArtifactRejected = false,
                confidenceScore = (1.0f - (motionIntensity * 0.5f).toFloat()).coerceIn(0.5f, 1.0f)
            )
        }
    }
}
