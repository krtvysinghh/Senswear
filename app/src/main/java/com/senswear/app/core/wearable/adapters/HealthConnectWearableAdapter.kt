package com.senswear.app.core.wearable.adapters

import com.senswear.app.core.domain.model.BatteryState
import com.senswear.app.core.domain.model.ConnectionState
import com.senswear.app.core.domain.model.FitnessSnapshot
import com.senswear.app.core.domain.model.WearableBrand
import com.senswear.app.core.domain.model.WearableDevice
import com.senswear.app.core.domain.model.WorkoutSession
import com.senswear.app.core.domain.model.WorkoutType
import com.senswear.app.core.healthconnect.HealthConnectManager
import com.senswear.app.core.wearable.CapabilityRegistry
import com.senswear.app.core.wearable.CapabilityState
import com.senswear.app.core.wearable.SyncReport
import com.senswear.app.core.wearable.WearableAdapter
import com.senswear.app.core.wearable.WearableCapability
import com.senswear.app.core.wearable.WearableIntegrationType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant

class HealthConnectWearableAdapter(
    private val healthConnectManager: HealthConnectManager,
    override val brand: WearableBrand
) : WearableAdapter {

    override val integrationType: WearableIntegrationType = WearableIntegrationType.HEALTH_CONNECT_AGGREGATED
    override val capabilities: Map<WearableCapability, CapabilityState> = CapabilityRegistry.getCapabilities(brand)

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _liveMetrics = MutableStateFlow<FitnessSnapshot?>(null)
    override val liveMetrics: StateFlow<FitnessSnapshot?> = _liveMetrics.asStateFlow()

    private val _currentDevice = MutableStateFlow<WearableDevice?>(null)
    override val currentDevice: StateFlow<WearableDevice?> = _currentDevice.asStateFlow()

    private val _rawPacketLogs = MutableStateFlow<List<String>>(emptyList())
    override val rawPacketLogs: StateFlow<List<String>> = _rawPacketLogs.asStateFlow()

    override suspend fun connect(macAddress: String?): Result<Unit> {
        val available = healthConnectManager.isAvailable()
        if (!available) {
            _connectionState.value = ConnectionState.ERROR
            return Result.failure(IllegalStateException("Health Connect is not available on this Android device"))
        }

        val hasPermissions = healthConnectManager.hasAllPermissions()
        if (!hasPermissions) {
            _connectionState.value = ConnectionState.ERROR
            return Result.failure(SecurityException("Health Connect permissions have not been granted"))
        }

        _connectionState.value = ConnectionState.CONNECTED
        _currentDevice.value = WearableDevice(
            id = "health_connect_${brand.name.lowercase()}",
            name = "${brand.displayName} (Health Connect)",
            macAddress = "N/A (Health Connect Bridge)",
            connectionState = ConnectionState.CONNECTED
        )
        return Result.success(Unit)
    }

    override suspend fun disconnect(): Result<Unit> {
        _connectionState.value = ConnectionState.DISCONNECTED
        _currentDevice.value = null
        _liveMetrics.value = null
        return Result.success(Unit)
    }

    override suspend fun syncHistory(): Result<SyncReport> {
        if (!healthConnectManager.hasAllPermissions()) {
            return Result.failure(SecurityException("Health Connect permissions missing"))
        }
        val startTime = Instant.now().minusSeconds(86400)
        val endTime = Instant.now()
        val records = healthConnectManager.readHeartRateHistory(startTime, endTime)
        return Result.success(
            SyncReport(
                recordsSynced = records.size,
                durationMs = 250,
                success = true
            )
        )
    }

    override suspend fun getBattery(): BatteryState? = null

    override suspend fun startWorkout(type: WorkoutType): Result<WorkoutSession> {
        return Result.failure(UnsupportedOperationException("Health Connect is a data aggregation layer, not a real-time BLE GATT controller"))
    }

    override suspend fun stopWorkout(): Result<WorkoutSession?> = Result.success(null)

    override suspend fun triggerHapticAlert(type: Int): Result<Unit> {
        return Result.failure(UnsupportedOperationException("Health Connect does not support remote haptic triggering"))
    }
}
