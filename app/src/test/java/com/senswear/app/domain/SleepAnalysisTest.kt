package com.senswear.app.domain

import com.senswear.app.core.domain.model.DataSource
import com.senswear.app.core.domain.model.SleepSession
import org.junit.Assert.assertEquals
import org.junit.Test

class SleepAnalysisTest {

    @Test
    fun `SleepSession calculates correct efficiency and stage percentages`() {
        val session = SleepSession(
            id = "s1",
            startTimeEpochMs = 1700000000000L,
            endTimeEpochMs = 1700000000000L + (8 * 3600 * 1000L),
            durationMinutes = 480,
            deepMinutes = 110,
            lightMinutes = 230,
            remMinutes = 110,
            awakeMinutes = 30,
            sleepScore = 88,
            source = DataSource.PEBBLE_QORE_2_BLE
        )

        assertEquals(93, session.efficiencyPercent)
        assertEquals(88, session.sleepScore)
    }
}
