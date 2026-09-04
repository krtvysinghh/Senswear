package com.senswear.app.wearable

import com.senswear.app.core.wearable.DataProvenance
import com.senswear.app.core.wearable.DataQuality
import com.senswear.app.core.wearable.WearableProtocol
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DataProvenanceTest {

    @Test
    fun `DataProvenance preserves source device, vendor, and protocol traceability`() {
        val provenance = DataProvenance(
            metricName = "heart_rate",
            canonicalValue = 74.0,
            canonicalUnit = "bpm",
            timestampEpochMs = 1700000000000L,
            sourceDeviceName = "Pebble Qore 2",
            sourceDeviceId = "AA:BB:CC:11:22:33",
            sourceVendor = "Pebble",
            sourceProtocol = WearableProtocol.BLE_VENDOR_QORE2,
            dataQuality = DataQuality.EXCELLENT,
            confidenceScore = 0.98f,
            isEstimated = false
        )

        assertEquals("heart_rate", provenance.metricName)
        assertEquals(74.0, provenance.canonicalValue, 0.01)
        assertEquals("bpm", provenance.canonicalUnit)
        assertEquals("Pebble Qore 2", provenance.sourceDeviceName)
        assertEquals(WearableProtocol.BLE_VENDOR_QORE2, provenance.sourceProtocol)
        assertFalse(provenance.isEstimated)
    }
}
