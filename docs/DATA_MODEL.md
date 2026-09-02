# Senswear Domain & Persistence Data Models

Senswear enforces strict internal unit standardization (SI units) across the domain and database layers. Conversions (e.g. kilometers to miles, Celsius to Fahrenheit) occur exclusively at the presentation UI layer.

---

## 1. Domain Models

| Domain Model | Primary Properties | Canonical SI Units |
| :--- | :--- | :--- |
| `FitnessSnapshot` | Steps, distance, calories, live HR, SpO2, HRV, stress, temp, battery | Meters, BPM, %, ms, °C, % |
| `DailyActivity` | epochDay, steps, goal, distance, activeCal, totalCal, activeMins, hourlySteps | Meters, kcal, minutes |
| `HeartRateReading`| timestamp, bpm, restingBpm, source, zone | Epoch ms, BPM |
| `Spo2Reading` | timestamp, percentage (70..100), source | Epoch ms, % |
| `HrvReading` | timestamp, rmssdMs, sdnnMs, source | Epoch ms, ms |
| `SleepSession` | id, startTime, endTime, durationMinutes, deep, light, rem, awake, sleepScore | Epoch ms, minutes |
| `StressReading` | timestamp, score (0..100), level, source | Epoch ms, score |
| `TemperatureReading`| timestamp, temperatureCelsius, baselineDeltaCelsius | Epoch ms, °C |
| `WorkoutSession` | id, type, startTime, endTime, durationSeconds, totalDistance, calories, avgHr | Meters, kcal, BPM |
| `WearableDevice` | id, name, macAddress, firmwareVersion, rssi, connectionState, batteryState | dBm, % |
| `Goal` | id, title, targetValue, unit, currentValue, type | — |
| `Achievement` | id, title, description, category, isUnlocked, unlockedTime, progress | % |

---

## 2. Local Database Schema (`senswear.db`)

The local persistence layer contains 11 optimized tables with secondary indices:

1. `daily_activity` (Primary Key: `epoch_day`)
2. `heart_rate_readings` (Primary Key: `id`, Indexed on: `timestamp`)
3. `spo2_readings` (Primary Key: `id`, Indexed on: `timestamp`)
4. `hrv_readings` (Primary Key: `id`, Indexed on: `timestamp`)
5. `stress_readings` (Primary Key: `id`, Indexed on: `timestamp`)
6. `temperature_readings` (Primary Key: `id`, Indexed on: `timestamp`)
7. `sleep_sessions` (Primary Key: `id`, Indexed on: `start_time`)
8. `workouts` (Primary Key: `id`, Indexed on: `start_time`)
9. `goals` (Primary Key: `id`)
10. `achievements` (Primary Key: `id`)
11. `sync_events` (Primary Key: `id`, Indexed on: `timestamp`)
