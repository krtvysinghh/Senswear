# Senswear

<p align="center">
  <img src="docs/assets/senswear_logo.svg" width="128" height="128" alt="Senswear Logo" />
</p>

<p align="center">
  <b>A production-grade, privacy-first Universal Wearable & Biometrics Platform for Android.</b><br>
  Engineered with Apple Native Liquid Glass aesthetics, Material 3 Adaptive layouts, direct Bluetooth LE GATT Central ingestion, OAuth2 Cloud Sync plugins, and Google Health Connect ecosystem bridging.
</p>

<p align="center">
  <a href="https://github.com/krtvysinghh/Senswear/releases/latest"><img src="https://img.shields.io/github/v/release/krtvysinghh/Senswear?color=00F0FF&label=Release&logo=github" alt="Latest Release" /></a>
  <img src="https://img.shields.io/badge/Platform-Android%208.0%2B%20(API%2026%E2%80%9336)-3DDC84?logo=android&logoColor=white" alt="Platform" />
  <img src="https://img.shields.io/badge/Kotlin-2.3.20-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose%20%2F%20Material%203%20Adaptive-4285F4?logo=jetpackcompose&logoColor=white" alt="Compose" />
  <img src="https://img.shields.io/badge/Health%20Connect-1.1.0-EA4335?logo=googlefit&logoColor=white" alt="Health Connect" />
  <img src="https://img.shields.io/badge/Bluetooth-BLE%205.4%20GATT%20Central-0082FC?logo=bluetooth&logoColor=white" alt="Bluetooth" />
  <img src="https://img.shields.io/badge/Architecture-Clean%20%2F%20Adapter%20Pattern-00C853" alt="Architecture" />
  <img src="https://img.shields.io/badge/License-Apache%202.0-blue" alt="License" />
</p>

---

## 🌟 Overview

**Senswear** is an open-source, vendor-neutral health and fitness telemetry platform on Android. It bridges raw physical Bluetooth Low Energy (BLE) sensor streams with Google Health Connect and vendor cloud APIs, normalizing disparate biometric protocols into a single, strongly-typed, verifiable domain model without cloud lock-in, advertising trackers, or synthetic data fabrication.

### Core Engineering Principles:
1. **Production Truth Over Fabrication**: Never generate synthetic telemetry, fake sine-wave heart rates, or placeholder battery levels. When hardware data is unavailable, the system explicitly communicates `Unavailable`, `Disconnected`, or `Unsupported`.
2. **Immutable Data Provenance**: Every metric preserves its complete origin trail (canonical value, metric name, unit, timestamp, source device MAC, vendor, transport protocol, data quality rating, and confidence score).
3. **Pluggable Adapter Architecture**: Vendor protocols are isolated in modular `WearableAdapter` implementations rather than tangled in conditional UI logic.
4. **Local Data Sovereignty**: 100% on-device SQLite database with zero cloud requirement, one-tap JSON/CSV data export, and full local purge controls.

---

## ✨ Key Features & Capabilities

### 💎 Apple Native Liquid Glass & Material 3 Adaptive UI
- **`SensLiquidGlassCard`**: Multi-layered frosted glass with specular highlight refraction edges (`Color(0x52FFFFFF)`), obsidian foundations, and ambient chromatic glow.
- **`SensLiquidDynamicIsland`**: Live floating status capsule HUD indicating real-time connection lifecycle, battery percentage, signal RSSI, and breathing pulse animation.
- **`SensLiquidProgressRing`**: Apple Fitness-style multi-layered liquid sweep progress ring with spring-damped physics.
- **`SensLiveWaveform`**: 60fps real-time physiological cardiac waveform canvas synchronized directly to live heart rate telemetry.
- **Material 3 Adaptive Multi-Form Factor**: Dynamically transitions between a floating bottom liquid glass capsule on mobile phones (< 600dp) and a lateral frosted glass Navigation Rail on tablets and foldables (≥ 600dp).
- **Butter-Smooth 120 FPS Rendering**: Bezier curves and bar charts rendered using `drawWithCache` to eliminate memory allocations during high-speed scrolling.

### ⌚ Universal Wearable Support Matrix
Senswear classifies hardware into distinct, honest integration tiers:

