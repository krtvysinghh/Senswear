package com.senswear.app.core.data.repository

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.senswear.app.core.data.local.SenswearDatabase
import com.senswear.app.core.domain.model.Achievement
import com.senswear.app.core.domain.model.BatteryState
import com.senswear.app.core.domain.model.ConnectionState
import com.senswear.app.core.domain.model.Goal
import com.senswear.app.core.domain.model.GoalType
import com.senswear.app.core.domain.model.HealthInsight
import com.senswear.app.core.domain.model.InsightSeverity
import com.senswear.app.core.domain.model.WearableDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GoalRepository(private val dbHelper: SenswearDatabase) {

    suspend fun getGoals(): List<Goal> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.query("goals", null, null, null, null, null, null)
        val list = mutableListOf<Goal>()
        cursor.use {
            while (it.moveToNext()) {
                val id = it.getString(it.getColumnIndexOrThrow("id"))
                val title = it.getString(it.getColumnIndexOrThrow("title"))
                val target = it.getDouble(it.getColumnIndexOrThrow("target_value"))
                val unit = it.getString(it.getColumnIndexOrThrow("unit"))
                val curr = it.getDouble(it.getColumnIndexOrThrow("current_value"))
                val typeStr = it.getString(it.getColumnIndexOrThrow("type"))
                val type = try { GoalType.valueOf(typeStr) } catch (e: Exception) { GoalType.DAILY_STEPS }
                list.add(Goal(id, title, target, unit, curr, type))
            }
        }
        list
    }

    suspend fun updateGoal(id: String, newTarget: Double) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("target_value", newTarget)
        }
        db.update("goals", values, "id = ?", arrayOf(id))
        dbHelper.notifyChanged()
    }
}

class AchievementRepository(private val dbHelper: SenswearDatabase) {

    suspend fun getAchievements(): List<Achievement> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.query("achievements", null, null, null, null, null, null)
        val list = mutableListOf<Achievement>()
        cursor.use {
            while (it.moveToNext()) {
                val id = it.getString(it.getColumnIndexOrThrow("id"))
                val title = it.getString(it.getColumnIndexOrThrow("title"))
                val desc = it.getString(it.getColumnIndexOrThrow("description"))
                val cat = it.getString(it.getColumnIndexOrThrow("category"))
                val unlocked = it.getInt(it.getColumnIndexOrThrow("is_unlocked")) == 1
                val unlockedTime = if (it.isNull(it.getColumnIndexOrThrow("unlocked_time"))) null else it.getLong(it.getColumnIndexOrThrow("unlocked_time"))
                val progress = it.getInt(it.getColumnIndexOrThrow("progress_percent"))
                val icon = it.getString(it.getColumnIndexOrThrow("icon"))
                list.add(Achievement(id, title, desc, cat, unlocked, unlockedTime, progress, icon))
            }
        }
        list
    }
}

class InsightsRepository {

    fun generateInsights(stepGoalPct: Float, sleepMinutes: Int, restingHr: Int): List<HealthInsight> {
        val list = mutableListOf<HealthInsight>()

        if (stepGoalPct >= 0.8f) {
            list.add(
                HealthInsight(
                    id = "ins_activity",
                    title = "Activity Momentum",
                    message = "You are at ${(stepGoalPct * 100).toInt()}% of your step goal today. Outstanding daily consistency.",
                    category = "Activity",
                    severity = InsightSeverity.POSITIVE
                )
            )
        }

        if (sleepMinutes >= 420) {
            list.add(
                HealthInsight(
                    id = "ins_sleep",
                    title = "Optimal Recovery",
                    message = "Your total sleep duration reached ${sleepMinutes / 60}h ${sleepMinutes % 60}m with strong deep sleep architecture.",
                    category = "Sleep",
                    severity = InsightSeverity.POSITIVE
                )
            )
        }

        if (restingHr in 55..70) {
            list.add(
                HealthInsight(
                    id = "ins_hr",
                    title = "Stable Cardiovascular Baseline",
                    message = "Resting heart rate remains at a calm $restingHr BPM, indicating balanced autonomic tone.",
                    category = "Heart Rate",
                    severity = InsightSeverity.NEUTRAL
                )
            )
        }

        return list
    }
}
