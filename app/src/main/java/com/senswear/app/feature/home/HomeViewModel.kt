package com.senswear.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senswear.app.core.ble.WearableConnector
import com.senswear.app.core.data.repository.ActivityRepository
import com.senswear.app.core.data.repository.HealthRepository
import com.senswear.app.core.data.repository.InsightsRepository
import com.senswear.app.core.data.repository.SleepRepository
import com.senswear.app.core.domain.model.ConnectionState
import com.senswear.app.core.domain.model.DailyActivity
import com.senswear.app.core.domain.model.FitnessSnapshot
import com.senswear.app.core.domain.model.HealthInsight
import com.senswear.app.core.domain.model.SleepSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val dailyActivity: DailyActivity? = null,
    val liveSnapshot: FitnessSnapshot = FitnessSnapshot(),
    val latestSleep: SleepSession? = null,
    val insights: List<HealthInsight> = emptyList(),
    val isSyncing: Boolean = false,
    val errorMessage: String? = null
)

class HomeViewModel(
    private val wearableConnector: WearableConnector,
    private val activityRepository: ActivityRepository,
    private val healthRepository: HealthRepository,
    private val sleepRepository: SleepRepository,
    private val insightsRepository: InsightsRepository = InsightsRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val liveMetrics = wearableConnector.liveMetrics
    val connectionState = wearableConnector.connectionState

    init {
        loadHomeData()
        observeLiveMetrics()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            try {
                val activity = activityRepository.getTodayActivity()
                val sleep = sleepRepository.getLatestSleepSession()
                val insights = insightsRepository.generateInsights(
                    stepGoalPct = activity.stepProgressPercent,
                    sleepMinutes = sleep.durationMinutes,
                    restingHr = 61
                )
                _uiState.value = _uiState.value.copy(
                    dailyActivity = activity,
                    latestSleep = sleep,
                    insights = insights
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    private fun observeLiveMetrics() {
        viewModelScope.launch {
            wearableConnector.liveMetrics.collect { snapshot ->
                _uiState.value = _uiState.value.copy(liveSnapshot = snapshot)
            }
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true)
            wearableConnector.syncHistory()
            loadHomeData()
            _uiState.value = _uiState.value.copy(isSyncing = false)
        }
    }
}
