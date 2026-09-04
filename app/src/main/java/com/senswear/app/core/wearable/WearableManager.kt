package com.senswear.app.core.wearable

import android.content.Context
import com.senswear.app.core.ble.ScannedWearable
import com.senswear.app.core.ble.UniversalWearableScanner
import com.senswear.app.core.ble.WearableConnector
import com.senswear.app.core.domain.model.BatteryState
import com.senswear.app.core.domain.model.ConnectionState
import com.senswear.app.core.domain.model.FitnessSnapshot
import com.senswear.app.core.domain.model.WearableBrand
import com.senswear.app.core.domain.model.WearableDevice
import com.senswear.app.core.domain.model.WorkoutSession
import com.senswear.app.core.domain.model.WorkoutType
import com.senswear.app.core.healthconnect.HealthConnectManager
import com.senswear.app.core.wearable.adapters.HealthConnectWearableAdapter
import com.senswear.app.core.wearable.adapters.PebbleQoreAdapter
import com.senswear.app.core.wearable.adapters.StandardBleHeartRateAdapter
import com.senswear.app.core.wearable.adapters.VendorApiWearableAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

class WearableManager(
    private val context: Context,
    private val healthConnectManager: HealthConnectManager
) : WearableConnector {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val scanner = UniversalWearableScanner(context)

    private val adapterMap = mutableMapOf<WearableBrand, WearableAdapter>()

    private val _selectedBrand = MutableStateFlow(WearableBrand.PEBBLE_QORE_2)
    val selectedBrand: StateFlow<WearableBrand> = _selectedBrand.asStateFlow()

    private val _activeAdapter = MutableStateFlow<WearableAdapter>(getOrCreateAdapter(WearableBrand.PEBBLE_QORE_2))
    val activeAdapter: StateFlow<WearableAdapter> = _activeAdapter.asStateFlow()

    override val connectionState: StateFlow<ConnectionState> = _activeAdapter.flatMapLatest { it.connectionState }
        .stateIn(scope, SharingStarted.Eagerly, ConnectionState.DISCONNECTED)

    override val liveMetrics: StateFlow<FitnessSnapshot> = _activeAdapter.flatMapLatest { adapter ->
        adapter.liveMetrics
    }.flatMapLatest { snapshot ->
        MutableStateFlow(snapshot ?: FitnessSnapshot())
    }.stateIn(scope, SharingStarted.Eagerly, FitnessSnapshot())

    override val currentDevice: StateFlow<WearableDevice?> = _activeAdapter.flatMapLatest { it.currentDevice }
        .stateIn(scope, SharingStarted.Eagerly, null)

    override val rawPacketLogs: StateFlow<List<String>> = _activeAdapter.flatMapLatest { it.rawPacketLogs }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    fun selectBrand(brand: WearableBrand) {
        _selectedBrand.value = brand
        val adapter = getOrCreateAdapter(brand)
        _activeAdapter.value = adapter
    }

    private fun getOrCreateAdapter(brand: WearableBrand): WearableAdapter {
        return adapterMap.getOrPut(brand) {
            when (brand) {
                WearableBrand.PEBBLE_QORE_2 -> PebbleQoreAdapter(context)
                WearableBrand.POLAR, WearableBrand.GENERIC_BLE -> StandardBleHeartRateAdapter(context, brand)
                WearableBrand.SAMSUNG_GALAXY_WATCH, WearableBrand.FITBIT -> HealthConnectWearableAdapter(healthConnectManager, brand)
                WearableBrand.APPLE_WATCH -> StandardBleHeartRateAdapter(context, brand)
                WearableBrand.WHOOP_STRAP, WearableBrand.GARMIN, WearableBrand.OURA_RING -> VendorApiWearableAdapter(
                    brand = brand,
                    integrationType = WearableIntegrationType.VENDOR_API_REQUIRED,
                    requirementNote = "Requires official ${brand.displayName} developer integration or cloud sync API."
                )
            }
        }
    }

    override suspend fun connect(macAddress: String?) {
        _activeAdapter.value.connect(macAddress)
    }

    override suspend fun disconnect() {
        _activeAdapter.value.disconnect()
    }

    override suspend fun syncHistory(): Result<Int> {
        val result = _activeAdapter.value.syncHistory()
        return result.map { it.recordsSynced }
    }

    override suspend fun getDeviceInfo(): WearableDevice? = _activeAdapter.value.currentDevice.value

    override suspend fun getBattery(): BatteryState {
        return _activeAdapter.value.getBattery() ?: BatteryState(percentage = 0, isCharging = false, estimatedDaysRemaining = 0)
    }

    override suspend fun startWorkout(type: WorkoutType): Result<WorkoutSession> {
        return _activeAdapter.value.startWorkout(type)
    }

    override suspend fun stopWorkout(): Result<WorkoutSession?> {
        return _activeAdapter.value.stopWorkout()
    }

    override suspend fun triggerHapticAlert(type: Int) {
        _activeAdapter.value.triggerHapticAlert(type)
    }
}
