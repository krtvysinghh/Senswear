package com.senswear.app.core.domain.model

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class LivePulseEvent(
    val timestampEpochMs: Long,
    val bpm: Int,
    val isRealTimeStream: Boolean = true
)

object LiveTelemetryBus {
    private val _pulseEvents = MutableSharedFlow<LivePulseEvent>(replay = 1, extraBufferCapacity = 64)
    val pulseEvents: SharedFlow<LivePulseEvent> = _pulseEvents.asSharedFlow()

    suspend fun emitPulse(bpm: Int) {
        _pulseEvents.emit(
            LivePulseEvent(
                timestampEpochMs = System.currentTimeMillis(),
                bpm = bpm
            )
        )
    }
}