| Wearable Ecosystem | Integration Type | Supported Telemetry | Limitations & Requirements |
| :--- | :--- | :--- | :--- |
| **Pebble Qore 2** | `FULL_DIRECT_BLE` | Steps, 1Hz Live HR, SpO₂, Skin Temp, HRV rMSSD, Stress, Sleep Stages, Battery, Haptics | Reference hardware; full 2-way proprietary frame (`0xFEE0`) & GATT Central support |
| **Polar (H10 / Verity)** / **Coros** | `STANDARD_GATT_BLE` | 1Hz Live HR, RR-Interval HRV, Battery | Standard Bluetooth SIG profiles (`0x180D`, `0x180F`) |
| **Samsung Galaxy Watch** (4/5/6/7) | `HEALTH_CONNECT_AGGREGATED` | Steps, Daily HR, Sleep Staging, Workouts, SpO₂ | Aggregated via Samsung Health to Google Health Connect; Live HR via watch HRM Broadcast |
| **Google Pixel Watch** | `HEALTH_CONNECT_AGGREGATED` | Steps, HR, Sleep, Workouts, Active Minutes | Google Health Connect native synchronization |
| **Apple Watch** | `STANDARD_GATT_BLE` (Partial) | Live Heart Rate during workouts | Standard BLE Broadcast mode (`0x180D`); watchOS background sync is closed on Android |
| **Whoop 4.0 / 3.0** | `STANDARD_GATT_BLE` + `VENDOR_CLOUD_API` | Live HR & HRV (BLE Broadcast), Recovery & Sleep (OAuth2 API) | Direct BLE Broadcast mode + optional Whoop Developer Cloud API OAuth2 plugin |
| **Garmin** (Forerunner / Fenix / Venu) | `HEALTH_CONNECT_AGGREGATED` + `VENDOR_CLOUD_API` | Steps, HR, Sleep, Workouts, Stress, Body Battery | Health Connect aggregation + optional Garmin Connect Health API OAuth2 plugin |
| **Oura Ring** (Gen 2 / Gen 3) | `HEALTH_CONNECT_AGGREGATED` + `VENDOR_CLOUD_API` | Sleep Stages, HRV, Skin Temp Deviation | Health Connect sync + optional Oura Cloud API |
| **Generic Bluetooth SIG Sensors** | `STANDARD_GATT_BLE` | Heart Rate (`0x180D`), Running Cadence (`0x1814`), Temp (`0x1809`), Battery (`0x180F`) | Universal open GATT Central standard |

---

## 🏛 Clean Architecture & Data Flow

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                                 Jetpack Compose UI                                     │
│  (SensAdaptiveScaffold, SensLiquidGlass, SensLiveWaveform, Dynamic Island, Charts)    │
└───────────────────────────────────────────┬────────────────────────────────────────────┘
                                            │ StateFlow / UiEvents
┌───────────────────────────────────────────▼────────────────────────────────────────────┐
│                             ViewModels & UI State Holders                              │
│         (HomeViewModel, ActivityViewModel, HealthViewModel, DeviceViewModel)          │
└───────────────────────────────────────────┬────────────────────────────────────────────┘
                                            │ Use Cases / Domain Models
┌───────────────────────────────────────────▼────────────────────────────────────────────┐
│                                    Domain Engine                                       │
│  • PhysiologicalDerivationEngine (Keytel Calories, Karvonen HR Zones, HRV Stress)      │
│  • CanonicalDataReconciler (Priority-Based Deduplication & Timestamp Alignment)        │
│  • CapabilityRegistry (Per-Vendor Feature Truth Matrix)                                │
└─────────────────────┬────────────────────────────────────────────┬─────────────────────┘
                      │                                            │
┌─────────────────────▼────────────────────┐  ┌────────────────────▼─────────────────────┐
│             Repository Layer             │  │            Wearable Manager              │
│  • ActivityRepository   • SleepRepo      │  │        (Active Adapter Selector)         │
│  • HealthRepository     • WorkoutRepo    │  └────────────────────┬─────────────────────┘
│  • DataProvenanceRepo   • GoalRepo       │                       │
└─────────────────────┬────────────────────┘  ┌────────────────────┴─────────────────────┐
                      │                       │            Wearable Adapters             │
