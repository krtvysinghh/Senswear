package com.senswear.app.core.domain.model

data class OxygenDesaturationEvent(
    val timestampEpochMs: Long,
    val baselinePercent: Int,
    val lowestPercent: Int,
    val dropPercent: Int
)

object Spo2DesaturationDetector {
    fun detectDrops(readings: List<Spo2Reading>, dropThreshold: Int = 4): List<OxygenDesaturationEvent> {
        val events = mutableListOf<OxygenDesaturationEvent>()
        if (readings.size < 2) return events

        var baseline = readings.first().percentage
        for (r in readings) {
            val drop = baseline - r.percentage
            if (drop >= dropThreshold && r.percentage < 94) {
                events.add(
                    OxygenDesaturationEvent(
                        timestampEpochMs = r.timestampEpochMs,
                        baselinePercent = baseline,
                        lowestPercent = r.percentage,
                        dropPercent = drop
                    )
                )
            } else if (r.percentage >= 96) {
                baseline = r.percentage
            }
        }
        return events
    }
}
