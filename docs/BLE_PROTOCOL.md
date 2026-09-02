# Pebble Qore 2 Bluetooth LE (BLE) Protocol Specification

This document specifies the Bluetooth Low Energy GATT communication protocol used between the **Pebble Qore 2** wearable band and the **Senswear** Android application.

---

## 1. Standard Bluetooth SIG GATT Services

### 1.1 Heart Rate Service (`0x180D`)
- **Characteristic**: Heart Rate Measurement (`0x2A37`, UUID `00002a37-0000-1000-8000-00805f9b34fb`)
- **Properties**: `NOTIFY`
- **CCCD**: `0x2902` write `0x0001`
- **Payload Structure**:
  - `Byte 0`: Flags (`0x00` = uint8 HR value; `0x01` = uint16 HR value)
  - `Byte 1 [..2]`: Heart Rate in Beats Per Minute (BPM)

### 1.2 Battery Service (`0x180F`)
- **Characteristic**: Battery Level (`0x2A19`, UUID `00002a19-0000-1000-8000-00805f9b34fb`)
- **Properties**: `READ`, `NOTIFY`
- **Payload Structure**: Single `uint8` byte representing 0% to 100%.

### 1.3 Device Information Service (`0x180A`)
- **Model Number String**: `0x2A24` (e.g. `"PB-Q2-BLACK"`)
- **Firmware Revision String**: `0x2A26` (e.g. `"v2.4.1-rc3"`)
- **Hardware Revision String**: `0x2A27` (e.g. `"Rev. C"`)
- **Manufacturer Name String**: `0x2A29` (e.g. `"Pebble"`)

### 1.4 Health Thermometer Service (`0x1809`)
- **Characteristic**: Temperature Measurement (`0x2A1C`, UUID `00002a1c-0000-1000-8000-00805f9b34fb`)
- **Properties**: `INDICATE`, `NOTIFY`
- **Payload Structure**: Flag byte + IEEE 11073 32-bit FLOAT in Celsius.

---

## 2. Pebble Vendor Custom Telemetry Service (`0xFEE0`)

- **Service UUID**: `0000fee0-0000-1000-8000-00805f9b34fb`
- **Command RX Characteristic**: `0000fee1-0000-1000-8000-00805f9b34fb` (`WRITE`, `WRITE_NO_RESPONSE`)
- **Telemetry TX Characteristic**: `0000fee2-0000-1000-8000-00805f9b34fb` (`NOTIFY`)

### 2.1 Live Multi-Sensor Telemetry Packet (`OpCode 0x10`)

Transmitted periodically or during live tracking to deliver high-density unified wellness metrics in an efficient 18-byte frame:

```
┌───────┬──────────────┬──────────────┬──────────────┬──────┬──────┬──────────────┬────────┬──────────────┬─────────┬──────────┐
│ Byte  │ 0            │ 1..4         │ 5..6         │ 7..8 │ 9    │ 10           │ 11..12 │ 13           │ 14..15  │ 16       │ 17       │
├───────┼──────────────┼──────────────┼──────────────┼──────┼──────┼──────────────┼────────┼──────────────┼─────────┼──────────┤
│ Field │ OpCode (0x10)│ Steps        │ Active Cals  │ Dist │ HR   │ SpO₂         │ HRV    │ Stress       │ Temp    │ Battery  │ Checksum │
│ Type  │ uint8        │ uint32 LE    │ uint16 LE    │uint16│uint8 │ uint8        │uint16  │ uint8 (0-100)│uint16 LE│ uint8    │ XOR(0-16)│
└───────┴──────────────┴──────────────┴──────────────┴──────┴──────┴──────────────┴────────┴──────────────┴─────────┴──────────┘
```

### 2.2 Checksum Verification
```kotlin
var checksum: Byte = 0
for (i in 0 until 17) {
    checksum = (checksum.toInt() xor data[i].toInt()).toByte()
}
if (checksum != data[17]) {
    // Drop malformed packet
}
```

### 2.3 Haptic Control Command
- Send `[0x20, pattern_id]` to `0xFEE1`:
  - `0x01`: Single pulse (Goal reached)
  - `0x02`: Double pulse (Workout started)
  - `0x03`: Triple pulse (Workout stopped)
