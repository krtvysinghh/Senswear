package com.senswear.app.core.domain.model

/**
 * Models and compensates for cardiovascular drift during extended aerobic workouts (>45 min)
 * caused by thermoregulation, dehydration, and stroke volume reduction.
 */
class CardiovascularDriftCompensator {

    data class DriftCompensatedEffort(
        val rawHeartRateBpm: Int,
        val compensatedAerobicBpm: Int,
        val driftPercentage: Double,
        val adjustedZone: Int
    )

    /**
     * @param durationMinutes Elapsed workout duration in minutes.
     * @param rawBpm Current measured heart rate.
     * @param ambientTempCelsius Ambient temperature during workout (default 20°C).
     */
    fun compensate(
        durationMinutes: Double,
        rawBpm: Int,
        ambientTempCelsius: Double = 20.0
    ): DriftCompensatedEffort {
        if (durationMinutes <= 30.0) {
            return DriftCompensatedEffort(
                rawHeartRateBpm = rawBpm,
                compensatedAerobicBpm = rawBpm,
                driftPercentage = 0.0,
                adjustedZone = mapBpmToZone(rawBpm)
            )
        }

        // Drift accelerates after 30 minutes, amplified by heat
        val heatMultiplier = (ambientTempCelsius - 18.0).coerceAtLeast(0.0) * 0.002
        val driftFactor = ((durationMinutes - 30.0) * (0.0015 + heatMultiplier)).coerceIn(0.0, 0.15)
        val compensatedBpm = (rawBpm * (1.0 - driftFactor)).toInt()

        return DriftCompensatedEffort(
            rawHeartRateBpm = rawBpm,
            compensatedAerobicBpm = compensatedBpm,
            driftPercentage = driftFactor * 100.0,
            adjustedZone = mapBpmToZone(compensatedBpm)
        )
    }

    private fun mapBpmToZone(bpm: Int): Int {
        return when {
            bpm < 110 -> 1 // Warm-up
            bpm < 135 -> 2 // Aerobic Base
            bpm < 155 -> 3 // Tempo
            bpm < 175 -> 4 // Threshold
            else -> 5      // Anaerobic / VO2 Max
        }
    }
}
