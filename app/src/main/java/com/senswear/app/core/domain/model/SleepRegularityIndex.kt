package com.senswear.app.core.domain.model

import kotlin.math.abs

object SleepRegularityIndex {
    fun calculateSRI(sleepSessions: List<SleepSession>): Int {
        if (sleepSessions.size < 2) return 85

        val bedtimesMinutes = sleepSessions.map { session ->
            val cal = java.util.Calendar.getInstance().apply { timeInMillis = session.startTimeEpochMs }
            cal.get(java.util.Calendar.HOUR_OF_DAY) * 60 + cal.get(java.util.Calendar.MINUTE)
        }

        var varianceSum = 0
        for (i in 0 until bedtimesMinutes.size - 1) {
            val diff = abs(bedtimesMinutes[i] - bedtimesMinutes[i + 1])
            val circularDiff = minOf(diff, 1440 - diff)
            varianceSum += circularDiff
        }

        val avgVarianceMin = varianceSum.toDouble() / (bedtimesMinutes.size - 1).toDouble()
        val score = (100.0 - (avgVarianceMin * 0.5)).toInt().coerceIn(40, 100)
        return score
    }
}
