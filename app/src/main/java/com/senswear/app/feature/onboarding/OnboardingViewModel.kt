package com.senswear.app.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senswear.app.core.ble.WearableConnector
import com.senswear.app.core.domain.model.WearableDevice
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class OnboardingStep {
    WELCOME,
    PERMISSIONS,
    SCANNING,
    DEVICE_FOUND,
    CONNECTING,
    HEALTH_CONNECT_SETUP,
    COMPLETE
}

data class OnboardingUiState(
    val currentStep: OnboardingStep = OnboardingStep.WELCOME,
    val isScanning: Boolean = false,
    val discoveredDevices: List<WearableDevice> = emptyList(),
    val selectedDevice: WearableDevice? = null,
    val isBluetoothGranted: Boolean = true,
    val isHealthConnectGranted: Boolean = true
)

class OnboardingViewModel(
    private val wearableConnector: WearableConnector
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun nextStep() {
        when (_uiState.value.currentStep) {
            OnboardingStep.WELCOME -> _uiState.value = _uiState.value.copy(currentStep = OnboardingStep.PERMISSIONS)
            OnboardingStep.PERMISSIONS -> startScanning()
            OnboardingStep.SCANNING -> {}
            OnboardingStep.DEVICE_FOUND -> connectToDevice()
            OnboardingStep.CONNECTING -> _uiState.value = _uiState.value.copy(currentStep = OnboardingStep.HEALTH_CONNECT_SETUP)
            OnboardingStep.HEALTH_CONNECT_SETUP -> _uiState.value = _uiState.value.copy(currentStep = OnboardingStep.COMPLETE)
            OnboardingStep.COMPLETE -> {}
        }
    }

    private fun startScanning() {
        _uiState.value = _uiState.value.copy(
            currentStep = OnboardingStep.SCANNING,
            isScanning = true
        )
        viewModelScope.launch {
            delay(1800) // Simulated / real discovery duration
            val mockQore2 = WearableDevice(
                id = "E4:5F:01:A8:2B:99",
                name = "Pebble Qore 2",
                macAddress = "E4:5F:01:A8:2B:99",
                rssi = -54,
                isPaired = true
            )
            _uiState.value = _uiState.value.copy(
                isScanning = false,
                currentStep = OnboardingStep.DEVICE_FOUND,
                discoveredDevices = listOf(mockQore2),
                selectedDevice = mockQore2
            )
        }
    }

    fun connectToDevice() {
        _uiState.value = _uiState.value.copy(currentStep = OnboardingStep.CONNECTING)
        viewModelScope.launch {
            wearableConnector.connect(_uiState.value.selectedDevice?.macAddress)
            delay(1200)
            _uiState.value = _uiState.value.copy(currentStep = OnboardingStep.HEALTH_CONNECT_SETUP)
        }
    }

    fun completeOnboarding(onFinish: () -> Unit) {
        onFinish()
    }
}
