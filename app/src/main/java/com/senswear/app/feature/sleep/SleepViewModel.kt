package com.senswear.app.feature.sleep

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senswear.app.core.data.repository.SleepRepository
import com.senswear.app.core.domain.model.SleepSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SleepUiState(
    val latestSession: SleepSession? = null,
    val recentSessions: List<SleepSession> = emptyList(),
    val averageScore: Int = 85,
    val averageDurationMinutes: Int = 452
)

class SleepViewModel(
    private val sleepRepository: SleepRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SleepUiState())
    val uiState: StateFlow<SleepUiState> = _uiState.asStateFlow()

    init {
        loadSleepData()
    }

    private fun loadSleepData() {
        viewModelScope.launch {
            val latest = sleepRepository.getLatestSleepSession()
            val recents = sleepRepository.getRecentSleepSessions(7)
            val avgScore = if (recents.isNotEmpty()) recents.map { it.sleepScore }.average().toInt() else 85
            val avgDur = if (recents.isNotEmpty()) recents.map { it.durationMinutes }.average().toInt() else 452

            _uiState.value = _uiState.value.copy(
                latestSession = latest,
                recentSessions = recents,
                averageScore = avgScore,
                averageDurationMinutes = avgDur
            )
        }
    }
}
