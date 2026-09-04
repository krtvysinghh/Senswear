package com.senswear.app.feature.device

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senswear.app.core.ble.ScannedWearable
import com.senswear.app.core.ble.UniversalWearableScanner
import com.senswear.app.core.ble.WearableConnector
import com.senswear.app.core.domain.model.BatteryState
import com.senswear.app.core.domain.model.ConnectionState
import com.senswear.app.core.domain.model.WearableBrand
import com.senswear.app.core.domain.model.WearableDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DeviceUiState(
    val device: WearableDevice? = null,
    val selectedBrand: WearableBrand = WearableBrand.PEBBLE_QORE_2,
    val batteryState: BatteryState = BatteryState(percentage = 100),
    val isScanning: Boolean = false,
    val scannedDevices: List<ScannedWearable> = emptyList(),
    val isDiagnosticsOpen: Boolean = false,
    val lastSyncMessage: String = "Awaiting connection",
    val isSyncing: Boolean = false
)

class DeviceViewModel(
    context: Context,
    private val wearableConnector: WearableConnector
) : ViewModel() {

    private val scanner = UniversalWearableScanner(context)

    private val _uiState = MutableStateFlow(DeviceUiState())
    val uiState: StateFlow<DeviceUiState> = _uiState.asStateFlow()

    val connectionState = wearableConnector.connectionState
    val currentDevice = wearableConnector.currentDevice
    val rawPacketLogs = wearableConnector.rawPacketLogs

    init {
        observeDevice()
        observeScanner()
    }

    private fun observeDevice() {
        viewModelScope.launch {
            wearableConnector.currentDevice.collect { dev ->
                _uiState.value = _uiState.value.copy(
                    device = dev,
                    batteryState = dev?.batteryState ?: BatteryState(percentage = 100)
                )
            }
        }
    }

    private fun observeScanner() {
        viewModelScope.launch {
            scanner.discoveredDevices.collect { list ->
                _uiState.value = _uiState.value.copy(scannedDevices = list)
            }
        }
        viewModelScope.launch {
            scanner.isScanning.collect { isScan ->
                _uiState.value = _uiState.value.copy(isScanning = isScan)
            }
        }
    }

    fun selectBrand(brand: WearableBrand) {
        _uiState.value = _uiState.value.copy(selectedBrand = brand)
    }

    fun startScanning() {
        scanner.startScan()
    }

    fun stopScanning() {
        scanner.stopScan()
    }

    fun connectDevice(macAddress: String? = null) {
        viewModelScope.launch {
            scanner.stopScan()
            wearableConnector.connect(macAddress)
        }
    }

    fun disconnectDevice() {
        viewModelScope.launch {
            wearableConnector.disconnect()
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true)
            wearableConnector.syncHistory()
            _uiState.value = _uiState.value.copy(
                isSyncing = false,
                lastSyncMessage = "Just now"
            )
        }
    }

    fun testHapticFeedback(pattern: Int = 1) {
        viewModelScope.launch {
            wearableConnector.triggerHapticAlert(pattern)
        }
    }
}
