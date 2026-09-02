package com.senswear.app.core.domain.model

data class ReadinessScore(
    val score: Int, // 0..100
    val advice: String,
    val readinessState: String
)

object ReadinessScoreCalculator {
    fun calculate(hrvRmssd: Int, sleepScore: Int, restingHr: Int): ReadinessScore {
        val hrvNorm = (hrvRmssd.toDouble() / 60.0).coerceIn(0.4, 1.3)
        val sleepNorm = (sleepScore.toDouble() / 100.0).coerceIn(0.4, 1.0)
        val hrNorm = if (restingHr in 50..70) 1.0 else 0.8

        val raw = (hrvNorm * 40.0 + sleepNorm * 40.0 + hrNorm * 20.0).toInt().coerceIn(30, 99)

        val (state, advice) = when {
            raw >= 85 -> "Prime Readiness" to "Body is primed for peak exertion, interval workouts, or maximum athletic intensity."
            raw >= 70 -> "Optimal Recovery" to "Normal physiological recovery. Proceed with regular endurance or strength training."
            else -> "Active Recovery Advised" to "Elevated autonomic stress. Consider light mobility, restorative walking, and extra sleep."
        }

        return ReadinessScore(score = raw, advice = advice, readinessState = state)
    }
}
