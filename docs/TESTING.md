# Senswear Testing Documentation

Senswear includes a comprehensive suite of automated unit tests covering domain models, data reconciliation, BLE packet decoding, and mathematical calculations.

---

## 1. Test Suite Overview

| Test Suite | File Location | Key Test Scenarios |
| :--- | :--- | :--- |
| **Canonical Reconciliation** | `test/.../reconciliation/CanonicalDataReconcilerTest.kt` | Source priority resolution (BLE > Health Connect > Phone sensor), step deduplication, HR cluster deduplication, sleep session merge, workout overlap prevention. |
| **BLE Protocol Decoding** | `test/.../ble/Qore2DecoderTest.kt` | 8-bit / 16-bit standard GATT HR decoding, Battery level uint8 decoding, IEEE 11073 32-bit float temperature parsing, Pebble Vendor live frame parsing, XOR checksum validation, corrupted packet rejection. |
| **Heart Rate Zones** | `test/.../domain/HeartRateZonesTest.kt` | Zone 1 (Warm Up) to Zone 5 (Max) boundary calculations. |
| **Goal Progress** | `test/.../domain/GoalProgressTest.kt` | Fractional progress computation, clipping, completion status. |
| **Sleep Analysis** | `test/.../domain/SleepAnalysisTest.kt` | Total sleep duration, efficiency percentage, and weighted sleep score evaluation. |

---

## 2. Executing Automated Tests
```bash
./gradlew testDebugUnitTest --info
```
