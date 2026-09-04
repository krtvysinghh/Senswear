package com.senswear.app.core.domain.model

enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    SCANNING,
    SYNCING,
    ERROR
}

data class BatteryState(
    val percentage: Int, // 0..100
    val isCharging: Boolean = false,
    val estimatedDaysRemaining: Int = (percentage * 45 / 100).coerceAtLeast(1),
    val lastUpdatedEpochMs: Long = System.currentTimeMillis()
)

data class WearableDevice(
    val id: String,
    val name: String,
    val macAddress: String,
    val firmwareVersion: String? = null,
    val modelNumber: String? = null,
    val hardwareRevision: String? = null,
    val rssi: Int = -62,
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED,
    val batteryState: BatteryState? = null,
    val lastSyncEpochMs: Long = 0L,
    val isPaired: Boolean = false
)
