package com.senswear.app.feature.device

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senswear.app.core.ble.WearableConnector
import com.senswear.app.core.domain.model.BatteryState
import com.senswear.app.core.domain.model.ConnectionState
import com.senswear.app.core.domain.model.WearableDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DeviceUiState(
    val device: WearableDevice? = null,
    val batteryState: BatteryState = BatteryState(percentage = 84),
    val isDiagnosticsOpen: Boolean = false,
    val lastSyncMessage: String = "12 seconds ago",
    val isSyncing: Boolean = false
)

class DeviceViewModel(
    private val wearableConnector: WearableConnector
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeviceUiState())
    val uiState: StateFlow<DeviceUiState> = _uiState.asStateFlow()

    val connectionState = wearableConnector.connectionState
    val currentDevice = wearableConnector.currentDevice
    val rawPacketLogs = wearableConnector.rawPacketLogs

    init {
        observeDevice()
    }

    private fun observeDevice() {
        viewModelScope.launch {
            wearableConnector.currentDevice.collect { dev ->
                _uiState.value = _uiState.value.copy(
                    device = dev,
                    batteryState = dev?.batteryState ?: BatteryState(percentage = 84)
                )
            }
        }
    }

    fun connectDevice() {
        viewModelScope.launch {
            wearableConnector.connect()
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
