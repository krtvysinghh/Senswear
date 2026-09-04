package com.senswear.app.domain

import com.senswear.app.core.domain.model.WearableBrand
import org.junit.Assert.assertEquals
import org.junit.Test

class WearableBrandTest {

    @Test
    fun `classifyDevice correctly categorizes major smartwatch and wearable brands`() {
        assertEquals(WearableBrand.APPLE_WATCH, WearableBrand.classifyDevice("Apple Watch Ultra"))
        assertEquals(WearableBrand.SAMSUNG_GALAXY_WATCH, WearableBrand.classifyDevice("Galaxy Watch6 Classic (SM-R960)"))
        assertEquals(WearableBrand.WHOOP_STRAP, WearableBrand.classifyDevice("WHOOP 4.0 - 12894"))
        assertEquals(WearableBrand.GARMIN, WearableBrand.classifyDevice("Garmin Forerunner 965"))
        assertEquals(WearableBrand.FITBIT, WearableBrand.classifyDevice("Fitbit Charge 6"))
        assertEquals(WearableBrand.OURA_RING, WearableBrand.classifyDevice("Oura Ring Gen3"))
        assertEquals(WearableBrand.PEBBLE_QORE_2, WearableBrand.classifyDevice("Pebble Qore 2"))
        assertEquals(WearableBrand.POLAR, WearableBrand.classifyDevice("Polar H10 93821"))
        assertEquals(WearableBrand.GENERIC_BLE, WearableBrand.classifyDevice("HRM-Dual Sensor"))
    }
}
