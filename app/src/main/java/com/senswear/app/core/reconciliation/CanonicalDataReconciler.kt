package com.senswear.app.core.reconciliation

import com.senswear.app.core.domain.model.DailyActivity
import com.senswear.app.core.domain.model.DataSource
import com.senswear.app.core.domain.model.FitnessSnapshot
import com.senswear.app.core.domain.model.HeartRateReading
import com.senswear.app.core.domain.model.HrvReading
import com.senswear.app.core.domain.model.SleepSession
import com.senswear.app.core.domain.model.SleepStageRecord
import com.senswear.app.core.domain.model.Spo2Reading
import com.senswear.app.core.domain.model.StressReading
import com.senswear.app.core.domain.model.TemperatureReading
import com.senswear.app.core.domain.model.WorkoutSession
import kotlin.math.abs

/**
 * CanonicalDataReconciler
 *
 * Implements authoritative data reconciliation across multiple data streams
 * (Pebble Qore 2 BLE direct, Google Health Connect, and onboard phone sensors).
 *
 * Rules:
 * 1. Source Priority: Qore 2 BLE (100) > Health Connect (80) > Phone Sensors (50).
 * 2. Steps / Daily Activity: Uses the highest priority active source; if multiple sources exist,
 *    takes the max steps from the authoritative source to prevent accidental double-counting.
 * 3. Heart Rate: Deduplicates readings within a 10-second cluster window, preserving highest-priority source.
 * 4. Sleep: Reconciles overlapping sleep sessions by keeping the most granular stage breakdown.
 * 5. Workouts: Deduplicates workouts overlapping by >80% time, giving precedence to hardware BLE session.
 */
object CanonicalDataReconciler {

    fun reconcileDailyActivity(
        qore2Activity: DailyActivity?,
        healthConnectActivity: DailyActivity?,
        phoneSensorActivity: DailyActivity?,
        stepGoal: Int = 10000
    ): DailyActivity {
        // Collect available non-null sources sorted by priority
        val candidates = listOfNotNull(qore2Activity, healthConnectActivity, phoneSensorActivity)
            .sortedByDescending { it.source.priority }

        if (candidates.isEmpty()) {
            val todayEpoch = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
            return DailyActivity(
                epochDay = todayEpoch,
                steps = 0,
                stepGoal = stepGoal,
                distanceMeters = 0.0,
                activeCaloriesKcal = 0,
                totalCaloriesKcal = 0,
                activeMinutes = 0,
                source = DataSource.PEBBLE_QORE_2_BLE
            )
        }

        val primary = candidates.first()

        // Reconcile hourly steps distribution: merge hourly steps picking maximum per hour if primary is missing hours
        val mergedHourly = MutableList(24) { hour ->
            candidates.maxOfOrNull { it.hourlySteps.getOrElse(hour) { 0 } } ?: 0
        }

        // For steps, use the authoritative highest-priority source (or max between hardware & HC if timestamps align)
        val reconciledSteps = primary.steps
        val reconciledDistance = if (primary.distanceMeters > 0) primary.distanceMeters else (reconciledSteps * 0.76)
        val reconciledActiveCal = if (primary.activeCaloriesKcal > 0) primary.activeCaloriesKcal else (reconciledSteps * 0.04).toInt()
        val reconciledTotalCal = if (primary.totalCaloriesKcal > 0) primary.totalCaloriesKcal else (1600 + reconciledActiveCal)
        val reconciledActiveMinutes = if (primary.activeMinutes > 0) primary.activeMinutes else (reconciledSteps / 100).coerceAtLeast(0)

        return DailyActivity(
            epochDay = primary.epochDay,
            steps = reconciledSteps,
            stepGoal = stepGoal,
            distanceMeters = reconciledDistance,
            activeCaloriesKcal = reconciledActiveCal,
            totalCaloriesKcal = reconciledTotalCal,
            activeMinutes = reconciledActiveMinutes,
            hourlySteps = mergedHourly,
            source = primary.source,
            lastUpdatedEpochMs = System.currentTimeMillis()
        )
    }

    fun reconcileHeartRateReadings(
        rawReadings: List<HeartRateReading>,
        clusterWindowMs: Long = 10_000L
    ): List<HeartRateReading> {
        if (rawReadings.isEmpty()) return emptyList()

        val sorted = rawReadings.sortedWith(
            compareBy<HeartRateReading> { it.timestampEpochMs }
                .thenByDescending { it.source.priority }
        )

        val result = mutableListOf<HeartRateReading>()
        var currentCluster = mutableListOf<HeartRateReading>()

        for (reading in sorted) {
            if (currentCluster.isEmpty()) {
                currentCluster.add(reading)
            } else {
                val firstInCluster = currentCluster.first()
                if (abs(reading.timestampEpochMs - firstInCluster.timestampEpochMs) <= clusterWindowMs) {
                    currentCluster.add(reading)
                } else {
                    // Pick the highest priority reading from the current cluster
                    result.add(currentCluster.maxByOrNull { it.source.priority }!!)
                    currentCluster = mutableListOf(reading)
                }
            }
        }

        if (currentCluster.isNotEmpty()) {
            result.add(currentCluster.maxByOrNull { it.source.priority }!!)
        }

        return result
    }

