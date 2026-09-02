package com.senswear.app.core.ble

class PacketSequenceTracker {
    private var lastSequenceNumber = -1
    var missedPacketsCount = 0
        private set

    fun trackSequence(sequenceNumber: Int): Boolean {
        var hasDropped = false
        if (lastSequenceNumber != -1) {
            val expected = (lastSequenceNumber + 1) and 0xFF
            if (sequenceNumber != expected) {
                val gap = if (sequenceNumber > lastSequenceNumber) sequenceNumber - lastSequenceNumber - 1 else (sequenceNumber + 256) - lastSequenceNumber - 1
                missedPacketsCount += gap
                hasDropped = true
            }
        }
        lastSequenceNumber = sequenceNumber
        return !hasDropped
    }

    fun reset() {
        lastSequenceNumber = -1
        missedPacketsCount = 0
    }
}
