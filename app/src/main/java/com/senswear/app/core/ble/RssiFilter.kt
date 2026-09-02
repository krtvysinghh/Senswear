package com.senswear.app.core.ble

class RssiFilter(private val windowSize: Int = 5) {
    private val buffer = mutableListOf<Int>()

    fun addSample(rssi: Int): Int {
        buffer.add(rssi)
        if (buffer.size > windowSize) {
            buffer.removeAt(0)
        }
        return buffer.average().toInt()
    }

    fun getSignalQualityPercent(smoothedRssi: Int): Int {
        // -40 dBm is ~100%, -90 dBm is ~0%
        return (((smoothedRssi + 90).toDouble() / 50.0) * 100.0).toInt().coerceIn(0, 100)
    }
}
