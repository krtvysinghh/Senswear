package com.senswear.app.reconciliation

import com.senswear.app.core.domain.model.SleepSession
import com.senswear.app.core.reconciliation.SleepBoundaryResolver
import com.senswear.app.core.reconciliation.TimestampAligner
import org.junit.Assert.assertEquals
import org.junit.Test

class TimezoneSyncTest {

    @Test
    fun `TimestampAligner accurately rounds down to minute boundaries`() {
        val rawTime = 1700000045123L
        val aligned = TimestampAligner.alignToMinuteBucket(rawTime)
        assertEquals(1700000040000L, aligned)
    }

    @Test
    fun `SleepBoundaryResolver handles epoch day boundary correctly`() {
        val wakeEpoch = 1700030400000L
        val session = SleepSession(
            id = "s_tz",
            startTimeEpochMs = wakeEpoch - (8 * 3600 * 1000L),
            endTimeEpochMs = wakeEpoch,
            durationMinutes = 480,
            deepMinutes = 100,
            lightMinutes = 250,
            remMinutes = 100,
            awakeMinutes = 30,
            sleepScore = 85
        )

        val day = SleepBoundaryResolver.assignSleepToWakeDate(session)
        assertEquals(wakeEpoch / (1000 * 60 * 60 * 24), day)
    }
}
