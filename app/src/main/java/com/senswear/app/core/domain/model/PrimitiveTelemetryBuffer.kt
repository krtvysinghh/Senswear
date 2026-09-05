package com.senswear.app.core.domain.model

/**
 * Memory-efficient unboxed circular buffer for high-frequency (25Hz - 100Hz) ECG/PPG telemetry.
 * Completely eliminates object allocation and GC pressure during live visualization.
 */
class PrimitiveTelemetryBuffer(
    val capacity: Int = 1000
) {
    private val values = FloatArray(capacity)
    private val timestamps = LongArray(capacity)
    private var head = 0
    private var count = 0

    fun append(value: Float, timestampMs: Long) {
        values[head] = value
        timestamps[head] = timestampMs
        head = (head + 1) % capacity
        if (count < capacity) count++
    }

    val size: Int get() = count

    fun getLatestValue(): Float? {
        if (count == 0) return null
        val latestIndex = if (head == 0) capacity - 1 else head - 1
        return values[latestIndex]
    }

    fun copySnapshot(): FloatArray {
        val out = FloatArray(count)
        val start = if (count < capacity) 0 else head
        for (i in 0 until count) {
            out[i] = values[(start + i) % capacity]
        }
        return out
    }

    fun clear() {
        head = 0
        count = 0
    }
}