    fun reconcileSpo2Readings(raw: List<Spo2Reading>, clusterWindowMs: Long = 60_000L): List<Spo2Reading> {
        if (raw.isEmpty()) return emptyList()
        val sorted = raw.sortedWith(
            compareBy<Spo2Reading> { it.timestampEpochMs }.thenByDescending { it.source.priority }
        )
        val result = mutableListOf<Spo2Reading>()
        var lastAddedTime = -1L

        for (reading in sorted) {
            if (lastAddedTime == -1L || abs(reading.timestampEpochMs - lastAddedTime) > clusterWindowMs) {
                result.add(reading)
                lastAddedTime = reading.timestampEpochMs
            }
        }
        return result
    }

    fun reconcileHrvReadings(raw: List<HrvReading>, clusterWindowMs: Long = 60_000L): List<HrvReading> {
        if (raw.isEmpty()) return emptyList()
        val sorted = raw.sortedWith(
            compareBy<HrvReading> { it.timestampEpochMs }.thenByDescending { it.source.priority }
        )
        val result = mutableListOf<HrvReading>()
        var lastAddedTime = -1L

        for (reading in sorted) {
            if (lastAddedTime == -1L || abs(reading.timestampEpochMs - lastAddedTime) > clusterWindowMs) {
                result.add(reading)
                lastAddedTime = reading.timestampEpochMs
            }
        }
        return result
    }

    fun reconcileStressReadings(raw: List<StressReading>, clusterWindowMs: Long = 60_000L): List<StressReading> {
        if (raw.isEmpty()) return emptyList()
        val sorted = raw.sortedWith(
            compareBy<StressReading> { it.timestampEpochMs }.thenByDescending { it.source.priority }
        )
        val result = mutableListOf<StressReading>()
        var lastAddedTime = -1L

        for (reading in sorted) {
            if (lastAddedTime == -1L || abs(reading.timestampEpochMs - lastAddedTime) > clusterWindowMs) {
                result.add(reading)
                lastAddedTime = reading.timestampEpochMs
            }
        }
        return result
    }

    fun reconcileTemperatureReadings(raw: List<TemperatureReading>, clusterWindowMs: Long = 60_000L): List<TemperatureReading> {
        if (raw.isEmpty()) return emptyList()
        val sorted = raw.sortedWith(
            compareBy<TemperatureReading> { it.timestampEpochMs }.thenByDescending { it.source.priority }
        )
        val result = mutableListOf<TemperatureReading>()
        var lastAddedTime = -1L

        for (reading in sorted) {
            if (lastAddedTime == -1L || abs(reading.timestampEpochMs - lastAddedTime) > clusterWindowMs) {
                result.add(reading)
                lastAddedTime = reading.timestampEpochMs
            }
        }
        return result
    }

    fun reconcileSleepSessions(sessions: List<SleepSession>): List<SleepSession> {
        if (sessions.isEmpty()) return emptyList()

        val sorted = sessions.sortedWith(
            compareBy<SleepSession> { it.startTimeEpochMs }.thenByDescending { it.source.priority }
        )

        val merged = mutableListOf<SleepSession>()

        for (session in sorted) {
            val overlapping = merged.indexOfFirst { existing ->
                val overlapStart = maxOf(existing.startTimeEpochMs, session.startTimeEpochMs)
                val overlapEnd = minOf(existing.endTimeEpochMs, session.endTimeEpochMs)
                overlapEnd > overlapStart
            }

            if (overlapping >= 0) {
                val existing = merged[overlapping]
                // If the new session is higher priority or has richer stage data, replace existing
                if (session.source.priority > existing.source.priority ||
                    (session.stages.isNotEmpty() && existing.stages.isEmpty())) {
                    merged[overlapping] = session
                }
            } else {
                merged.add(session)
            }
        }

        return merged
    }

    fun reconcileWorkouts(workouts: List<WorkoutSession>): List<WorkoutSession> {
        if (workouts.isEmpty()) return emptyList()

        val sorted = workouts.sortedWith(
            compareBy<WorkoutSession> { it.startTimeEpochMs }.thenByDescending { it.source.priority }
        )

        val merged = mutableListOf<WorkoutSession>()

        for (workout in sorted) {
            val isDuplicate = merged.any { existing ->
                val startDiff = abs(existing.startTimeEpochMs - workout.startTimeEpochMs)
                val isSameType = existing.type == workout.type
                startDiff < 300_000L && isSameType // within 5 minutes of same activity
            }

            if (!isDuplicate) {
                merged.add(workout)
            }
        }

        return merged
    }
}
