package com.senswear.app.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senswear.app.core.data.repository.ActivityRepository
import com.senswear.app.core.data.repository.SleepRepository
import com.senswear.app.core.data.repository.WorkoutRepository
import com.senswear.app.core.domain.model.DailyActivity
import com.senswear.app.core.domain.model.SleepSession
import com.senswear.app.core.domain.model.WorkoutSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class HistoryRange(val label: String, val days: Int) {
    WEEK("7 Days", 7),
    MONTH("30 Days", 30),
    QUARTER("90 Days", 90)
}

data class HistoryUiState(
    val range: HistoryRange = HistoryRange.WEEK,
    val activities: List<DailyActivity> = emptyList(),
    val sleepSessions: List<SleepSession> = emptyList(),
    val workouts: List<WorkoutSession> = emptyList()
)

class HistoryViewModel(
    private val activityRepository: ActivityRepository,
    private val sleepRepository: SleepRepository,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory(HistoryRange.WEEK)
    }

    fun setRange(range: HistoryRange) {
        _uiState.value = _uiState.value.copy(range = range)
        loadHistory(range)
    }

    private fun loadHistory(range: HistoryRange) {
        viewModelScope.launch {
            val acts = activityRepository.getRecentActivities(range.days)
            val sleeps = sleepRepository.getRecentSleepSessions(range.days.coerceAtMost(14))
            val wks = workoutRepository.getRecentWorkouts()
            _uiState.value = _uiState.value.copy(
                activities = acts,
                sleepSessions = sleeps,
                workouts = wks
            )
        }
    }
}
