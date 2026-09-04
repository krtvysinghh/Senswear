package com.senswear.app.core.data.repository

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.senswear.app.core.data.local.SenswearDatabase
import com.senswear.app.core.domain.model.DataSource
import com.senswear.app.core.domain.model.WorkoutSession
import com.senswear.app.core.domain.model.WorkoutType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WorkoutRepository(private val dbHelper: SenswearDatabase) {

    suspend fun getRecentWorkouts(): List<WorkoutSession> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "workouts",
            null,
            null,
            null,
            null, null, "start_time DESC"
        )
        val list = mutableListOf<WorkoutSession>()
        cursor.use {
            while (it.moveToNext()) {
                val id = it.getString(it.getColumnIndexOrThrow("id"))
                val typeStr = it.getString(it.getColumnIndexOrThrow("type"))
                val type = try { WorkoutType.valueOf(typeStr) } catch (e: Exception) { WorkoutType.OUTDOOR_WALK }
                val start = it.getLong(it.getColumnIndexOrThrow("start_time"))
                val end = if (it.isNull(it.getColumnIndexOrThrow("end_time"))) null else it.getLong(it.getColumnIndexOrThrow("end_time"))
                val duration = it.getLong(it.getColumnIndexOrThrow("duration_seconds"))
                val distance = it.getDouble(it.getColumnIndexOrThrow("distance_meters"))
                val calories = it.getInt(it.getColumnIndexOrThrow("calories"))
                val avgHr = it.getInt(it.getColumnIndexOrThrow("avg_hr"))
                val maxHr = it.getInt(it.getColumnIndexOrThrow("max_hr"))
                val src = try { DataSource.valueOf(it.getString(it.getColumnIndexOrThrow("source"))) } catch (e: Exception) { DataSource.PEBBLE_QORE_2_BLE }

                list.add(
                    WorkoutSession(
                        id = id,
                        type = type,
                        startTimeEpochMs = start,
                        endTimeEpochMs = end,
                        durationSeconds = duration,
                        totalDistanceMeters = distance,
                        totalCaloriesKcal = calories,
                        avgHeartRateBpm = avgHr,
                        maxHeartRateBpm = maxHr,
                        source = src
                    )
                )
            }
        }
        list
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
