package com.senswear.app.wearable

import com.senswear.app.core.domain.model.WearableBrand
import com.senswear.app.core.wearable.CapabilityRegistry
import com.senswear.app.core.wearable.CapabilityStatus
import com.senswear.app.core.wearable.WearableCapability
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WearableCapabilityTest {

    @Test
    fun `Pebble Qore 2 supports direct continuous biometrics but marks GPS unsupported`() {
        val caps = CapabilityRegistry.getCapabilities(WearableBrand.PEBBLE_QORE_2)
        assertEquals(CapabilityStatus.SUPPORTED, caps[WearableCapability.HEART_RATE]?.status)
        assertEquals(CapabilityStatus.SUPPORTED, caps[WearableCapability.STEPS]?.status)
        assertEquals(CapabilityStatus.SUPPORTED, caps[WearableCapability.SPO2]?.status)
        assertEquals(CapabilityStatus.SUPPORTED, caps[WearableCapability.BODY_TEMPERATURE]?.status)
        assertEquals(CapabilityStatus.UNSUPPORTED, caps[WearableCapability.GPS]?.status)
    }

    @Test
    fun `Apple Watch on Android honestly declares sleep and historical sync unsupported`() {
        val caps = CapabilityRegistry.getCapabilities(WearableBrand.APPLE_WATCH)
        assertEquals(CapabilityStatus.SUPPORTED, caps[WearableCapability.HEART_RATE]?.status)
        assertEquals(CapabilityStatus.UNSUPPORTED, caps[WearableCapability.SLEEP]?.status)
        assertEquals(CapabilityStatus.UNSUPPORTED, caps[WearableCapability.HISTORICAL_SYNC]?.status)
    }

    @Test
    fun `Whoop Strap honestly declares proprietary recovery requires vendor cloud API`() {
        val caps = CapabilityRegistry.getCapabilities(WearableBrand.WHOOP_STRAP)
        assertEquals(CapabilityStatus.SUPPORTED, caps[WearableCapability.HEART_RATE]?.status)
        assertEquals(CapabilityStatus.REQUIRES_VENDOR_API, caps[WearableCapability.RECOVERY]?.status)
    }

    @Test
    fun `Galaxy Watch declares Health Connect aggregation support`() {
        val caps = CapabilityRegistry.getCapabilities(WearableBrand.SAMSUNG_GALAXY_WATCH)
        assertEquals(CapabilityStatus.SUPPORTED, caps[WearableCapability.STEPS]?.status)
        assertEquals(CapabilityStatus.SUPPORTED, caps[WearableCapability.SLEEP]?.status)
        assertNotNull(caps[WearableCapability.STEPS]?.description)
    }
}
