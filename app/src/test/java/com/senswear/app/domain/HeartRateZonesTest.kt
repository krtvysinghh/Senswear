package com.senswear.app.domain

import com.senswear.app.core.domain.model.HeartRateZone
import org.junit.Assert.assertEquals
import org.junit.Test

class HeartRateZonesTest {

    @Test
    fun `HeartRateZone correctly maps BPM to appropriate intensity zones`() {
        val maxHr = 190

        assertEquals(HeartRateZone.REST, HeartRateZone.fromBpm(70, maxHr))
        assertEquals(HeartRateZone.ZONE_1, HeartRateZone.fromBpm(105, maxHr))
        assertEquals(HeartRateZone.ZONE_2, HeartRateZone.fromBpm(125, maxHr))
        assertEquals(HeartRateZone.ZONE_3, HeartRateZone.fromBpm(145, maxHr))
        assertEquals(HeartRateZone.ZONE_4, HeartRateZone.fromBpm(165, maxHr))
        assertEquals(HeartRateZone.ZONE_5, HeartRateZone.fromBpm(185, maxHr))
    }
}
