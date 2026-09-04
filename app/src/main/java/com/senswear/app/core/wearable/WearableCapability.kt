package com.senswear.app.core.wearable

enum class WearableCapability {
    STEPS,
    HEART_RATE,
    RESTING_HEART_RATE,
    HRV,
    SPO2,
    SLEEP,
    SLEEP_STAGES,
    RESPIRATORY_RATE,
    BODY_TEMPERATURE,
    CALORIES,
    DISTANCE,
    WORKOUTS,
    GPS,
    STRESS,
    RECOVERY,
    BATTERY,
    HAPTICS,
    NOTIFICATIONS,
    MUSIC_CONTROL,
    DEVICE_TIME,
    HISTORICAL_SYNC,
    REAL_TIME_STREAMING
}

enum class CapabilityStatus {
    SUPPORTED,
    UNSUPPORTED,
    REQUIRES_PERMISSION,
    REQUIRES_VENDOR_API,
    REQUIRES_DEVICE_CONNECTION,
    TEMPORARILY_UNAVAILABLE
}

data class CapabilityState(
    val status: CapabilityStatus,
    val description: String? = null
)
