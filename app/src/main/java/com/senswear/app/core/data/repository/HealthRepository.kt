package com.senswear.app.core.data.repository

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.senswear.app.core.data.local.SenswearDatabase
import com.senswear.app.core.domain.model.DataSource
import com.senswear.app.core.domain.model.HeartRateReading
import com.senswear.app.core.domain.model.HrvReading
import com.senswear.app.core.domain.model.Spo2Reading
import com.senswear.app.core.domain.model.StressReading
import com.senswear.app.core.domain.model.TemperatureReading
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HealthRepository(private val dbHelper: SenswearDatabase) {

    suspend fun saveHeartRate(reading: HeartRateReading) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("timestamp", reading.timestampEpochMs)
            put("bpm", reading.bpm)
            put("resting_bpm", reading.restingHeartRateBpm)
            put("source", reading.source.name)
        }
        db.insert("heart_rate_readings", null, values)
        dbHelper.notifyChanged()
    }

    suspend fun getHeartRateHistory(hours: Int = 24): List<HeartRateReading> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cutoff = System.currentTimeMillis() - (hours * 3600 * 1000L)
        val cursor = db.query(
            "heart_rate_readings",
            null,
            "timestamp >= ?",
            arrayOf(cutoff.toString()),
            null, null, "timestamp ASC"
        )

        val list = mutableListOf<HeartRateReading>()
        cursor.use {
            while (it.moveToNext()) {
                val time = it.getLong(it.getColumnIndexOrThrow("timestamp"))
                val bpm = it.getInt(it.getColumnIndexOrThrow("bpm"))
                val resting = if (it.isNull(it.getColumnIndexOrThrow("resting_bpm"))) null else it.getInt(it.getColumnIndexOrThrow("resting_bpm"))
                val src = try { DataSource.valueOf(it.getString(it.getColumnIndexOrThrow("source"))) } catch (e: Exception) { DataSource.PEBBLE_QORE_2_BLE }
                list.add(HeartRateReading(time, bpm, resting, src))
            }
        }
        list
    }

    suspend fun saveSpo2(reading: Spo2Reading) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("timestamp", reading.timestampEpochMs)
            put("percentage", reading.percentage)
            put("source", reading.source.name)
        }
        db.insert("spo2_readings", null, values)
        dbHelper.notifyChanged()
    }

    suspend fun getRecentSpo2Readings(days: Int = 7): List<Spo2Reading> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cutoff = System.currentTimeMillis() - (days * 86400 * 1000L)
        val cursor = db.query(
            "spo2_readings",
            null,
            "timestamp >= ?",
            arrayOf(cutoff.toString()),
            null, null, "timestamp ASC"
        )
        val list = mutableListOf<Spo2Reading>()
        cursor.use {
            while (it.moveToNext()) {
                val time = it.getLong(it.getColumnIndexOrThrow("timestamp"))
                val pct = it.getInt(it.getColumnIndexOrThrow("percentage"))
                val src = try { DataSource.valueOf(it.getString(it.getColumnIndexOrThrow("source"))) } catch (e: Exception) { DataSource.PEBBLE_QORE_2_BLE }
                list.add(Spo2Reading(time, pct, src))
            }
        }
        list
    }

    suspend fun saveHrv(reading: HrvReading) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("timestamp", reading.timestampEpochMs)
            put("rmssd", reading.rmssdMs)
            put("sdnn", reading.sdnnMs)
            put("source", reading.source.name)
        }
        db.insert("hrv_readings", null, values)
        dbHelper.notifyChanged()
    }

    suspend fun getRecentHrvReadings(days: Int = 7): List<HrvReading> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cutoff = System.currentTimeMillis() - (days * 86400 * 1000L)
        val cursor = db.query(
            "hrv_readings",
            null,
            "timestamp >= ?",
            arrayOf(cutoff.toString()),
            null, null, "timestamp ASC"
        )
        val list = mutableListOf<HrvReading>()
        cursor.use {
            while (it.moveToNext()) {
                val time = it.getLong(it.getColumnIndexOrThrow("timestamp"))
                val rmssd = it.getInt(it.getColumnIndexOrThrow("rmssd"))
                val sdnn = if (it.isNull(it.getColumnIndexOrThrow("sdnn"))) null else it.getInt(it.getColumnIndexOrThrow("sdnn"))
                val src = try { DataSource.valueOf(it.getString(it.getColumnIndexOrThrow("source"))) } catch (e: Exception) { DataSource.PEBBLE_QORE_2_BLE }
                list.add(HrvReading(time, rmssd, sdnn, src))
            }
        }
        list
    }

    suspend fun saveTemperature(reading: TemperatureReading) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("timestamp", reading.timestampEpochMs)
            put("temp_celsius", reading.temperatureCelsius)
            put("baseline_delta", reading.baselineDeltaCelsius)
            put("source", reading.source.name)
        }
        db.insert("temperature_readings", null, values)
        dbHelper.notifyChanged()
    }

    suspend fun getRecentTemperatureReadings(days: Int = 7): List<TemperatureReading> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cutoff = System.currentTimeMillis() - (days * 86400 * 1000L)
        val cursor = db.query(
            "temperature_readings",
            null,
            "timestamp >= ?",
            arrayOf(cutoff.toString()),
            null, null, "timestamp ASC"
        )
        val list = mutableListOf<TemperatureReading>()
        cursor.use {
            while (it.moveToNext()) {
                val time = it.getLong(it.getColumnIndexOrThrow("timestamp"))
                val temp = it.getDouble(it.getColumnIndexOrThrow("temp_celsius"))
                val delta = it.getDouble(it.getColumnIndexOrThrow("baseline_delta"))
                val src = try { DataSource.valueOf(it.getString(it.getColumnIndexOrThrow("source"))) } catch (e: Exception) { DataSource.PEBBLE_QORE_2_BLE }
                list.add(TemperatureReading(time, temp, delta, src))
            }
        }
        list
    }

    suspend fun saveStress(reading: StressReading) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("timestamp", reading.timestampEpochMs)
            put("score", reading.score)
            put("source", reading.source.name)
        }
        db.insert("stress_readings", null, values)
        dbHelper.notifyChanged()
    }

    suspend fun getRecentStressReadings(days: Int = 7): List<StressReading> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cutoff = System.currentTimeMillis() - (days * 86400 * 1000L)
        val cursor = db.query(
            "stress_readings",
            null,
            "timestamp >= ?",
            arrayOf(cutoff.toString()),
            null, null, "timestamp ASC"
        )
        val list = mutableListOf<StressReading>()
        cursor.use {
            while (it.moveToNext()) {
                val time = it.getLong(it.getColumnIndexOrThrow("timestamp"))
                val score = it.getInt(it.getColumnIndexOrThrow("score"))
                val src = try { DataSource.valueOf(it.getString(it.getColumnIndexOrThrow("source"))) } catch (e: Exception) { DataSource.PEBBLE_QORE_2_BLE }
                list.add(StressReading(time, score, src))
            }
        }
        list
    }
}
