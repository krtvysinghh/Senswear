package com.senswear.app.core.ble

/**
 * Test harness providing simulated GATT server behavior, simulated packet drop rates,
 * and disconnect/reconnect latency for CI integration testing.
 */
class MockGattServerTestBed(
    var simulatedPacketDropRate: Float = 0.0f,
    var simulatedLatencyMs: Long = 10L
) {
    private var isConnected = false
    private val packetLog = mutableListOf<ByteArray>()

    fun simulateConnect() {
        isConnected = true
    }

    fun simulateDisconnect() {
        isConnected = false
    }

    fun transmitPacket(payload: ByteArray): Boolean {
        if (!isConnected) return false
        if (Math.random().toFloat() < simulatedPacketDropRate) {
            return false // Dropped packet
        }
        packetLog.add(payload)
        return true
    }

    fun getReceivedCount(): Int = packetLog.size

    fun reset() {
        packetLog.clear()
        isConnected = false
    }
}
