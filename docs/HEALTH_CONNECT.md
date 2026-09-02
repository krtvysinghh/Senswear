# Google Health Connect Integration

Senswear integrates deeply with Google Health Connect (`androidx.health.connect:connect-client:1.1.0-alpha11`) to facilitate bi-directional data exchange with the wider Android health ecosystem.

---

## 1. Supported Record Types

| Record Type | Read | Write | Senswear Domain Mapping |
| :--- | :---: | :---: | :--- |
| `StepsRecord` | Yes | Yes | `DailyActivity.steps` & `DailyActivity.hourlySteps` |
| `DistanceRecord` | Yes | Yes | `DailyActivity.distanceMeters` |
| `TotalCaloriesBurnedRecord` | Yes | Yes | `DailyActivity.totalCaloriesKcal` |
| `HeartRateRecord` | Yes | Yes | `HeartRateReading` time-series |
| `RestingHeartRateRecord` | Yes | No | `HeartRateReading.restingHeartRateBpm` |
| `HeartRateVariabilityRmssdRecord` | Yes | No | `HrvReading.rmssdMs` |
| `OxygenSaturationRecord` | Yes | Yes | `Spo2Reading.percentage` |
| `SleepSessionRecord` | Yes | Yes | `SleepSession` and sleep stages |
| `BodyTemperatureRecord` | Yes | No | `TemperatureReading.temperatureCelsius` |
| `ExerciseSessionRecord` | Yes | Yes | `WorkoutSession` |

---

## 2. Permissions & Privacy Declarations

All Health Connect permissions are declared in `AndroidManifest.xml` alongside the Android 14+ required intent filter:
```xml
<activity-alias
    android:name="ViewPermissionUsageActivity"
    android:exported="true"
    android:targetActivity=".MainActivity"
    android:permission="android.permission.START_VIEW_PERMISSION_USAGE">
    <intent-filter>
        <action android:name="android.intent.action.VIEW_PERMISSION_USAGE" />
        <category android:name="android.intent.category.HEALTH_PERMISSIONS" />
    </intent-filter>
</activity-alias>
```
