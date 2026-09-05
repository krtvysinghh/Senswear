package com.senswear.app.core.domain.model

/**
 * Detects heart rate zone transitions during guided workouts to trigger audio cues and haptic pulses.
 */
class HeartRateZoneAnnouncer {

    data class ZoneTransition(
        val previousZone: Int,
        val newZone: Int,
        val transitionName: String,
        val shouldAlertUser: Boolean
    )

    private var currentZone: Int? = null

    fun determineZone(bpm: Int, maxHr: Int = 190): Int {
        val pct = (bpm.toDouble() / maxHr.toDouble()) * 100.0
        return when {
            pct < 60.0 -> 1 // Active Recovery
            pct < 70.0 -> 2 // Aerobic Endurance
            pct < 80.0 -> 3 // Tempo
            pct < 90.0 -> 4 // Threshold
            else -> 5       // Anaerobic / VO2 Max
        }
    }

    fun onPulseUpdate(bpm: Int, maxHr: Int = 190): ZoneTransition? {
        val newZone = determineZone(bpm, maxHr)
        val prev = currentZone

        if (prev == null) {
            currentZone = newZone
            return null
        }

        return if (prev != newZone) {
            currentZone = newZone
            ZoneTransition(
                previousZone = prev,
                newZone = newZone,
                transitionName = "Entering Zone $newZone",
                shouldAlertUser = true
            )
        } else {
            null
        }
    }

    fun reset() {
        currentZone = null
    }
}
