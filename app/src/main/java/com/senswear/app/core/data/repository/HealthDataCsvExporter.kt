package com.senswear.app.core.data.repository

import com.senswear.app.core.domain.model.DailyActivity
import com.senswear.app.core.domain.model.HeartRateReading

object HealthDataCsvExporter {
    fun exportDailyActivityCsv(activities: List<DailyActivity>): String {
        val sb = StringBuilder()
        sb.append("EpochDay,Steps,Goal,DistanceMeters,ActiveCaloriesKcal,TotalCaloriesKcal,ActiveMinutes,Source\n")
        for (a in activities) {
            sb.append("${a.epochDay},${a.steps},${a.stepGoal},${a.distanceMeters},${a.activeCaloriesKcal},${a.totalCaloriesKcal},${a.activeMinutes},${a.source.name}\n")
        }
        return sb.toString()
    }

    fun exportHeartRateCsv(readings: List<HeartRateReading>): String {
        val sb = StringBuilder()
        sb.append("TimestampEpochMs,BPM,RestingBPM,Source\n")
        for (r in readings) {
            sb.append("${r.timestampEpochMs},${r.bpm},${r.restingHeartRateBpm ?: ""},${r.source.name}\n")
        }
        return sb.toString()
    }
}
