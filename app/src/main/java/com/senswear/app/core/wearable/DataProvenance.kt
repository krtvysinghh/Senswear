package com.senswear.app.core.wearable

enum class DataQuality {
    EXCELLENT,
    GOOD,
    DEGRADED,
    ESTIMATED,
    UNRELIABLE
}

enum class WearableProtocol {
    BLE_GATT_STANDARD,
    BLE_VENDOR_QORE2,
    HEALTH_CONNECT_AGGREGATION,
    VENDOR_CLOUD_API,
    VENDOR_PROPRIETARY_SDK,
    SYSTEM_CALCULATED
}

data class DataProvenance(
    val metricName: String,
    val canonicalValue: Double,
    val canonicalUnit: String,
    val timestampEpochMs: Long,
    val startTimeEpochMs: Long? = null,
    val endTimeEpochMs: Long? = null,
    val sourceDeviceName: String,
    val sourceDeviceId: String,
    val sourceVendor: String,
    val sourceProtocol: WearableProtocol,
    val dataQuality: DataQuality = DataQuality.GOOD,
    val confidenceScore: Float = 1.0f,
    val isEstimated: Boolean = false,
    val syncTimestampEpochMs: Long = System.currentTimeMillis(),
    val rawPayloadFingerprint: String? = null
)
