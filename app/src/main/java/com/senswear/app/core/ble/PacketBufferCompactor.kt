package com.senswear.app.core.ble

/**
 * Coalesces consecutive burst BLE telemetry packets to minimize CPU wakeups and database locks.
 */
class PacketBufferCompactor(
    private val maxBatchSize: Int = 50
) {
    private val buffer = mutableListOf<ByteArray>()

    fun appendPacket(packet: ByteArray): List<ByteArray>? {
        buffer.add(packet)
        return if (buffer.size >= maxBatchSize) {
            val flushBatch = buffer.toList()
            buffer.clear()
            flushBatch
        } else {
            null
        }
    }

    fun flushRemaining(): List<ByteArray> {
        val remaining = buffer.toList()
        buffer.clear()
        return remaining
    }
}
