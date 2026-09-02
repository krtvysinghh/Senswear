package com.senswear.app.core.healthconnect

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.BodyTemperatureRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.senswear.app.core.domain.model.DailyActivity
import com.senswear.app.core.domain.model.DataSource
import com.senswear.app.core.domain.model.HeartRateReading
import com.senswear.app.core.domain.model.HrvReading
import com.senswear.app.core.domain.model.SleepSession
import com.senswear.app.core.domain.model.Spo2Reading
import com.senswear.app.core.domain.model.TemperatureReading
import com.senswear.app.core.domain.model.WorkoutSession
import java.time.Instant
import java.time.temporal.ChronoUnit

class HealthConnectManager(private val context: Context) {

    val healthConnectClient: HealthConnectClient? by lazy {
        if (isAvailable()) {
            HealthConnectClient.getOrCreate(context)
        } else {
            null
        }
    }

    val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getWritePermission(StepsRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getWritePermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(RestingHeartRateRecord::class),
        HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class),
        HealthPermission.getReadPermission(OxygenSaturationRecord::class),
        HealthPermission.getWritePermission(OxygenSaturationRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getWritePermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(BodyTemperatureRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(DistanceRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getWritePermission(ExerciseSessionRecord::class)
    )

    fun isAvailable(): Boolean {
        val status = HealthConnectClient.getSdkStatus(context)
        return status == HealthConnectClient.SDK_AVAILABLE
    }

    suspend fun hasAllPermissions(): Boolean {
        val client = healthConnectClient ?: return false
        val granted = client.permissionController.getGrantedPermissions()
        return granted.containsAll(permissions)
    }

    suspend fun readTodayActivity(): DailyActivity? {
        val client = healthConnectClient ?: return null
        return try {
            val now = Instant.now()
            val startOfDay = now.truncatedTo(ChronoUnit.DAYS)
            val timeFilter = TimeRangeFilter.between(startOfDay, now)

            val stepsResponse = client.readRecords(
                ReadRecordsRequest(
                    recordType = StepsRecord::class,
                    timeRangeFilter = timeFilter
                )
            )

            val totalSteps = stepsResponse.records.sumOf { it.count }

            val distanceResponse = client.readRecords(
                ReadRecordsRequest(
                    recordType = DistanceRecord::class,
                    timeRangeFilter = timeFilter
                )
            )
            val totalDistanceMeters = distanceResponse.records.sumOf { it.distance.inMeters }

            val caloriesResponse = client.readRecords(
                ReadRecordsRequest(
                    recordType = TotalCaloriesBurnedRecord::class,
                    timeRangeFilter = timeFilter
                )
            )
            val totalCalories = caloriesResponse.records.sumOf { it.energy.inKilocalories.toInt() }

            DailyActivity(
                epochDay = System.currentTimeMillis() / (1000 * 60 * 60 * 24),
                steps = totalSteps.toInt(),
                distanceMeters = totalDistanceMeters,
                activeCaloriesKcal = (totalSteps * 0.04).toInt(),
                totalCaloriesKcal = if (totalCalories > 0) totalCalories else (1600 + (totalSteps * 0.04).toInt()),
                activeMinutes = (totalSteps / 100).toInt(),
                source = DataSource.HEALTH_CONNECT
            )
        } catch (e: Exception) {
            null
        }
    }

    suspend fun readHeartRateHistory(startTime: Instant, endTime: Instant): List<HeartRateReading> {
        val client = healthConnectClient ?: return emptyList()
        return try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = HeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            response.records.flatMap { record ->
                record.samples.map { sample ->
                    HeartRateReading(
                        timestampEpochMs = sample.time.toEpochMilli(),
                        bpm = sample.beatsPerMinute.toInt(),
                        source = DataSource.HEALTH_CONNECT
                    )
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun readSleepSessions(startTime: Instant, endTime: Instant): List<SleepSession> {
        val client = healthConnectClient ?: return emptyList()
        return try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = SleepSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            response.records.map { record ->
                val durationMin = ChronoUnit.MINUTES.between(record.startTime, record.endTime).toInt()
                val deep = (durationMin * 0.20).toInt()
                val rem = (durationMin * 0.22).toInt()
                val awake = (durationMin * 0.06).toInt()
                val light = (durationMin - deep - rem - awake).coerceAtLeast(0)

                SleepSession(
                    id = record.metadata.id,
                    startTimeEpochMs = record.startTime.toEpochMilli(),
                    endTimeEpochMs = record.endTime.toEpochMilli(),
                    durationMinutes = durationMin,
                    deepMinutes = deep,
                    lightMinutes = light,
                    remMinutes = rem,
                    awakeMinutes = awake,
                    sleepScore = (((deep * 1.5 + rem * 1.2 + light * 0.8) / durationMin.coerceAtLeast(1)) * 90).toInt().coerceIn(40, 99),
                    source = DataSource.HEALTH_CONNECT
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
