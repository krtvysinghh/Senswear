package com.senswear.app.core.domain.model

enum class WearableBrand(
    val displayName: String,
    val brandCategory: String,
    val defaultIconName: String,
    val namePatterns: List<String>
) {
    PEBBLE_QORE_2(
        displayName = "Pebble Qore 2",
        brandCategory = "Screen-Free Wellness Band",
        defaultIconName = "watch",
        namePatterns = listOf("Pebble", "Qore", "Q2", "PB-Q2")
    ),
    SAMSUNG_GALAXY_WATCH(
        displayName = "Samsung Galaxy Watch",
        brandCategory = "Wear OS Smartwatch",
        defaultIconName = "watch",
        namePatterns = listOf("Galaxy Watch", "Galaxy", "SM-R", "Samsung")
    ),
    APPLE_WATCH(
        displayName = "Apple Watch",
        brandCategory = "watchOS Companion (BLE Broadcast)",
        defaultIconName = "watch",
        namePatterns = listOf("Apple Watch", "Apple", "Watch")
    ),
    WHOOP_STRAP(
        displayName = "Whoop 4.0 / 3.0",
        brandCategory = "Performance Recovery Strap",
        defaultIconName = "watch",
        namePatterns = listOf("Whoop", "WHOOP", "Whoop Strap")
    ),
    GARMIN(
        displayName = "Garmin Watch",
        brandCategory = "GPS Sports Watch",
        defaultIconName = "watch",
        namePatterns = listOf("Garmin", "Forerunner", "Fenix", "Venu", "Instinct", "Tactix")
    ),
    FITBIT(
        displayName = "Fitbit Tracker",
        brandCategory = "Fitness Tracker",
        defaultIconName = "watch",
        namePatterns = listOf("Fitbit", "Charge", "Sense", "Versa", "Inspire", "Luxe")
    ),
    OURA_RING(
        displayName = "Oura Ring",
        brandCategory = "Smart Health Ring",
        defaultIconName = "watch",
        namePatterns = listOf("Oura", "Oura Ring")
    ),
    POLAR(
        displayName = "Polar / Coros",
        brandCategory = "Heart Rate & Cadence Sensor",
        defaultIconName = "watch",
        namePatterns = listOf("Polar", "H10", "Verity", "Coros", "Pace")
    ),
    GENERIC_BLE(
        displayName = "Universal Smart Wearable",
        brandCategory = "Bluetooth SIG Standard Wearable",
        defaultIconName = "watch",
        namePatterns = emptyList()
    );

    companion object {
        fun classifyDevice(deviceName: String?): WearableBrand {
            if (deviceName.isNullOrBlank()) return GENERIC_BLE
            val lower = deviceName.lowercase()
            for (brand in entries) {
                if (brand == GENERIC_BLE) continue
                if (brand.namePatterns.any { pattern -> lower.contains(pattern.lowercase()) }) {
                    return brand
                }
            }
            return GENERIC_BLE
        }
    }
}
