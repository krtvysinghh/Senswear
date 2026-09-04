package com.senswear.app.core.data.repository

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.senswear.app.core.data.local.SenswearDatabase
import com.senswear.app.core.domain.model.DailyActivity
import com.senswear.app.core.domain.model.DataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ActivityRepository(private val dbHelper: SenswearDatabase) {

    suspend fun getTodayActivity(): DailyActivity? = withContext(Dispatchers.IO) {
        val todayEpoch = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
        getActivityForDay(todayEpoch)
    }

    suspend fun getActivityForDay(epochDay: Long): DailyActivity? = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "daily_activity",
            null,
            "epoch_day = ?",
            arrayOf(epochDay.toString()),
            null, null, null
        )

        cursor.use {
            if (it.moveToFirst()) {
                val steps = it.getInt(it.getColumnIndexOrThrow("steps"))
                val stepGoal = it.getInt(it.getColumnIndexOrThrow("step_goal"))
                val distance = it.getDouble(it.getColumnIndexOrThrow("distance_meters"))
                val activeCal = it.getInt(it.getColumnIndexOrThrow("active_calories"))
                val totalCal = it.getInt(it.getColumnIndexOrThrow("total_calories"))
                val activeMin = it.getInt(it.getColumnIndexOrThrow("active_minutes"))
                val hourlyCsv = it.getString(it.getColumnIndexOrThrow("hourly_steps"))
                val sourceStr = it.getString(it.getColumnIndexOrThrow("source"))
                val updated = it.getLong(it.getColumnIndexOrThrow("last_updated"))

                val hourlyList = hourlyCsv.split(",").mapNotNull { s -> s.trim().toIntOrNull() }
                val finalHourly = if (hourlyList.size == 24) hourlyList else List(24) { 0 }

                val source = try { DataSource.valueOf(sourceStr) } catch (e: Exception) { DataSource.PEBBLE_QORE_2_BLE }

                DailyActivity(
                    epochDay = epochDay,
                    steps = steps,
                    stepGoal = stepGoal,
                    distanceMeters = distance,
                    activeCaloriesKcal = activeCal,
                    totalCaloriesKcal = totalCal,
                    activeMinutes = activeMin,
                    hourlySteps = finalHourly,
                    source = source,
                    lastUpdatedEpochMs = updated
                )
            } else null
        }
    }

    suspend fun saveDailyActivity(activity: DailyActivity) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("epoch_day", activity.epochDay)
            put("steps", activity.steps)
            put("step_goal", activity.stepGoal)
            put("distance_meters", activity.distanceMeters)
            put("active_calories", activity.activeCaloriesKcal)
            put("total_calories", activity.totalCaloriesKcal)
            put("active_minutes", activity.activeMinutes)
            put("hourly_steps", activity.hourlySteps.joinToString(","))
            put("source", activity.source.name)
            put("last_updated", activity.lastUpdatedEpochMs)
        }
        db.insertWithOnConflict("daily_activity", null, values, SQLiteDatabase.CONFLICT_REPLACE)
        dbHelper.notifyChanged()
    }

    suspend fun getRecentActivities(daysCount: Int = 7): List<DailyActivity> = withContext(Dispatchers.IO) {
        val currentEpoch = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
        val list = mutableListOf<DailyActivity>()
        for (i in 0 until daysCount) {
            val day = currentEpoch - i
            val act = getActivityForDay(day)
            if (act != null) {
                list.add(act)
            }
        }
        list
    }
}
