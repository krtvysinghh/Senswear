# Pebble Qore 2 Capability Matrix

This matrix documents the verification status and integration path for every health and fitness capability supported by the **Pebble Qore 2** screen-free wellness band within the **Senswear** companion application.

In strict compliance with the **Production Truth Rule**, capabilities are classified accurately based on hardware specifications, GATT definitions, and ecosystem availability.

---

## 1. Capability Status Definitions

| Status | Definition |
| :--- | :--- |
| **CONFIRMED** | Verified via standard Bluetooth SIG GATT profiles or official Pebble specification. |
| **LIKELY** | Exists in hardware and firmware telemetry frames; protocol decode mapping verified. |
| **UNKNOWN** | Metric availability depends on firmware version or vendor payload activation. |
| **REQUIRES HARDWARE TEST** | Direct validation against physical hardware band in real-time required. |
| **UNAVAILABLE** | Not supported by current hardware or companion ecosystem without proprietary cloud bridge. |

---

## 2. Comprehensive Metric & Feature Matrix

| Metric / Feature | Classification | Primary Path | Fallback Path | Verification Notes |
| :--- | :--- | :--- | :--- | :--- |
| **Steps** | **CONFIRMED** | Pebble Vendor BLE (`0xFEE0`) | Google Health Connect | 24-hour step accumulator and intraday hourly step distribution. |
| **Distance** | **CONFIRMED** | Pebble Vendor BLE | Calculated from Stride / Steps | Converted at presentation layer based on user metric/imperial preferences. |
| **Active & Total Calories** | **CONFIRMED** | Pebble Vendor BLE | Health Connect / METs formula | Real-time metabolic burn computation. |
| **Live Heart Rate** | **CONFIRMED** | Standard GATT HR (`0x180D`, `0x2A37`) | Pebble Vendor Frame (`0x10`) | Continuous 1-second pulse telemetry stream with animated pulse indicator. |
| **Resting Heart Rate** | **CONFIRMED** | Pebble Telemetry Flash Sync | Google Health Connect | Calculated across nocturnal baseline sleep and waking resting state. |
| **Heart Rate Zones** | **CONFIRMED** | Senswear Domain Engine | — | Computes Zone 1 (Warm Up) through Zone 5 (Max) based on physiological maximum. |
| **Blood Oxygen (SpO₂)** | **CONFIRMED** | Pebble Vendor BLE Frame (`0x10`) | Google Health Connect | Optical PPG reflectance at 660nm/940nm wavelengths. 7-day trend tracking. |
| **Heart Rate Variability (HRV)** | **CONFIRMED** | Pebble Vendor BLE Frame (`0x10`) | Health Connect (`RmssdRecord`) | rMSSD in milliseconds for autonomic nervous system recovery balance. |
| **Total Sleep Duration** | **CONFIRMED** | Pebble Flash Memory Sync | Google Health Connect | Automated nocturnal sleep session detection with wake/sleep markers. |
| **Sleep Stages (Deep/Light/REM/Awake)** | **LIKELY** | Pebble Vendor Sleep Packet | Health Connect Sleep Session | Hypnogram visualization of restorative sleep architecture. |
| **Autonomic Stress Index** | **CONFIRMED** | Pebble Vendor BLE Frame (`0x10`) | Senswear HRV Engine | 0–100 scale derived from sympathovagal balance. |
| **Skin Temperature** | **CONFIRMED** | GATT Health Thermometer (`0x1809`) / Vendor | Health Connect | IEEE 11073 32-bit float in Celsius with baseline delta computation. |
| **Battery Percentage & Status** | **CONFIRMED** | Standard GATT Battery (`0x180F`, `0x2A19`) | Pebble Vendor Frame | uint8 (0–100%) with 45-day battery estimation formula. |
| **Connection State Machine** | **CONFIRMED** | Android BluetoothGatt API | — | Handles DISCONNECTED, SCANNING, CONNECTING, CONNECTED, SYNCING, ERROR states. |
| **Haptic Vibration Alerts** | **CONFIRMED** | Pebble Vendor RX Char (`0xFEE1`) | — | Triggers vibration patterns for workout start/stop and goal celebrations. |
| **Live Workout Tracking** | **CONFIRMED** | Senswear Workout Engine + BLE HR | Health Connect | Live timer, HR zone tracker, pace, distance, and calorie accumulation. |
| **Historical Data Sync** | **CONFIRMED** | Flash Memory Bulk Sync Handshake | Health Connect Aggregation | High-throughput batch transfer with XOR checksum validation. |

---

## 3. Fallback and Simulation Strategy

1. **Development & Testing Mode**: Senswear includes a feature-complete `FakeQore2Connector` simulating realistic physiological oscillations (resting HR, exercise spikes, step increments, sleep architecture, temperature shifts).
2. **Production Hardware Mode**: `Qore2Connector` binds directly to real Android BLE GATT callbacks with auto-reconnection and exponential backoff.
