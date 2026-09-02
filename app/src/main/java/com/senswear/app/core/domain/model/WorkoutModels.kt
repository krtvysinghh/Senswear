package com.senswear.app.core.domain.model

enum class WorkoutType(val displayName: String, val iconName: String, val baseMet: Double) {
    OUTDOOR_WALK("Outdoor Walk", "directions_walk", 3.8),
    OUTDOOR_RUN("Outdoor Run", "directions_run", 8.5),
    CYCLING("Outdoor Cycling", "directions_bike", 7.0),
    STRENGTH_TRAINING("Strength Training", "fitness_center", 5.0),
    HIIT("HIIT Interval", "bolt", 8.0),
    YOGA("Yoga & Mobility", "self_improvement", 2.5),
    SWIMMING("Pool Swimming", "pool", 6.0),
    ROWING("Rowing Machine", "rowing", 6.8),
    ELLIPTICAL("Elliptical", "transfer_within_a_station", 5.5),
    OTHER("Freestyle Workout", "sports", 4.0)
}

data class WorkoutSample(
    val timestampEpochMs: Long,
    val heartRateBpm: Int,
    val speedKmh: Double = 0.0,
    val paceMinPerKm: Double = 0.0,
    val cadenceRpm: Int? = null,
    val currentDistanceMeters: Double = 0.0,
    val caloriesAccumulated: Int = 0
)

data class WorkoutSession(
    val id: String,
    val type: WorkoutType,
    val startTimeEpochMs: Long,
    val endTimeEpochMs: Long? = null,
    val durationSeconds: Long = 0L,
    val totalDistanceMeters: Double = 0.0,
    val totalCaloriesKcal: Int = 0,
    val avgHeartRateBpm: Int = 0,
    val maxHeartRateBpm: Int = 0,
    val samples: List<WorkoutSample> = emptyList(),
    val isLive: Boolean = false,
    val source: DataSource = DataSource.PEBBLE_QORE_2_BLE
) {
    val distanceKm: Double
        get() = totalDistanceMeters / 1000.0

    val paceString: String
        get() = if (totalDistanceMeters > 50 && durationSeconds > 10) {
            val totalMinutes = durationSeconds / 60.0
            val totalKm = totalDistanceMeters / 1000.0
            val minPerKm = totalMinutes / totalKm
            val mins = minPerKm.toInt()
            val secs = ((minPerKm - mins) * 60).toInt()
            String.format("%d'%02d\" /km", mins, secs)
        } else "--'--\" /km"
}
