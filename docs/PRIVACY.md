# Senswear Privacy Policy & Data Sovereignty

Senswear is founded on strict privacy-first and local-first architecture.

---

## 1. Zero Cloud Dependency
- **No Remote Servers**: Senswear does not operate remote database servers, backend APIs, or user authentication systems.
- **Zero Telemetry / Analytics**: Senswear contains no third-party tracking, crash analytics SDKs, advertising frameworks, or user behavioral monitoring.
- **Local Storage**: All biometric and activity records (Heart Rate, Steps, SpO₂, HRV, Stress, Sleep, Temperature, and Workouts) reside exclusively on your physical Android device in an encrypted SQLite sandbox (`senswear.db`).

---

## 2. Bluetooth Low Energy Privacy
- Bluetooth communication is strictly point-to-point between your phone and your Pebble Qore 2 wearable band.
- Bluetooth MAC addresses and telemetry packets are processed locally and never transmitted across the network.

---

## 3. Google Health Connect Privacy
- Data shared with Google Health Connect is governed by your device's operating system security controls.
- Senswear requests only read/write access necessary for user-directed fitness management.

---

## 4. User Data Control
- **Export Data**: Users can export their complete biometric history into standard structured JSON from Settings.
- **Purge All Data**: Users can permanently erase all local database records and configuration settings at any time with a single tap.
