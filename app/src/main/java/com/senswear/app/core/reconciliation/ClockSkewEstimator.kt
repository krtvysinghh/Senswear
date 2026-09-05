package com.senswear.app.core.reconciliation

/**
 * Corrects wearable internal clock drift via linear regression between hardware tick timestamps
 * and Android local monotonic wall-clock time.
 */
class ClockSkewEstimator(
    private val maxSamples: Int = 50
) {
    private val samples = mutableListOf<Pair<Long, Long>>() // (HardwareTimeMs, SystemTimeMs)

    fun recordSyncPoint(hardwareTimestampMs: Long, systemTimestampMs: Long) {
        if (samples.size >= maxSamples) {
            samples.removeAt(0)
        }
        samples.add(Pair(hardwareTimestampMs, systemTimestampMs))
    }

    /**
     * Converts a raw hardware packet timestamp to an accurate NTP/System synchronized timestamp.
     */
    fun estimateTrueTimestamp(hardwareTimestampMs: Long): Long {
        if (samples.size < 2) return hardwareTimestampMs

        val n = samples.size.toDouble()
        val sumX = samples.sumOf { it.first.toDouble() }
        val sumY = samples.sumOf { it.second.toDouble() }
        val sumXY = samples.sumOf { it.first.toDouble() * it.second.toDouble() }
        val sumX2 = samples.sumOf { it.first.toDouble() * it.first.toDouble() }

        val slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX)
        val intercept = (sumY - slope * sumX) / n

        return (slope * hardwareTimestampMs + intercept).toLong()
    }
}
