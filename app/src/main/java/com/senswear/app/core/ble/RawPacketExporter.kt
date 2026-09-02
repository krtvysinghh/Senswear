package com.senswear.app.core.ble

object RawPacketExporter {
    fun formatPacketsAsPcapText(logs: List<String>): String {
        val sb = StringBuilder()
        sb.append("# Senswear Pebble Qore 2 Raw Packet Trace\n")
        sb.append("# Generated: ${System.currentTimeMillis()}\n\n")
        for (log in logs) {
            sb.append(log).append("\n")
        }
        return sb.toString()
    }
}
