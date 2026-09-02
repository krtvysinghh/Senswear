package com.senswear.app.core.domain.model

data class Vo2MaxResult(
    val scoreMlKgMin: Double,
    val fitnessCategory: String
)

object Vo2MaxEstimator {
    fun estimateFromRestingHeartRate(restingBpm: Int, maxBpm: Int = 190): Vo2MaxResult {
        val resting = restingBpm.coerceIn(40, 100).toDouble()
        val max = maxBpm.coerceIn(140, 220).toDouble()
        val vo2 = 15.3 * (max / resting)
        val rounded = Math.round(vo2 * 10.0) / 10.0

        val category = when {
            rounded >= 55.0 -> "Superior (Elite)"
            rounded >= 48.0 -> "Excellent"
            rounded >= 42.0 -> "Good"
            rounded >= 35.0 -> "Fair"
            else -> "Needs Attention"
        }

        return Vo2MaxResult(scoreMlKgMin = rounded, fitnessCategory = category)
    }
}
