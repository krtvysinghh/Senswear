package com.senswear.app.core.sync

import androidx.work.Constraints
import androidx.work.NetworkType

object SyncConstraintsFactory {
    fun createBackgroundSyncConstraints(): Constraints {
        return Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(true)
            .setRequiresStorageNotLow(true)
            .build()
    }
}
