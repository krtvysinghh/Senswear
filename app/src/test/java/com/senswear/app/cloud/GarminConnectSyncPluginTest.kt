package com.senswear.app.cloud

import com.senswear.app.core.wearable.DataQuality
import com.senswear.app.core.wearable.WearableProtocol
import com.senswear.app.core.wearable.cloud.GarminConnectSyncPlugin
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class GarminConnectSyncPluginTest {

    @Test
    fun `parseDailiesPayload correctly extracts daily steps, stress, resting HR, and provenance`() {
        val sampleJson = """
            [
              {
                "summaryId": "garmin_sum_441",
                "startTimeGMT": "2026-09-04T00:00:00Z",
                "steps": 11420,
                "stepGoal": 10000,
                "distanceInMeters": 8820.0,
                "activeKilocalories": 490,
                "totalKilocalories": 2190,
                "moderateIntensityDurationInSeconds": 1800,
                "vigorousIntensityDurationInSeconds": 1200,
                "restingHeartRateInBeatsPerMinute": 54,
                "averageStressLevel": 28
              }
            ]
        """.trimIndent()

        val result = GarminConnectSyncPlugin.parseDailiesPayload(sampleJson)

        assertEquals(1, result.activities.size)
        val act = result.activities.first()
        assertEquals(11420, act.steps)
        assertEquals(8820.0, act.distanceMeters, 0.01)
        assertEquals(490, act.activeCaloriesKcal)
        assertEquals(50, act.activeMinutes) // (1800 + 1200) / 60 = 50 min

        assertEquals(1, result.restingHeartRates.size)
        assertEquals(54, result.restingHeartRates.first().bpm)

        assertEquals(1, result.stressReadings.size)
        assertEquals(28, result.stressReadings.first().score)

        assertEquals(1, result.provenanceRecords.size)
        val prov = result.provenanceRecords.first()
        assertEquals("daily_steps", prov.metricName)
        assertEquals(11420.0, prov.canonicalValue, 0.01)
        assertEquals("Garmin", prov.sourceVendor)
        assertEquals("Garmin Watch", prov.sourceDeviceName)
        assertEquals(WearableProtocol.VENDOR_CLOUD_API, prov.sourceProtocol)
        assertEquals(DataQuality.EXCELLENT, prov.dataQuality)
    }
}
