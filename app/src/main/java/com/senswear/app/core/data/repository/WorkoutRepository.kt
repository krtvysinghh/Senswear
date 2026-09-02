package com.senswear.app.core.data.repository

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.senswear.app.core.data.local.SenswearDatabase
import com.senswear.app.core.domain.model.DataSource
import com.senswear.app.core.domain.model.WorkoutSample
import com.senswear.app.core.domain.model.WorkoutSession
import com.senswear.app.core.domain.model.WorkoutType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WorkoutRepository(private val dbHelper: SenswearDatabase) {

    suspend fun getRecentWorkouts(): List<WorkoutSession> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        listOf(
            WorkoutSession(
                id = "workout_1",
                type = WorkoutType.OUTDOOR_WALK,
                startTimeEpochMs = now - (3 * 3600 * 1000L),
                endTimeEpochMs = now - (2 * 3600 * 1000L + 28 * 60 * 1000L),
                durationSeconds = 1920L, // 32m
                totalDistanceMeters = 3400.0,
                totalCaloriesKcal = 186,
                avgHeartRateBpm = 118,
                maxHeartRateBpm = 138,
                source = DataSource.PEBBLE_QORE_2_BLE
            ),
            WorkoutSession(
                id = "workout_2",
                type = WorkoutType.OUTDOOR_RUN,
                startTimeEpochMs = now - (26 * 3600 * 1000L),
                endTimeEpochMs = now - (25 * 3600 * 1000L + 15 * 60 * 1000L),
                durationSeconds = 2700L, // 45m
                totalDistanceMeters = 6800.0,
                totalCaloriesKcal = 512,
                avgHeartRateBpm = 152,
                maxHeartRateBpm = 174,
                source = DataSource.PEBBLE_QORE_2_BLE
            ),
            WorkoutSession(
                id = "workout_3",
                type = WorkoutType.HIIT,
                startTimeEpochMs = now - (50 * 3600 * 1000L),
                endTimeEpochMs = now - (49 * 3600 * 1000L + 30 * 60 * 1000L),
                durationSeconds = 1800L, // 30m
                totalDistanceMeters = 0.0,
                totalCaloriesKcal = 340,
                avgHeartRateBpm = 161,
                maxHeartRateBpm = 182,
                source = DataSource.PEBBLE_QORE_2_BLE
            )
        )
    }

    suspend fun saveWorkout(session: WorkoutSession) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("id", session.id)
            put("type", session.type.name)
            put("start_time", session.startTimeEpochMs)
            put("end_time", session.endTimeEpochMs)
            put("duration_seconds", session.durationSeconds)
            put("distance_meters", session.totalDistanceMeters)
            put("calories", session.totalCaloriesKcal)
            put("avg_hr", session.avgHeartRateBpm)
            put("max_hr", session.maxHeartRateBpm)
            put("source", session.source.name)
        }
        db.insertWithOnConflict("workouts", null, values, SQLiteDatabase.CONFLICT_REPLACE)
        dbHelper.notifyChanged()
    }
}
