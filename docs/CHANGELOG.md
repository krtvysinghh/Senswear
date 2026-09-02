# Changelog

All notable changes to **Senswear** will be documented in this file.

## [1.0.0] - 2026-09-02

### Added
- **Core Architecture**:
  - Full Clean Architecture implementation with Kotlin, Jetpack Compose, Coroutines, Flow, StateFlow, Room-backed SQLite database, and DataStore.
  - `WearableConnector` abstraction decoupling wearable implementations (`Qore2Connector`, `FakeQore2Connector`).
  - `CanonicalDataReconciler` providing authoritative source prioritization (BLE hardware > Health Connect > Phone sensor), cluster deduplication, and collision resolution.
- **Pebble Qore 2 Direct BLE**:
  - Standard Bluetooth SIG GATT client for Heart Rate (`0x180D`), Battery (`0x180F`), Device Information (`0x180A`), and Health Thermometer (`0x1809`).
  - Pebble Vendor Telemetry decoder (`0xFEE0`) for unified live frames with XOR checksum validation.
  - Auto-reconnect state machine, exponential backoff, and live diagnostic packet logger.
- **Google Health Connect**:
  - Bi-directional sync for Steps, Heart Rate, Sleep Sessions, SpO₂, Temperature, Distance, and Workouts.
- **Glassmorphism Design System**:
  - Dark-first luxury aesthetic with translucent surfaces (`SensGlassSurface`, `SensGlassCard`, `SensGlassButton`, `SensGlassChip`).
  - Canvas-drawn Bezier curves (`SensLineChart`), rounded hourly bar charts (`SensHourlyBarChart`), sleep hypnograms (`SensSleepHypnogram`), and HR zone meters (`SensHeartRateZonesChart`).
- **Feature Modules**:
  - Home Dashboard with step progress ring, live HR pulse badge, and metric cards.
  - Activity Hub with 24h hourly breakdown and 7-day/30-day trends.
  - Health Hub with dedicated screens for Heart Rate, SpO₂, HRV, Stress, and Temperature.
  - Sleep & Recovery screen with hypnogram stages (Deep, Light, REM, Awake) and sleep debt insights.
  - Live Workout Recording with timer, pace, HR zones, calories, and haptic feedback.
  - Device Management with battery health, specs, and real-time developer diagnostics.
  - Onboarding Flow with hardware permissions and device discovery.
  - Settings with customizable goals, unit converter, and complete local data export/purge tools.
- **Documentation & Testing**:
  - Unit test suite covering data reconciliation, GATT packet decoding, HR zones, and sleep algorithms.
  - Full technical documentation suite in `docs/`.
