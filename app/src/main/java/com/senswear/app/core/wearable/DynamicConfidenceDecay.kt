package com.senswear.app.core.wearable

/**
 * Calculates dynamic confidence decay based on packet loss rate over sliding windows.
 */
class DynamicConfidenceDecay(
    private val windowSizeSamples: Int = 60,
    private val maxAllowedDropRate: Float = 0.20f
) {
    private val receptionWindow = ArrayDeque<Boolean>()

    fun recordPacketEvent(received: Boolean) {
        if (receptionWindow.size >= windowSizeSamples) {
            receptionWindow.removeFirst()
        }
        receptionWindow.addLast(received)
    }

    fun computeAdjustedConfidence(baseConfidence: Float): Float {
        if (receptionWindow.isEmpty()) return baseConfidence

        val receivedCount = receptionWindow.count { it }
        val successRate = receivedCount.toFloat() / receptionWindow.size.toFloat()
        val dropRate = 1.0f - successRate

        val penalty = if (dropRate > maxAllowedDropRate) {
            (dropRate - maxAllowedDropRate) * 1.5f
        } else {
            0.0f
        }

        return (baseConfidence - penalty).coerceIn(0.1f, 1.0f)
    }
}
