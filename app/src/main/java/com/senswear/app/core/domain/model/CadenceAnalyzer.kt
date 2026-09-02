package com.senswear.app.core.domain.model

data class CadenceAnalysis(
    val currentSpm: Int,
    val averageSpm: Int,
    val peakSpm: Int,
    val cadenceZone: String
)

object CadenceAnalyzer {
    fun analyze(stepSamples: List<Int>, durationMinutes: Int): CadenceAnalysis {
        val totalSteps = stepSamples.sum()
        val avg = if (durationMinutes > 0) totalSteps / durationMinutes else 0
        val peak = stepSamples.maxOrNull() ?: 0

        val zone = when {
            avg >= 175 -> "Optimal Running Cadence (175+ SPM)"
            avg >= 155 -> "Moderate Jogging (155–174 SPM)"
            avg >= 115 -> "Brisk Walk (115–154 SPM)"
            else -> "Casual Walk (< 115 SPM)"
        }

        return CadenceAnalysis(
            currentSpm = stepSamples.lastOrNull() ?: avg,
            averageSpm = avg,
            peakSpm = peak,
            cadenceZone = zone
        )
    }
}
