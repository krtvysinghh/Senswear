package com.senswear.app.core.domain.model

/**
 * Maintains a 7-day rolling median nocturnal skin temperature baseline and detects significant
 * fever or recovery deviations.
 */
class NocturnalTemperatureBaseline {

    data class TemperatureAnomaly(
        val baselineCelsius: Double,
        val currentNightCelsius: Double,
        val deviationCelsius: Double,
        val isSignificantAnomaly: Boolean,
        val indication: AnomalyIndication
    )

    enum class AnomalyIndication {
        NORMAL_VARIATION,
        ELEVATED_POSSIBLE_ILLNESS,
        DEPRESSED_PERIPHERAL_VASOCONSTRICTION
    }

    fun evaluateNightlyTemperature(
        pastSevenNightsCelsius: List<Double>,
        currentNightCelsius: Double
    ): TemperatureAnomaly {
        val baseline = if (pastSevenNightsCelsius.isNotEmpty()) {
            val sorted = pastSevenNightsCelsius.sorted()
            sorted[sorted.size / 2] // Median
        } else {
            36.5 // Standard human baseline
        }

        val deviation = currentNightCelsius - baseline
        val indication = when {
            deviation >= 0.70 -> AnomalyIndication.ELEVATED_POSSIBLE_ILLNESS
            deviation <= -0.80 -> AnomalyIndication.DEPRESSED_PERIPHERAL_VASOCONSTRICTION
            else -> AnomalyIndication.NORMAL_VARIATION
        }

        return TemperatureAnomaly(
            baselineCelsius = baseline,
            currentNightCelsius = currentNightCelsius,
            deviationCelsius = deviation,
            isSignificantAnomaly = indication != AnomalyIndication.NORMAL_VARIATION,
            indication = indication
        )
    }
}
