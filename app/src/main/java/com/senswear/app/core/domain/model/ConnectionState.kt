package com.senswear.app.core.domain.model

enum class ConnectionState(val label: String) {
    DISCONNECTED("Disconnected"),
    SCANNING("Scanning for Qore 2..."),
    CONNECTING("Connecting..."),
    CONNECTED("Connected"),
    SYNCING("Syncing Health Data..."),
    ERROR("Connection Error")
}

data class BatteryState(
    val percentage: Int, // 0..100
    val isCharging: Boolean = false,
    val estimatedDaysRemaining: Int = (percentage * 45 / 100).coerceAtLeast(1),
    val lastUpdatedEpochMs: Long = System.currentTimeMillis()
)

data class WearableDevice(
    val id: String,
    val name: String = "Pebble Qore 2",
    val macAddress: String,
    val firmwareVersion: String = "v2.4.1-rc3",
    val modelNumber: String = "PB-Q2-BLACK",
    val hardwareRevision: String = "Rev. C",
    val rssi: Int = -62,
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED,
    val batteryState: BatteryState = BatteryState(percentage = 85),
    val lastSyncEpochMs: Long = 0L,
    val isPaired: Boolean = false
)