┌─────────────────────▼────────────────────┐  │  • PebbleQoreAdapter (Direct BLE 0xFEE0) │
│       Persistence (Senswear DB v2)       │  │  • StandardBleHeartRateAdapter (0x180D)  │
│  • daily_activity     • sleep_sessions   │  │  • HealthConnectWearableAdapter          │
│  • heart_rate         • spo2 / hrv       │  │  • Whoop / Garmin Cloud Sync Plugins     │
│  • workouts           • provenance (v2)  │  └──────────────────────────────────────────┘
└──────────────────────────────────────────┘
```

---

## 🔬 Authoritative Data Provenance Model

Every health measurement recorded into the system encapsulates an immutable audit trail:

```kotlin
data class DataProvenance(
    val metricName: String,               // e.g. "heart_rate", "daily_steps", "hrv_rmssd"
    val canonicalValue: Double,           // Normalized numeric value
    val canonicalUnit: String,            // "bpm", "steps", "meters", "celsius", "kcal", "ms"
    val timestampEpochMs: Long,           // Measurement epoch timestamp (UTC)
    val startTimeEpochMs: Long? = null,
    val endTimeEpochMs: Long? = null,
    val sourceDeviceName: String,         // e.g. "Pebble Qore 2", "Polar H10", "Whoop 4.0"
    val sourceDeviceId: String,           // Bluetooth MAC or Cloud Session ID
    val sourceVendor: String,             // "Pebble", "Polar", "Samsung", "Garmin", "Whoop"
    val sourceProtocol: WearableProtocol, // BLE_GATT_STANDARD, BLE_VENDOR_QORE2, HEALTH_CONNECT, VENDOR_CLOUD_API
    val dataQuality: DataQuality,         // EXCELLENT, GOOD, DEGRADED, ESTIMATED, UNRELIABLE
    val confidenceScore: Float = 1.0f,    // 0.0 .. 1.0
    val isEstimated: Boolean = false,     // True if physiologically derived
    val syncTimestampEpochMs: Long = System.currentTimeMillis()
)
```

---

## 🛠️ Installation, Setup & Build Guide

### Prerequisites
- **Android Studio**: Ladybug (2024.2.1+) or newer.
- **JDK**: OpenJDK 17 or OpenJDK 21 configured in `JAVA_HOME`.
- **Android SDK**: API Level 36 (Android 16), SDK Build-Tools 36.0.0.
- **Physical Device / Emulator**: Android 8.0 (API 26) or higher. Bluetooth LE features require a physical Android device.

---

### 1. Clone & Setup Project
```bash
git clone https://github.com/krtvysinghh/Senswear.git
cd Senswear
```

---

### 2. Verify JDK & Environment
Ensure JDK 17 is active:
```bash
export JAVA_HOME="/opt/homebrew/opt/openjdk@17" # macOS Homebrew example
java -version
```

---

### 3. Run Automated Tests
Execute the comprehensive unit test suite (domain math, BLE decoders, provenance, cloud normalizers):
```bash
./gradlew testDebugUnitTest
```

---

### 4. Build Debug APK
```bash
./gradlew assembleDebug
```
The generated APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

---

### 5. Build Hardened Release APK (with R8 Minification)
```bash
./gradlew assembleRelease
```
The optimized, ProGuard/R8-minified release APK (approx. 2.7 MB) will be generated at:
`app/build/outputs/apk/release/app-release-unsigned.apk`

---

### 6. Install to Physical Device via ADB
Connect your Android phone via USB with USB Debugging enabled:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

### 7. Android Permissions & Health Connect Setup
1. **Bluetooth Permissions**: When prompted, grant **Nearby Devices (Bluetooth Scan & Connect)**.
2. **Google Health Connect**:
   - Ensure the **Health Connect** app (or system service on Android 14+) is installed.
   - When launching Senswear, tap **Sync Now** or navigate to **Settings** to grant read/write permissions for Steps, Heart Rate, Sleep, and Workouts.

---

## 🧩 Developer Guide: Implementing a New `WearableAdapter`

Adding a new smartwatch or sensor integration does not require modifying core business logic or UI code:

1. **Create the Adapter Class**:
   Implement `WearableAdapter` in `core/wearable/adapters/`:
   ```kotlin
   class MyNewSensorAdapter(private val context: Context) : WearableAdapter {
       override val brand: WearableBrand = WearableBrand.GENERIC_BLE
       override val integrationType: WearableIntegrationType = WearableIntegrationType.STANDARD_GATT_BLE
       override val capabilities: Map<WearableCapability, CapabilityState> = CapabilityRegistry.getCapabilities(brand)

       override val connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
       override val liveMetrics = MutableStateFlow<FitnessSnapshot?>(null)
       override val currentDevice = MutableStateFlow<WearableDevice?>(null)
       override val rawPacketLogs = MutableStateFlow<List<String>>(emptyList())

       override suspend fun connect(macAddress: String?): Result<Unit> { /* GATT handshake */ }
       override suspend fun disconnect(): Result<Unit> { /* Teardown */ }
       override suspend fun syncHistory(): Result<SyncReport> { /* Batch sync */ }
       override suspend fun getBattery(): BatteryState? { /* Battery query */ }
       override suspend fun startWorkout(type: WorkoutType): Result<WorkoutSession> { /* Workout session */ }
       override suspend fun stopWorkout(): Result<WorkoutSession?> { /* End session */ }
       override suspend fun triggerHapticAlert(type: Int): Result<Unit> { /* Haptic command */ }
   }
   ```

2. **Register in `WearableManager`**:
   Add the adapter mapping inside `WearableManager.getOrCreateAdapter(brand)`.

3. **Register Capabilities in `CapabilityRegistry`**:
   Declare supported and unsupported capabilities inside `CapabilityRegistry.getCapabilities(brand)`.

---

## 📁 Repository Structure

```
Senswear/
├── app/
│   ├── src/main/
│   │   ├── java/com/senswear/app/
│   │   │   ├── MainActivity.kt                      # Main Activity entry point
│   │   │   ├── SenswearApp.kt                       # Application singleton & dependency wiring
│   │   │   ├── core/
│   │   │   │   ├── ble/                             # Bluetooth LE GATT Central client & decoders
│   │   │   │   ├── data/                            # SQLite Database (v2), entities, and repositories
│   │   │   │   ├── designsystem/                    # Liquid Glass UI components & Theme
│   │   │   │   ├── domain/                          # Physiological engine & canonical models
│   │   │   │   ├── healthconnect/                   # Google Health Connect client bridge
│   │   │   │   ├── reconciliation/                  # Timestamp aligner & source deduplicator
│   │   │   │   └── wearable/                        # Universal Adapter engine, capabilities & cloud plugins
│   │   │   ├── feature/
│   │   │   │   ├── activity/                        # Daily movement, cadence, step distribution
│   │   │   │   ├── device/                          # Wearables Hub, radar scanner, diagnostics
│   │   │   │   ├── health/                          # Cardiac telemetry, SpO2, HRV, temp, stress
│   │   │   │   ├── history/                         # Long-term trends and calendar views
│   │   │   │   ├── home/                            # Hero dashboard, Dynamic Island, live ECG wave
│   │   │   │   ├── onboarding/                      # Device discovery and permission setup
│   │   │   │   ├── settings/                        # Goal configurations, units, data export/purge
│   │   │   │   ├── sleep/                           # Sleep score & hypnogram stages
│   │   │   │   └── workouts/                        # Live workout tracking & HR zone meter
│   │   │   └── navigation/                          # Adaptive navigation host (Rail / Bottom Bar)
│   │   └── res/                                     # Vector drawables, luxury app icon, strings
│   ├── src/test/                                    # Comprehensive automated unit & integration tests
│   ├── build.gradle.kts                             # App module build definition & R8 configuration
│   └── proguard-rules.pro                           # Production R8 keep & optimization rules
├── docs/                                            # In-depth architectural & protocol specifications
├── build.gradle.kts                                 # Project-level Gradle build configuration
└── settings.gradle.kts                              # Gradle settings & plugin repositories
```

---

## 🔒 Security, Privacy & Legal

- **Zero Cloud Tracking**: No telemetry, no third-party analytics SDKs, no ad IDs, and no accounts.
- **Local Data Sovereignty**: All biometric history is stored strictly in the private app sandbox SQLite database.
- **Health Disclaimer**: Senswear is designed for personal fitness and wellness tracking. Metrics provided are not intended for medical diagnosis, clinical treatment, or medical decision-making.

---

## 📄 License

Senswear is open source released under the **[Apache License 2.0](LICENSE)**.
