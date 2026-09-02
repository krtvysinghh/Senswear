package com.senswear.app.core.data.local.entity

import com.senswear.app.core.domain.model.DataSource
import com.senswear.app.core.domain.model.HeartRateZone
import com.senswear.app.core.domain.model.StressLevel
import com.senswear.app.core.domain.model.WorkoutType

data class DailyActivityEntity(
    val epochDay: Long,
    val steps: Int,
    val stepGoal: Int,
    val distanceMeters: Double,
    val activeCaloriesKcal: Int,
    val totalCaloriesKcal: Int,
    val activeMinutes: Int,
    val hourlyStepsCsv: String,
    val sourceName: String,
    val lastUpdatedEpochMs: Long
)

data class HeartRateReadingEntity(
    val id: Long = 0L,
    val timestampEpochMs: Long,
    val bpm: Int,
    val restingHeartRateBpm: Int?,
    val sourceName: String
)

data class Spo2ReadingEntity(
    val id: Long = 0L,
    val timestampEpochMs: Long,
    val percentage: Int,
    val sourceName: String
)

data class HrvReadingEntity(
    val id: Long = 0L,
    val timestampEpochMs: Long,
    val rmssdMs: Int,
    val sdnnMs: Int?,
    val sourceName: String
)

data class StressReadingEntity(
    val id: Long = 0L,
    val timestampEpochMs: Long,
    val score: Int,
    val sourceName: String
)

data class TemperatureReadingEntity(
    val id: Long = 0L,
    val timestampEpochMs: Long,
    val temperatureCelsius: Double,
    val baselineDeltaCelsius: Double,
    val sourceName: String
)

data class SleepSessionEntity(
    val id: String,
    val startTimeEpochMs: Long,
    val endTimeEpochMs: Long,
    val durationMinutes: Int,
    val deepMinutes: Int,
    val lightMinutes: Int,
    val remMinutes: Int,
    val awakeMinutes: Int,
    val sleepScore: Int,
    val sourceName: String
)

data class WorkoutSessionEntity(
    val id: String,
    val typeName: String,
    val startTimeEpochMs: Long,
    val endTimeEpochMs: Long?,
    val durationSeconds: Long,
    val totalDistanceMeters: Double,
    val totalCaloriesKcal: Int,
    val avgHeartRateBpm: Int,
    val maxHeartRateBpm: Int,
    val sourceName: String
)

data class GoalEntity(
    val id: String,
    val title: String,
    val targetValue: Double,
    val unit: String,
    val currentValue: Double,
    val typeName: String
)

data class AchievementEntity(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val isUnlocked: Boolean,
    val unlockedEpochMs: Long?,
    val progressPercent: Int,
    val iconName: String
)
