package com.senswear.app.cloud

import com.senswear.app.core.wearable.DataQuality
import com.senswear.app.core.wearable.WearableProtocol
import com.senswear.app.core.wearable.cloud.WhoopCloudSyncPlugin
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WhoopCloudSyncPluginTest {

    @Test
    fun `parseRecoveryPayload correctly extracts recovery, HRV, resting HR, and provenance`() {
        val sampleJson = """
            {
              "records": [
                {
                  "cycle_id": "whoop_cycle_9921",
                  "updated_at": "2026-09-04T08:00:00Z",
                  "score": {
                    "recovery_score": 84,
                    "resting_heart_rate": 56,
                    "hrv_rmssd_milli": 68.4,
                    "spo2_percentage": 98.2,
                    "skin_temp_celsius": 36.4
                  }
                }
              ]
            }
        """.trimIndent()

        val result = WhoopCloudSyncPlugin.parseRecoveryPayload(sampleJson)

        assertEquals(1, result.heartRates.size)
        assertEquals(56, result.heartRates.first().bpm)
        assertEquals(56, result.heartRates.first().restingHeartRateBpm)

        assertEquals(1, result.hrvs.size)
        assertEquals(68, result.hrvs.first().rmssdMs)

        assertEquals(1, result.spo2s.size)
        assertEquals(98, result.spo2s.first().percentage)

        assertEquals(1, result.temperatures.size)
        assertEquals(36.4, result.temperatures.first().temperatureCelsius, 0.01)

        assertEquals(4, result.provenanceRecords.size)
        val rhrProv = result.provenanceRecords.first { it.metricName == "resting_heart_rate" }
        assertEquals("Whoop", rhrProv.sourceVendor)
        assertEquals("Whoop 4.0", rhrProv.sourceDeviceName)
        assertEquals(WearableProtocol.VENDOR_CLOUD_API, rhrProv.sourceProtocol)
        assertEquals(DataQuality.EXCELLENT, rhrProv.dataQuality)
        assertFalse(rhrProv.isEstimated)
    }
}
