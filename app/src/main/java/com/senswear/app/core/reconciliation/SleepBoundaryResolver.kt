package com.senswear.app.core.reconciliation

import com.senswear.app.core.domain.model.SleepSession
import java.util.Calendar

object SleepBoundaryResolver {
    fun assignSleepToWakeDate(session: SleepSession): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = session.endTimeEpochMs
        }
        return (session.endTimeEpochMs / (1000 * 60 * 60 * 24))
    }
}
