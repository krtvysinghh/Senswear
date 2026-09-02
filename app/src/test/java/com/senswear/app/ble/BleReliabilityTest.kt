package com.senswear.app.ble

import com.senswear.app.core.ble.ExponentialBackoffRetry
import com.senswear.app.core.ble.RetryConfig
import com.senswear.app.core.ble.RssiFilter
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BleReliabilityTest {

    @Test
    fun `ExponentialBackoffRetry increments attempts and respects max attempts`() = runBlocking {
        val retry = ExponentialBackoffRetry(RetryConfig(initialDelayMs = 10, maxAttempts = 3, jitter = false))

        assertTrue(retry.hasRemainingAttempts)
        assertTrue(retry.waitNextRetry())
        assertTrue(retry.waitNextRetry())
        assertTrue(retry.waitNextRetry())
        assertFalse(retry.hasRemainingAttempts)
        assertFalse(retry.waitNextRetry())
    }

    @Test
    fun `RssiFilter smooths noisy RSSI readings correctly`() {
        val filter = RssiFilter(windowSize = 3)
        filter.addSample(-60)
        filter.addSample(-70)
        val smoothed = filter.addSample(-80)

        assertEquals(-70, smoothed)
        val quality = filter.getSignalQualityPercent(-70)
        assertEquals(40, quality)
    }
}
