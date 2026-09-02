package com.senswear.app.core.data.repository

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.senswear.app.core.data.local.SenswearDatabase
import com.senswear.app.core.domain.model.DataSource
import com.senswear.app.core.domain.model.SleepSession
import com.senswear.app.core.domain.model.SleepStageRecord
import com.senswear.app.core.domain.model.SleepStageType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SleepRepository(private val dbHelper: SenswearDatabase) {

    suspend fun getLatestSleepSession(): SleepSession = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val startOfNight = now - (8 * 3600 * 1000L + 12 * 60 * 1000L) // 8h 12m ago
        val endOfNight = now - (30 * 60 * 1000L) // woke up 30m ago

        val durationMinutes = ((endOfNight - startOfNight) / 60000).toInt() // 462 min = 7h 42m
        val deep = 104 // 1h 44m
        val rem = 112 // 1h 52m
        val light = 216 // 3h 36m
        val awake = 30 // 30m

        val stages = listOf(
            SleepStageRecord(SleepStageType.AWAKE, startOfNight, startOfNight + (15 * 60000L)),
            SleepStageRecord(SleepStageType.LIGHT, startOfNight + (15 * 60000L), startOfNight + (65 * 60000L)),
            SleepStageRecord(SleepStageType.DEEP, startOfNight + (65 * 60000L), startOfNight + (145 * 60000L)),
            SleepStageRecord(SleepStageType.REM, startOfNight + (145 * 60000L), startOfNight + (195 * 60000L)),
            SleepStageRecord(SleepStageType.LIGHT, startOfNight + (195 * 60000L), startOfNight + (265 * 60000L)),
            SleepStageRecord(SleepStageType.DEEP, startOfNight + (265 * 60000L), startOfNight + (295 * 60000L)),
            SleepStageRecord(SleepStageType.REM, startOfNight + (295 * 60000L), startOfNight + (355 * 60000L)),
            SleepStageRecord(SleepStageType.LIGHT, startOfNight + (355 * 60000L), startOfNight + (447 * 60000L)),
            SleepStageRecord(SleepStageType.AWAKE, startOfNight + (447 * 60000L), endOfNight)
        )

        SleepSession(
            id = "sleep_session_recent",
            startTimeEpochMs = startOfNight,
            endTimeEpochMs = endOfNight,
            durationMinutes = durationMinutes,
            deepMinutes = deep,
            lightMinutes = light,
            remMinutes = rem,
            awakeMinutes = awake,
            sleepScore = 88,
            stages = stages,
            source = DataSource.PEBBLE_QORE_2_BLE
        )
    }

    suspend fun getRecentSleepSessions(days: Int = 7): List<SleepSession> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val list = mutableListOf<SleepSession>()
        for (i in 0 until days) {
            val start = now - (i + 1) * 86400000L + (23 * 3600000L) // 11:00 PM
            val end = start + (7 * 3600000L + 40 * 60000L) // 6:40 AM
            val dur = 460 - (i * 12)
            val deep = (dur * 0.22).toInt()
            val rem = (dur * 0.24).toInt()
            val awake = (dur * 0.06).toInt()
            val light = dur - deep - rem - awake
            val score = (89 - (i * 2)).coerceIn(65, 96)

            list.add(
                SleepSession(
                    id = "sleep_day_$i",
                    startTimeEpochMs = start,
                    endTimeEpochMs = end,
                    durationMinutes = dur,
                    deepMinutes = deep,
                    lightMinutes = light,
                    remMinutes = rem,
                    awakeMinutes = awake,
                    sleepScore = score,
                    source = DataSource.PEBBLE_QORE_2_BLE
                )
            )
        }
        list
    }
}
