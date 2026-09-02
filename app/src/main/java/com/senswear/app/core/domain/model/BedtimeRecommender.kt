package com.senswear.app.core.domain.model

data class BedtimeWindow(
    val idealBedtimeString: String,
    val targetWakeTimeString: String,
    val targetSleepDurationHours: Double
)

object BedtimeRecommender {
    fun calculateOptimalBedtime(desiredWakeHour: Int, desiredWakeMinute: Int, sleepGoalHours: Double = 8.0): BedtimeWindow {
        val totalWakeMins = desiredWakeHour * 60 + desiredWakeMinute
        val sleepDurationMins = (sleepGoalHours * 60).toInt()
        var bedtimeMins = totalWakeMins - sleepDurationMins - 15 // 15 min sleep latency

        if (bedtimeMins < 0) bedtimeMins += 1440

        val bedH = bedtimeMins / 60
        val bedM = bedtimeMins % 60
        val bedStr = String.format("%02d:%02d %s", if (bedH % 12 == 0) 12 else bedH % 12, bedM, if (bedH >= 12) "PM" else "AM")
        val wakeStr = String.format("%02d:%02d %s", if (desiredWakeHour % 12 == 0) 12 else desiredWakeHour % 12, desiredWakeMinute, if (desiredWakeHour >= 12) "PM" else "AM")

        return BedtimeWindow(
            idealBedtimeString = bedStr,
            targetWakeTimeString = wakeStr,
            targetSleepDurationHours = sleepGoalHours
        )
    }
}
