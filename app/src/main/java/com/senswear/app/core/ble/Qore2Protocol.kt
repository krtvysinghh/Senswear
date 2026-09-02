package com.senswear.app.core.ble

import java.util.UUID

object Qore2Protocol {
    // Standard Bluetooth SIG GATT Services
    val HEART_RATE_SERVICE_UUID: UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
    val HEART_RATE_MEASUREMENT_CHAR_UUID: UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")

    val BATTERY_SERVICE_UUID: UUID = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb")
    val BATTERY_LEVEL_CHAR_UUID: UUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb")

    val DEVICE_INFO_SERVICE_UUID: UUID = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb")
    val MODEL_NUMBER_CHAR_UUID: UUID = UUID.fromString("00002a24-0000-1000-8000-00805f9b34fb")
    val FIRMWARE_REVISION_CHAR_UUID: UUID = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb")
    val HARDWARE_REVISION_CHAR_UUID: UUID = UUID.fromString("00002a27-0000-1000-8000-00805f9b34fb")
    val MANUFACTURER_NAME_CHAR_UUID: UUID = UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb")

    val HEALTH_THERMOMETER_SERVICE_UUID: UUID = UUID.fromString("00001809-0000-1000-8000-00805f9b34fb")
    val TEMPERATURE_MEASUREMENT_CHAR_UUID: UUID = UUID.fromString("00002a1c-0000-1000-8000-00805f9b34fb")

    // Standard Client Characteristic Configuration Descriptor (CCCD)
    val CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    // Pebble Qore Vendor Telemetry Service (for bulk historical records, SpO2, HRV, Stress sync)
    val PEBBLE_VENDOR_SERVICE_UUID: UUID = UUID.fromString("0000fee0-0000-1000-8000-00805f9b34fb")
    val PEBBLE_VENDOR_RX_CHAR_UUID: UUID = UUID.fromString("0000fee1-0000-1000-8000-00805f9b34fb")
    val PEBBLE_VENDOR_TX_NOTIFY_UUID: UUID = UUID.fromString("0000fee2-0000-1000-8000-00805f9b34fb")

    // Pebble Qore Vendor OpCodes
    const val OPCODE_SYNC_START: Byte = 0x01
    const val OPCODE_SYNC_PAYLOAD: Byte = 0x02
    const val OPCODE_SYNC_ACK: Byte = 0x03
    const val OPCODE_SYNC_FINISH: Byte = 0x04
    const val OPCODE_LIVE_METRICS_REPORT: Byte = 0x10
    const val OPCODE_HAPTIC_TRIGGER: Byte = 0x20
    const val OPCODE_WORKOUT_COMMAND: Byte = 0x30

    // Pebble Device Name Advertisements
    val PEBBLE_DEVICE_NAMES = listOf("Pebble Qore 2", "Pebble Qore", "Qore 2", "PB-Q2")
}
