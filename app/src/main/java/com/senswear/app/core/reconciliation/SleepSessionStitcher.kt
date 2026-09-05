package com.senswear.app.core.reconciliation

/**
 * Stitches split nocturnal sleep sessions separated by < 60 minutes into a unified circadian sleep session.
 */
class SleepSessionStitcher(
    private val maxBreakDurationMs: Long = 60 * 60 * 1000L // 60 minutes
) {
    data class SleepInterval(
        val startTimestampMs: Long,
        val endTimestampMs: Long,
        val stage: String,
        val efficiencyScore: Int
    )

    fun stitch(sessions: List<SleepInterval>): List<SleepInterval> {
        if (sessions.size <= 1) return sessions

        val sorted = sessions.sortedBy { it.startTimestampMs }
        val stitched = mutableListOf<SleepInterval>()

        var current = sorted[0]

        for (i in 1 until sorted.size) {
            val next = sorted[i]
            val gap = next.startTimestampMs - current.endTimestampMs

            if (gap in 0..maxBreakDurationMs) {
                // Merge sessions
                val mergedEnd = maxOf(current.endTimestampMs, next.endTimestampMs)
                val mergedEfficiency = (current.efficiencyScore + next.efficiencyScore) / 2
                current = current.copy(
                    endTimestampMs = mergedEnd,
                    efficiencyScore = mergedEfficiency
                )
            } else {
                stitched.add(current)
                current = next
            }
        }
        stitched.add(current)

        return stitched
    }
}
