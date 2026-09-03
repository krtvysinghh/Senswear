package com.senswear.app.ble

import com.senswear.app.core.domain.model.LivePulseEvent
import com.senswear.app.core.domain.model.LiveTelemetryBus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FastBleConnectorTest {

    @Test
    fun `LiveTelemetryBus emits and replays latest pulse event`() = runBlocking {
        LiveTelemetryBus.emitPulse(bpm = 78)
        val event: LivePulseEvent = LiveTelemetryBus.pulseEvents.first()

        assertEquals(78, event.bpm)
        assertTrue(event.isRealTimeStream)
        assertTrue(event.timestampEpochMs > 0)
    }
}
