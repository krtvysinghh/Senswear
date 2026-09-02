package com.senswear.app.core.domain.model

data class TemperatureAnomaly(
    val hasElevatedTemp: Boolean,
    val deltaCelsius: Double,
    val insightMessage: String?
)

object TemperatureAnomalyDetector {
    fun evaluate(currentTempCelsius: Double, baselineTempCelsius: Double = 36.6): TemperatureAnomaly {
        val delta = currentTempCelsius - baselineTempCelsius
        val roundedDelta = Math.round(delta * 10.0) / 10.0

        val hasElevated = roundedDelta >= 0.8
        val msg = if (hasElevated) {
            "Nocturnal skin temperature is +${roundedDelta}°C above your 7-day baseline, which may indicate immune activation or overtraining."
        } else null

        return TemperatureAnomaly(
            hasElevatedTemp = hasElevated,
            deltaCelsius = roundedDelta,
            insightMessage = msg
        )
    }
}
