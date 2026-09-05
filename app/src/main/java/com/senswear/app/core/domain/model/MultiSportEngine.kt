package com.senswear.app.core.domain.model

/**
 * Computes sport-specific physiological metrics:
 * - Swimming: SWOLF efficiency score (Time in seconds + Stroke count)
 * - Strength: Repetition cadence & set pacing
 * - Rowing: Stroke rate per minute (SPM) and 500m split pace
 */
class MultiSportEngine {

    data class SwolfResult(
        val swolfScore: Int,
        val efficiencyRating: String
    )

    data class RowingSplit(
        val strokeRateSpm: Double,
        val split500mSeconds: Double
    )

    fun calculateSwolf(lapTimeSeconds: Int, strokeCount: Int): SwolfResult {
        val score = lapTimeSeconds + strokeCount
        val rating = when {
            score < 35 -> "Elite"
            score < 45 -> "Excellent"
            score < 55 -> "Good"
            else -> "Developing"
        }
        return SwolfResult(score, rating)
    }

    fun calculateRowingSplit(distanceMeters: Double, elapsedSeconds: Double, strokeCount: Int): RowingSplit {
        val spm = if (elapsedSeconds > 0.0) (strokeCount / elapsedSeconds) * 60.0 else 0.0
        val split500 = if (distanceMeters > 0.0) (elapsedSeconds / distanceMeters) * 500.0 else 0.0
        return RowingSplit(strokeRateSpm = spm, split500mSeconds = split500)
    }
}
