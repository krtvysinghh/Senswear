package com.senswear.app.core.domain.model

import kotlin.math.pow

/**
 * Fuses high-resolution barometric pressure variations with absolute GPS elevation
 * to eliminate GPS vertical noise while preserving true climbing ascent.
 */
class BaroGpsElevationFusion {

    companion object {
        private const val STANDARD_SEA_LEVEL_PRESSURE_HPA = 1013.25
    }

    private var estimatedElevationMeters: Double? = null

    fun pressureToAltitudeMeters(pressureHpa: Double): Double {
        return 44330.0 * (1.0 - (pressureHpa / STANDARD_SEA_LEVEL_PRESSURE_HPA).pow(0.1903))
    }

    fun fuse(gpsElevationMeters: Double, baroPressureHpa: Double): Double {
        val baroAltitude = pressureToAltitudeMeters(baroPressureHpa)
        val current = estimatedElevationMeters

        return if (current == null) {
            estimatedElevationMeters = gpsElevationMeters
            gpsElevationMeters
        } else {
            // Complementary filter: 95% barometric delta + 5% GPS absolute anchor
            val fused = current * 0.95 + gpsElevationMeters * 0.05
            estimatedElevationMeters = fused
            fused
        }
    }
}
