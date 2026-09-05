package com.senswear.app.core.reconciliation

import com.senswear.app.core.wearable.DataProvenance
import com.senswear.app.core.wearable.WearableProtocol

/**
 * Arbitrates conflicting metrics from simultaneous multi-wearable sources
 * based on clinical accuracy hierarchy:
 * 1. Direct BLE Chest Strap ECG (Highest accuracy)
 * 2. Direct BLE Wrist Optical (Pebble Qore 2, Polar Verity)
 * 3. Health Connect Aggregated (Galaxy Watch, Pixel Watch)
 * 4. Cloud Sync REST API (Whoop, Garmin Connect)
 */
class WearableHierarchyArbitrator {

    enum class SourceTier(val priorityRank: Int) {
        DIRECT_BLE_CHEST_ECG(1),
        DIRECT_BLE_WRIST_OPTICAL(2),
        HEALTH_CONNECT_ON_DEVICE(3),
        CLOUD_SYNC_REST(4)
    }

    fun determineTier(provenance: DataProvenance): SourceTier {
        return when (provenance.sourceProtocol) {
            WearableProtocol.BLE_GATT_STANDARD -> {
                if (provenance.sourceVendor.contains("Polar", ignoreCase = true) || provenance.sourceVendor.contains("Garmin HRM", ignoreCase = true)) {
                    SourceTier.DIRECT_BLE_CHEST_ECG
                } else {
                    SourceTier.DIRECT_BLE_WRIST_OPTICAL
                }
            }
            WearableProtocol.BLE_VENDOR_QORE2 -> SourceTier.DIRECT_BLE_WRIST_OPTICAL
            WearableProtocol.HEALTH_CONNECT_AGGREGATION -> SourceTier.HEALTH_CONNECT_ON_DEVICE
            WearableProtocol.VENDOR_CLOUD_API -> SourceTier.CLOUD_SYNC_REST
            WearableProtocol.VENDOR_PROPRIETARY_SDK -> SourceTier.CLOUD_SYNC_REST
            WearableProtocol.SYSTEM_CALCULATED -> SourceTier.CLOUD_SYNC_REST
        }
    }

    /**
     * Resolves the authoritative metric when multiple devices report in the same time window.
     */
    fun selectAuthoritativeProvenance(first: DataProvenance, second: DataProvenance): DataProvenance {
        val tier1 = determineTier(first)
        val tier2 = determineTier(second)

        return when {
            tier1.priorityRank < tier2.priorityRank -> first
            tier2.priorityRank < tier1.priorityRank -> second
            else -> if (first.confidenceScore >= second.confidenceScore) first else second
        }
    }
}
