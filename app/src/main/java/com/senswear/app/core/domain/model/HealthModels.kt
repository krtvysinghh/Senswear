package com.senswear.app.core.domain.model

data class FitnessSnapshot(
    val timestampEpochMs: Long = System.currentTimeMillis(),
    val steps: Int = 0,
    val distanceMeters: Double = 0.0,
    val activeCaloriesKcal: Int = 0,
    val totalCaloriesKcal: Int = 0,
    val liveHeartRateBpm: Int? = null,
    val restingHeartRateBpm: Int? = null,
    val spo2Percent: Int? = null,
    val hrvRmssdMs: Int? = null,
    val stressScore: Int? = null,
    val skinTemperatureCelsius: Double? = null,
    val batteryPercent: Int = 100,
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED,
    val lastSyncEpochMs: Long = 0L,
    val activeWorkout: WorkoutSession? = null
)

data class DailyActivity(
    val epochDay: Long, // Epoch day
    val steps: Int,
    val stepGoal: Int = 10000,
    val distanceMeters: Double,
    val activeCaloriesKcal: Int,
    val totalCaloriesKcal: Int,
    val activeMinutes: Int,
    val hourlySteps: List<Int> = List(24) { 0 },
    val source: DataSource = DataSource.PEBBLE_QORE_2_BLE,
    val lastUpdatedEpochMs: Long = System.currentTimeMillis()
) {
    val stepProgressPercent: Float
        get() = if (stepGoal > 0) (steps.toFloat() / stepGoal.toFloat()).coerceIn(0f, 2f) else 0f

    val distanceKm: Double
        get() = distanceMeters / 1000.0
}

enum class HeartRateZone(val title: String, val rangeDescription: String, val minPct: Float, val maxPct: Float) {
    REST("Resting / Very Light", "< 50% max HR", 0f, 0.50f),
    ZONE_1("Zone 1 — Warm Up", "50% - 60% max HR", 0.50f, 0.60f),
    ZONE_2("Zone 2 — Fat Burn / Endurance", "60% - 70% max HR", 0.60f, 0.70f),
    ZONE_3("Zone 3 — Aerobic / Cardio", "70% - 80% max HR", 0.70f, 0.80f),
    ZONE_4("Zone 4 — Threshold", "80% - 90% max HR", 0.80f, 0.90f),
    ZONE_5("Zone 5 — Maximum Performance", "90% - 100% max HR", 0.90f, 1.0f);

    companion object {
        fun fromBpm(bpm: Int, maxHr: Int = 190): HeartRateZone {
            val pct = bpm.toFloat() / maxHr.coerceAtLeast(100).toFloat()
            return when {
                pct < 0.50f -> REST
                pct < 0.60f -> ZONE_1
                pct < 0.70f -> ZONE_2
                pct < 0.80f -> ZONE_3
                pct < 0.90f -> ZONE_4
                else -> ZONE_5
            }
        }
    }
}

data class HeartRateReading(
    val timestampEpochMs: Long,
    val bpm: Int,
    val restingHeartRateBpm: Int? = null,
    val source: DataSource = DataSource.PEBBLE_QORE_2_BLE
) {
    val zone: HeartRateZone
        get() = HeartRateZone.fromBpm(bpm)
}

data class Spo2Reading(
    val timestampEpochMs: Long,
    val percentage: Int, // 70..100
    val source: DataSource = DataSource.PEBBLE_QORE_2_BLE
)

data class HrvReading(
    val timestampEpochMs: Long,
    val rmssdMs: Int,
    val sdnnMs: Int? = null,
    val source: DataSource = DataSource.PEBBLE_QORE_2_BLE
)

enum class StressLevel(val label: String, val scoreRange: IntRange) {
    RELAXED("Relaxed", 0..25),
    LOW("Low Stress", 26..50),
    MEDIUM("Medium Stress", 51..75),
    HIGH("High Stress", 76..100);

    companion object {
        fun fromScore(score: Int): StressLevel = when (score) {
            in 0..25 -> RELAXED
            in 26..50 -> LOW
            in 51..75 -> MEDIUM
            else -> HIGH
        }
    }
}

data class StressReading(
    val timestampEpochMs: Long,
    val score: Int, // 0..100
    val source: DataSource = DataSource.PEBBLE_QORE_2_BLE
) {
    val level: StressLevel
        get() = StressLevel.fromScore(score)
}

data class TemperatureReading(
    val timestampEpochMs: Long,
    val temperatureCelsius: Double,
    val baselineDeltaCelsius: Double = 0.0,
    val source: DataSource = DataSource.PEBBLE_QORE_2_BLE
)

enum class SleepStageType(val label: String) {
    AWAKE("Awake"),
    LIGHT("Light Sleep"),
    DEEP("Deep Sleep"),
    REM("REM Sleep")
}

data class SleepStageRecord(
    val stage: SleepStageType,
    val startTimeEpochMs: Long,
    val endTimeEpochMs: Long
) {
    val durationMinutes: Int
        get() = ((endTimeEpochMs - startTimeEpochMs) / 60000).toInt().coerceAtLeast(0)
}

data class SleepSession(
    val id: String,
    val startTimeEpochMs: Long,
    val endTimeEpochMs: Long,
    val durationMinutes: Int,
    val deepMinutes: Int,
    val lightMinutes: Int,
    val remMinutes: Int,
    val awakeMinutes: Int,
    val sleepScore: Int, // 0..100
    val stages: List<SleepStageRecord> = emptyList(),
    val source: DataSource = DataSource.PEBBLE_QORE_2_BLE
) {
    val efficiencyPercent: Int
        get() = if (durationMinutes > 0) {
            (((durationMinutes - awakeMinutes).toFloat() / durationMinutes.toFloat()) * 100).toInt().coerceIn(0, 100)
        } else 0
}
