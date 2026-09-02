package com.senswear.app.core.domain.model

enum class DataSource(val priority: Int, val displayName: String) {
    PEBBLE_QORE_2_BLE(priority = 100, displayName = "Pebble Qore 2 (BLE Direct)"),
    HEALTH_CONNECT(priority = 80, displayName = "Google Health Connect"),
    PHONE_SENSORS(priority = 50, displayName = "Phone Step Sensor"),
    MANUAL_INPUT(priority = 10, displayName = "Manual Entry"),
    SIMULATED_DEBUG(priority = 1, displayName = "Debug Simulator")
}
