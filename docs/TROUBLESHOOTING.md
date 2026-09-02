# Pebble Qore 2 Hardware Troubleshooting Guide

---

## 1. Bluetooth Connection Issues
- **Symptom**: App indicates `DISCONNECTED` or gets stuck in `SCANNING`.
- **Remedy**:
  1. Ensure Pebble Qore 2 is charged (place on magnetic charging dock to wake from deep sleep).
  2. Toggle Bluetooth off and on in Android Quick Settings.
  3. Ensure location services are enabled on Android 11 and older devices.

---

## 2. Telemetry Sync Delays
- **Symptom**: `Last synced` is older than 15 minutes.
- **Remedy**:
  1. Tap **Sync Now** on the Home or Device screen.
  2. Senswear will initiate the flash bulk transfer handshake over GATT `0xFEE1`.
