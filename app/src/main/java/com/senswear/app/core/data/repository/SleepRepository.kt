package com.senswear.app.core.data.repository

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.senswear.app.core.data.local.SenswearDatabase
import com.senswear.app.core.domain.model.DataSource
import com.senswear.app.core.domain.model.SleepSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SleepRepository(private val dbHelper: SenswearDatabase) {

    suspend fun getLatestSleepSession(): SleepSession? = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "sleep_sessions",
            null,
            null,
            null,
            null, null, "start_time DESC", "1"
        )
        cursor.use {
            if (it.moveToFirst()) {
                val id = it.getString(it.getColumnIndexOrThrow("id"))
                val start = it.getLong(it.getColumnIndexOrThrow("start_time"))
                val end = it.getLong(it.getColumnIndexOrThrow("end_time"))
                val dur = it.getInt(it.getColumnIndexOrThrow("duration_minutes"))
                val deep = it.getInt(it.getColumnIndexOrThrow("deep_minutes"))
                val light = it.getInt(it.getColumnIndexOrThrow("light_minutes"))
                val rem = it.getInt(it.getColumnIndexOrThrow("rem_minutes"))
                val awake = it.getInt(it.getColumnIndexOrThrow("awake_minutes"))
                val score = it.getInt(it.getColumnIndexOrThrow("sleep_score"))
                val src = try { DataSource.valueOf(it.getString(it.getColumnIndexOrThrow("source"))) } catch (e: Exception) { DataSource.PEBBLE_QORE_2_BLE }
                SleepSession(id, start, end, dur, deep, light, rem, awake, score, emptyList(), src)
            } else null
        }
    }

    suspend fun getRecentSleepSessions(days: Int = 7): List<SleepSession> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cutoff = System.currentTimeMillis() - (days * 86400 * 1000L)
        val cursor = db.query(
            "sleep_sessions",
            null,
            "start_time >= ?",
            arrayOf(cutoff.toString()),
            null, null, "start_time DESC"
        )
        val list = mutableListOf<SleepSession>()
        cursor.use {
            while (it.moveToNext()) {
                val id = it.getString(it.getColumnIndexOrThrow("id"))
                val start = it.getLong(it.getColumnIndexOrThrow("start_time"))
                val end = it.getLong(it.getColumnIndexOrThrow("end_time"))
                val dur = it.getInt(it.getColumnIndexOrThrow("duration_minutes"))
                val deep = it.getInt(it.getColumnIndexOrThrow("deep_minutes"))
                val light = it.getInt(it.getColumnIndexOrThrow("light_minutes"))
                val rem = it.getInt(it.getColumnIndexOrThrow("rem_minutes"))
                val awake = it.getInt(it.getColumnIndexOrThrow("awake_minutes"))
                val score = it.getInt(it.getColumnIndexOrThrow("sleep_score"))
                val src = try { DataSource.valueOf(it.getString(it.getColumnIndexOrThrow("source"))) } catch (e: Exception) { DataSource.PEBBLE_QORE_2_BLE }
                list.add(SleepSession(id, start, end, dur, deep, light, rem, awake, score, emptyList(), src))
            }
        }
        list
    }

    suspend fun saveSleepSession(session: SleepSession) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("id", session.id)
            put("start_time", session.startTimeEpochMs)
            put("end_time", session.endTimeEpochMs)
            put("duration_minutes", session.durationMinutes)
            put("deep_minutes", session.deepMinutes)
            put("light_minutes", session.lightMinutes)
            put("rem_minutes", session.remMinutes)
            put("awake_minutes", session.awakeMinutes)
            put("sleep_score", session.sleepScore)
            put("source", session.source.name)
        }
        db.insertWithOnConflict("sleep_sessions", null, values, SQLiteDatabase.CONFLICT_REPLACE)
        dbHelper.notifyChanged()
    }
}
