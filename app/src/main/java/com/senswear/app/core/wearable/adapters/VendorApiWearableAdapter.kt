package com.senswear.app.core.wearable.adapters

import com.senswear.app.core.domain.model.BatteryState
import com.senswear.app.core.domain.model.ConnectionState
import com.senswear.app.core.domain.model.FitnessSnapshot
import com.senswear.app.core.domain.model.WearableBrand
import com.senswear.app.core.domain.model.WearableDevice
import com.senswear.app.core.domain.model.WorkoutSession
import com.senswear.app.core.domain.model.WorkoutType
import com.senswear.app.core.wearable.CapabilityRegistry
import com.senswear.app.core.wearable.CapabilityState
import com.senswear.app.core.wearable.SyncReport
import com.senswear.app.core.wearable.WearableAdapter
import com.senswear.app.core.wearable.WearableCapability
import com.senswear.app.core.wearable.WearableIntegrationType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Honest Adapter for smartwatches/wearables that require official proprietary Cloud APIs, OEM SDKs,
 * or are closed/unsupported for direct third-party Android BLE access (e.g. Apple Watch on Android, Garmin Cloud API).
 */
class VendorApiWearableAdapter(
    override val brand: WearableBrand,
    override val integrationType: WearableIntegrationType = WearableIntegrationType.VENDOR_API_REQUIRED,
    private val requirementNote: String = "Requires official vendor developer credentials or cloud API OAuth integration."
) : WearableAdapter {

    override val capabilities: Map<WearableCapability, CapabilityState> = CapabilityRegistry.getCapabilities(brand)

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _liveMetrics = MutableStateFlow<FitnessSnapshot?>(null)
    override val liveMetrics: StateFlow<FitnessSnapshot?> = _liveMetrics.asStateFlow()

    private val _currentDevice = MutableStateFlow<WearableDevice?>(null)
    override val currentDevice: StateFlow<WearableDevice?> = _currentDevice.asStateFlow()

    private val _rawPacketLogs = MutableStateFlow<List<String>>(listOf("[$brand] $requirementNote"))
    override val rawPacketLogs: StateFlow<List<String>> = _rawPacketLogs.asStateFlow()

    override suspend fun connect(macAddress: String?): Result<Unit> {
        _connectionState.value = ConnectionState.ERROR
        return Result.failure(
            UnsupportedOperationException(
                "Direct connection to ${brand.displayName} is restricted. $requirementNote"
            )
        )
    }

    override suspend fun disconnect(): Result<Unit> {
        _connectionState.value = ConnectionState.DISCONNECTED
        return Result.success(Unit)
    }

    override suspend fun syncHistory(): Result<SyncReport> {
        return Result.failure(UnsupportedOperationException("Cloud API sync requires vendor OAuth token"))
    }

    override suspend fun getBattery(): BatteryState? = null

    override suspend fun startWorkout(type: WorkoutType): Result<WorkoutSession> {
        return Result.failure(UnsupportedOperationException("Workout control not available via direct BLE for ${brand.displayName}"))
    }

    override suspend fun stopWorkout(): Result<WorkoutSession?> = Result.success(null)

    override suspend fun triggerHapticAlert(type: Int): Result<Unit> {
        return Result.failure(UnsupportedOperationException("Haptics not available for ${brand.displayName}"))
    }
}
