# Senswear Architecture & System Design

Senswear is architected following modern Android development best practices, Clean Architecture principles, and a dark-first Glassmorphism design system.

---

## 1. System Architecture Layers

```
┌────────────────────────────────────────────────────────────────────────┐
│                          Jetpack Compose UI                            │
│  (SensGlassSurface, Glass Cards, Charts, Animations, Dark Glass Theme) │
├────────────────────────────────────────────────────────────────────────┤
│                      ViewModels & UI State Holders                     │
│    (HomeViewModel, ActivityViewModel, HealthViewModel, etc.)           │
├────────────────────────────────────────────────────────────────────────┤
│                          Domain / Use Cases                            │
│ (CanonicalDataReconciler, HeartRateZones, GoalProgress, Insights)      │
├────────────────────────────────────────────────────────────────────────┤
│                       Repository Layer (Flow)                          │
│ (ActivityRepo, HealthRepo, SleepRepo, WorkoutRepo, GoalRepo, etc.)     │
├──────────────────────────────────┬─────────────────────────────────────┤
│     Local Storage Layer          │          Wearable / Health          │
│   • Senswear SQLite DB (11 tbls) │   • WearableConnector (Qore 2 BLE)  │
│   • DataStore Preferences        │   • Google Health Connect Manager   │
│   • Encrypted/Local Backup       │   • FakeQore2Connector (Debug only) │
└──────────────────────────────────┴─────────────────────────────────────┘
```

---

## 2. Core Architectural Pillars

### A. Wearable Abstraction (`WearableConnector`)
All wearable communication is decoupled behind the `WearableConnector` interface:
```kotlin
interface WearableConnector {
    val connectionState: StateFlow<ConnectionState>
    val liveMetrics: StateFlow<FitnessSnapshot>
    val currentDevice: StateFlow<WearableDevice?>
    val rawPacketLogs: StateFlow<List<String>>

    suspend fun connect(macAddress: String? = null)
    suspend fun disconnect()
    suspend fun syncHistory(): Result<Int>
    suspend fun getDeviceInfo(): WearableDevice?
    suspend fun getBattery(): BatteryState
    suspend fun startWorkout(type: WorkoutType): Result<WorkoutSession>
    suspend fun stopWorkout(): Result<WorkoutSession?>
    suspend fun triggerHapticAlert(type: Int = 1)
}
```
This enables zero-friction addition of future wearable devices without altering ViewModel or UI layers.

### B. Dual Data Strategy & Canonical Data Reconciliation
Senswear seamlessly marries direct low-latency BLE telemetry with Android's Google Health Connect platform:
- **Priority Hierarchy**: Pebble Qore 2 Hardware BLE (Priority 100) > Google Health Connect (Priority 80) > Onboard Phone Sensors (Priority 50).
- **Cluster Window Deduplication**: Heart rate and telemetry samples within a 10-second window are deduplicated, favoring authoritative wearable hardware.
- **Sleep & Workout Merging**: Overlapping sessions are unified, preserving granular sleep hypnogram stages and sample streams.

### C. Glassmorphism Design System
- **Surfaces**: `SensGlassSurface` and `SensGlassCard` with vertical translucent gradients, hairline specular borders, and soft accent glows.
- **Canvas Visualizations**: Custom Bezier line charts (`SensLineChart`), rounded bar charts (`SensHourlyBarChart`), sleep hypnograms (`SensSleepHypnogram`), and heart rate zone meters (`SensHeartRateZonesChart`).
- **Typography & Accessibility**: WCAG AAA/AA contrast compliant with large metric numerals and responsive text scalings.
