package com.senswear.app.core.wearable

import com.senswear.app.core.domain.model.WearableBrand

object CapabilityRegistry {

    fun getCapabilities(brand: WearableBrand): Map<WearableCapability, CapabilityState> {
        return when (brand) {
            WearableBrand.PEBBLE_QORE_2 -> mapOf(
                WearableCapability.STEPS to CapabilityState(CapabilityStatus.SUPPORTED),
                WearableCapability.HEART_RATE to CapabilityState(CapabilityStatus.SUPPORTED),
                WearableCapability.RESTING_HEART_RATE to CapabilityState(CapabilityStatus.SUPPORTED),
                WearableCapability.HRV to CapabilityState(CapabilityStatus.SUPPORTED),
                WearableCapability.SPO2 to CapabilityState(CapabilityStatus.SUPPORTED),
                WearableCapability.BODY_TEMPERATURE to CapabilityState(CapabilityStatus.SUPPORTED),
                WearableCapability.CALORIES to CapabilityState(CapabilityStatus.SUPPORTED),
                WearableCapability.DISTANCE to CapabilityState(CapabilityStatus.SUPPORTED),
                WearableCapability.SLEEP to CapabilityState(CapabilityStatus.SUPPORTED),
                WearableCapability.SLEEP_STAGES to CapabilityState(CapabilityStatus.SUPPORTED),
                WearableCapability.STRESS to CapabilityState(CapabilityStatus.SUPPORTED),
                WearableCapability.BATTERY to CapabilityState(CapabilityStatus.SUPPORTED),
                WearableCapability.HAPTICS to CapabilityState(CapabilityStatus.SUPPORTED),
                WearableCapability.REAL_TIME_STREAMING to CapabilityState(CapabilityStatus.SUPPORTED),
                WearableCapability.HISTORICAL_SYNC to CapabilityState(CapabilityStatus.SUPPORTED),
                WearableCapability.GPS to CapabilityState(CapabilityStatus.UNSUPPORTED, "Screen-free hardware lacks onboard GPS receiver; uses phone GPS")
            )

            WearableBrand.POLAR -> mapOf(
                WearableCapability.HEART_RATE to CapabilityState(CapabilityStatus.SUPPORTED),
                WearableCapability.HRV to CapabilityState(CapabilityStatus.SUPPORTED, "Extracted from GATT 0x2A37 RR-intervals"),
                WearableCapability.BATTERY to CapabilityState(CapabilityStatus.SUPPORTED),
                WearableCapability.REAL_TIME_STREAMING to CapabilityState(CapabilityStatus.SUPPORTED),
                WearableCapability.STEPS to CapabilityState(CapabilityStatus.UNSUPPORTED, "HRM sensor does not transmit step telemetry"),
                WearableCapability.SLEEP to CapabilityState(CapabilityStatus.UNSUPPORTED)
            )

            WearableBrand.SAMSUNG_GALAXY_WATCH -> mapOf(
                WearableCapability.STEPS to CapabilityState(CapabilityStatus.SUPPORTED, "Aggregated via Google Health Connect"),
                WearableCapability.HEART_RATE to CapabilityState(CapabilityStatus.SUPPORTED, "Available via Health Connect and standard BLE Broadcast mode"),
                WearableCapability.SLEEP to CapabilityState(CapabilityStatus.SUPPORTED, "Aggregated via Google Health Connect"),
                WearableCapability.WORKOUTS to CapabilityState(CapabilityStatus.SUPPORTED, "Aggregated via Google Health Connect"),
                WearableCapability.SPO2 to CapabilityState(CapabilityStatus.SUPPORTED, "Aggregated via Google Health Connect"),
                WearableCapability.REAL_TIME_STREAMING to CapabilityState(CapabilityStatus.SUPPORTED, "When HRM Broadcast mode is enabled on watch"),
                WearableCapability.HISTORICAL_SYNC to CapabilityState(CapabilityStatus.SUPPORTED, "Via Health Connect Background Sync")
            )

            WearableBrand.APPLE_WATCH -> mapOf(
                WearableCapability.HEART_RATE to CapabilityState(CapabilityStatus.SUPPORTED, "When BLE Heart Rate Broadcast is active in workout mode"),
                WearableCapability.REAL_TIME_STREAMING to CapabilityState(CapabilityStatus.SUPPORTED, "Standard GATT 0x180D broadcast mode"),
                WearableCapability.STEPS to CapabilityState(CapabilityStatus.UNSUPPORTED, "Apple HealthKit does not run natively on Android"),
                WearableCapability.SLEEP to CapabilityState(CapabilityStatus.UNSUPPORTED, "Proprietary watchOS sleep database requires iOS companion"),
                WearableCapability.HISTORICAL_SYNC to CapabilityState(CapabilityStatus.UNSUPPORTED, "Direct historical sync unsupported without iOS companion app")
            )

            WearableBrand.WHOOP_STRAP -> mapOf(
                WearableCapability.HEART_RATE to CapabilityState(CapabilityStatus.SUPPORTED, "When Whoop BLE HR Broadcast mode is enabled"),
                WearableCapability.HRV to CapabilityState(CapabilityStatus.SUPPORTED, "Via BLE RR-interval stream"),
                WearableCapability.REAL_TIME_STREAMING to CapabilityState(CapabilityStatus.SUPPORTED),
                WearableCapability.RECOVERY to CapabilityState(CapabilityStatus.REQUIRES_VENDOR_API, "Whoop proprietary strain & recovery algorithms require Whoop Cloud API OAuth"),
                WearableCapability.SLEEP to CapabilityState(CapabilityStatus.REQUIRES_VENDOR_API, "Nocturnal sleep staging requires Whoop Cloud API sync")
            )

            WearableBrand.GARMIN -> mapOf(
                WearableCapability.HEART_RATE to CapabilityState(CapabilityStatus.SUPPORTED, "When Garmin 'Broadcast Heart Rate' BLE mode is enabled"),
                WearableCapability.REAL_TIME_STREAMING to CapabilityState(CapabilityStatus.SUPPORTED),
                WearableCapability.STEPS to CapabilityState(CapabilityStatus.SUPPORTED, "Aggregated via Garmin Connect & Google Health Connect"),
                WearableCapability.WORKOUTS to CapabilityState(CapabilityStatus.SUPPORTED, "Aggregated via Google Health Connect"),
                WearableCapability.HISTORICAL_SYNC to CapabilityState(CapabilityStatus.REQUIRES_VENDOR_API, "Full direct historical telemetry requires Garmin Connect Developer Program API")
            )

            WearableBrand.FITBIT -> mapOf(
                WearableCapability.STEPS to CapabilityState(CapabilityStatus.SUPPORTED, "Aggregated via Google Health Connect"),
                WearableCapability.HEART_RATE to CapabilityState(CapabilityStatus.SUPPORTED, "Aggregated via Google Health Connect"),
                WearableCapability.SLEEP to CapabilityState(CapabilityStatus.SUPPORTED, "Aggregated via Google Health Connect"),
                WearableCapability.WORKOUTS to CapabilityState(CapabilityStatus.SUPPORTED, "Aggregated via Google Health Connect")
            )

            WearableBrand.OURA_RING -> mapOf(
                WearableCapability.SLEEP to CapabilityState(CapabilityStatus.SUPPORTED, "Aggregated via Google Health Connect"),
                WearableCapability.BODY_TEMPERATURE to CapabilityState(CapabilityStatus.SUPPORTED, "Aggregated via Google Health Connect"),
                WearableCapability.HRV to CapabilityState(CapabilityStatus.SUPPORTED, "Aggregated via Google Health Connect"),
                WearableCapability.REAL_TIME_STREAMING to CapabilityState(CapabilityStatus.REQUIRES_VENDOR_API, "Oura Ring uses encrypted BLE transport; full telemetry via Oura Cloud API")
            )

            WearableBrand.GENERIC_BLE -> mapOf(
                WearableCapability.HEART_RATE to CapabilityState(CapabilityStatus.SUPPORTED, "Standard GATT 0x180D Profile"),
                WearableCapability.BATTERY to CapabilityState(CapabilityStatus.SUPPORTED, "Standard GATT 0x180F Profile"),
                WearableCapability.REAL_TIME_STREAMING to CapabilityState(CapabilityStatus.SUPPORTED)
            )
        }
    }
}
