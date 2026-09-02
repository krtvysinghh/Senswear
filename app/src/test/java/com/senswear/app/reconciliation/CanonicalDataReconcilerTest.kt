package com.senswear.app.reconciliation

import com.senswear.app.core.domain.model.DailyActivity
import com.senswear.app.core.domain.model.DataSource
import com.senswear.app.core.domain.model.HeartRateReading
import com.senswear.app.core.domain.model.SleepSession
import com.senswear.app.core.domain.model.WorkoutSession
import com.senswear.app.core.domain.model.WorkoutType
import com.senswear.app.core.reconciliation.CanonicalDataReconciler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CanonicalDataReconcilerTest {

    @Test
    fun `reconcileDailyActivity prioritizes Pebble Qore 2 BLE over Health Connect and Phone sensors`() {
        val qore2Activity = DailyActivity(
            epochDay = 19800L,
            steps = 8421,
            stepGoal = 10000,
            distanceMeters = 6400.0,
            activeCaloriesKcal = 342,
            totalCaloriesKcal = 1942,
            activeMinutes = 48,
            source = DataSource.PEBBLE_QORE_2_BLE
        )

        val healthConnectActivity = DailyActivity(
            epochDay = 19800L,
            steps = 8390,
            stepGoal = 10000,
            distanceMeters = 6350.0,
            activeCaloriesKcal = 330,
            totalCaloriesKcal = 1930,
            activeMinutes = 46,
            source = DataSource.HEALTH_CONNECT
        )

        val phoneSensorActivity = DailyActivity(
            epochDay = 19800L,
            steps = 5812,
            stepGoal = 10000,
            distanceMeters = 4400.0,
            activeCaloriesKcal = 220,
            totalCaloriesKcal = 1820,
            activeMinutes = 30,
            source = DataSource.PHONE_SENSORS
        )

        val reconciled = CanonicalDataReconciler.reconcileDailyActivity(
            qore2Activity = qore2Activity,
            healthConnectActivity = healthConnectActivity,
            phoneSensorActivity = phoneSensorActivity
        )

        // Must be the authoritative 8,421 steps from Qore 2, NOT sum (22,623)
        assertEquals(8421, reconciled.steps)
        assertEquals(DataSource.PEBBLE_QORE_2_BLE, reconciled.source)
    }

    @Test
    fun `reconcileHeartRateReadings deduplicates within cluster window preserving highest priority source`() {
        val baseTime = 1700000000000L

        val readings = listOf(
            HeartRateReading(baseTime, 72, source = DataSource.PHONE_SENSORS),
            HeartRateReading(baseTime + 2000L, 75, source = DataSource.PEBBLE_QORE_2_BLE),
            HeartRateReading(baseTime + 4000L, 74, source = DataSource.HEALTH_CONNECT),
            // Next cluster (> 10s away)
            HeartRateReading(baseTime + 15000L, 80, source = DataSource.PEBBLE_QORE_2_BLE)
        )

        val reconciled = CanonicalDataReconciler.reconcileHeartRateReadings(readings, clusterWindowMs = 10_000L)

        assertEquals(2, reconciled.size)
        assertEquals(75, reconciled[0].bpm)
        assertEquals(DataSource.PEBBLE_QORE_2_BLE, reconciled[0].source)
        assertEquals(80, reconciled[1].bpm)
    }

    @Test
    fun `reconcileSleepSessions merges overlapping sessions with stage priority`() {
        val start = 1700000000000L
        val end = start + (8 * 3600 * 1000L)

        val sessionHC = SleepSession(
            id = "hc_1",
            startTimeEpochMs = start,
            endTimeEpochMs = end,
            durationMinutes = 480,
            deepMinutes = 0,
            lightMinutes = 480,
            remMinutes = 0,
            awakeMinutes = 0,
            sleepScore = 70,
            stages = emptyList(),
            source = DataSource.HEALTH_CONNECT
        )

        val sessionQore = SleepSession(
            id = "qore_1",
            startTimeEpochMs = start,
            endTimeEpochMs = end,
            durationMinutes = 480,
            deepMinutes = 110,
            lightMinutes = 230,
            remMinutes = 110,
            awakeMinutes = 30,
            sleepScore = 88,
            source = DataSource.PEBBLE_QORE_2_BLE
        )

        val reconciled = CanonicalDataReconciler.reconcileSleepSessions(listOf(sessionHC, sessionQore))

        assertEquals(1, reconciled.size)
        assertEquals(88, reconciled[0].sleepScore)
        assertEquals(DataSource.PEBBLE_QORE_2_BLE, reconciled[0].source)
    }

    @Test
    fun `reconcileWorkouts prevents duplicate sessions within 5 minutes`() {
        val start = 1700000000000L

        val workout1 = WorkoutSession(
            id = "w1",
            type = WorkoutType.OUTDOOR_RUN,
            startTimeEpochMs = start,
            durationSeconds = 1800L,
            totalCaloriesKcal = 300,
            source = DataSource.PEBBLE_QORE_2_BLE
        )

        val workoutDuplicateHC = WorkoutSession(
            id = "w2",
            type = WorkoutType.OUTDOOR_RUN,
            startTimeEpochMs = start + 60000L, // 1 min difference
            durationSeconds = 1800L,
            totalCaloriesKcal = 295,
            source = DataSource.HEALTH_CONNECT
        )

        val reconciled = CanonicalDataReconciler.reconcileWorkouts(listOf(workout1, workoutDuplicateHC))

        assertEquals(1, reconciled.size)
        assertEquals(DataSource.PEBBLE_QORE_2_BLE, reconciled[0].source)
    }
}
