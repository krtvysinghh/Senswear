package com.senswear.app.core.wearable

import com.senswear.app.core.domain.model.BatteryState
import com.senswear.app.core.domain.model.ConnectionState
import com.senswear.app.core.domain.model.FitnessSnapshot
import com.senswear.app.core.domain.model.WearableBrand
import com.senswear.app.core.domain.model.WearableDevice
import com.senswear.app.core.domain.model.WorkoutSession
import com.senswear.app.core.domain.model.WorkoutType
import kotlinx.coroutines.flow.StateFlow

data class SyncReport(
    val recordsSynced: Int,
    val durationMs: Long,
    val success: Boolean,
    val errorMessage: String? = null
)

interface WearableAdapter {
    val brand: WearableBrand
    val integrationType: WearableIntegrationType
    val capabilities: Map<WearableCapability, CapabilityState>
    val connectionState: StateFlow<ConnectionState>
    val liveMetrics: StateFlow<FitnessSnapshot?>
    val currentDevice: StateFlow<WearableDevice?>
    val rawPacketLogs: StateFlow<List<String>>

    fun isCapabilitySupported(capability: WearableCapability): Boolean {
        return capabilities[capability]?.status == CapabilityStatus.SUPPORTED
    }

    suspend fun connect(macAddress: String? = null): Result<Unit>
    suspend fun disconnect(): Result<Unit>
    suspend fun syncHistory(): Result<SyncReport>
    suspend fun getBattery(): BatteryState?
    suspend fun startWorkout(type: WorkoutType): Result<WorkoutSession>
    suspend fun stopWorkout(): Result<WorkoutSession?>
    suspend fun triggerHapticAlert(type: Int): Result<Unit>
}
