package com.senswear.app.core.wearable

enum class WearableIntegrationType(
    val title: String,
    val description: String,
    val isDirectBleAccessible: Boolean
) {
    FULL_DIRECT_BLE(
        title = "Full Direct BLE",
        description = "Complete 2-way real-time BLE GATT connection and historical packet sync without cloud dependency.",
        isDirectBleAccessible = true
    ),
    STANDARD_GATT_BLE(
        title = "Standard Bluetooth SIG GATT",
        description = "Direct BLE connection using open standard Bluetooth SIG profiles (0x180D Heart Rate, 0x1814 RSC, 0x1809 Thermometer).",
        isDirectBleAccessible = true
    ),
    HEALTH_CONNECT_AGGREGATED(
        title = "Health Connect Ecosystem",
        description = "Unified health aggregation via Google Health Connect from vendor OEM companion apps on Android.",
        isDirectBleAccessible = false
    ),
    VENDOR_API_REQUIRED(
        title = "Official Vendor Cloud API Required",
        description = "Requires official developer credentials / OAuth tokens from the vendor's cloud service.",
        isDirectBleAccessible = false
    ),
    UNSUPPORTED_PROPRIETARY(
        title = "Closed Proprietary Transport",
        description = "Vendor utilizes private encrypted protocols not legally or technically exposed for direct third-party Android access.",
        isDirectBleAccessible = false
    )
}
