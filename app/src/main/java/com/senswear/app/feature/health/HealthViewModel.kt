package com.senswear.app.feature.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senswear.app.core.ble.WearableConnector
import com.senswear.app.core.data.repository.HealthRepository
import com.senswear.app.core.domain.model.HeartRateReading
import com.senswear.app.core.domain.model.HrvReading
import com.senswear.app.core.domain.model.Spo2Reading
import com.senswear.app.core.domain.model.StressReading
import com.senswear.app.core.domain.model.TemperatureReading
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class HealthSubTab(val title: String) {
    HEART_RATE("Heart"),
    SPO2("SpO₂"),
    HRV("HRV"),
    STRESS("Stress"),
    TEMPERATURE("Temp")
}

data class HealthUiState(
    val selectedTab: HealthSubTab = HealthSubTab.HEART_RATE,
    val heartRateHistory: List<HeartRateReading> = emptyList(),
    val spo2Readings: List<Spo2Reading> = emptyList(),
    val hrvReadings: List<HrvReading> = emptyList(),
    val stressReadings: List<StressReading> = emptyList(),
    val temperatureReadings: List<TemperatureReading> = emptyList(),
    val restingHr: Int = 61,
    val avgHr: Int = 74,
    val maxHr: Int = 142,
    val minHr: Int = 52
)

class HealthViewModel(
    private val wearableConnector: WearableConnector,
    private val healthRepository: HealthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HealthUiState())
    val uiState: StateFlow<HealthUiState> = _uiState.asStateFlow()

    val liveMetrics = wearableConnector.liveMetrics

    init {
        loadHealthData()
    }

    private fun loadHealthData() {
        viewModelScope.launch {
            val hrHistory = healthRepository.getHeartRateHistory(24)
            val spo2 = healthRepository.getRecentSpo2Readings()
            val hrv = healthRepository.getRecentHrvReadings()
            val stress = healthRepository.getRecentStressReadings()
            val temp = healthRepository.getRecentTemperatureReadings()

            val bpmValues = hrHistory.map { it.bpm }
            val avg = if (bpmValues.isNotEmpty()) bpmValues.average().toInt() else 74
            val min = bpmValues.minOrNull() ?: 52
            val max = bpmValues.maxOrNull() ?: 142

            _uiState.value = _uiState.value.copy(
                heartRateHistory = hrHistory,
                spo2Readings = spo2,
                hrvReadings = hrv,
                stressReadings = stress,
                temperatureReadings = temp,
                avgHr = avg,
                minHr = min,
                maxHr = max
            )
        }
    }

    fun selectTab(tab: HealthSubTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }
}
