package com.senswear.app.feature.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senswear.app.core.data.repository.ActivityRepository
import com.senswear.app.core.data.repository.GoalRepository
import com.senswear.app.core.domain.model.DailyActivity
import com.senswear.app.core.domain.model.Goal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ActivityUiState(
    val todayActivity: DailyActivity? = null,
    val weeklyHistory: List<DailyActivity> = emptyList(),
    val goals: List<Goal> = emptyList(),
    val selectedTimeRange: Int = 7 // 7 days or 30 days
)

class ActivityViewModel(
    private val activityRepository: ActivityRepository,
    private val goalRepository: GoalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivityUiState())
    val uiState: StateFlow<ActivityUiState> = _uiState.asStateFlow()

    init {
        loadActivityData()
    }

    private fun loadActivityData() {
        viewModelScope.launch {
            val today = activityRepository.getTodayActivity()
            val history = activityRepository.getRecentActivities(7)
            val goals = goalRepository.getGoals()
            _uiState.value = _uiState.value.copy(
                todayActivity = today,
                weeklyHistory = history,
                goals = goals
            )
        }
    }

    fun setTimeRange(days: Int) {
        viewModelScope.launch {
            val history = activityRepository.getRecentActivities(days)
            _uiState.value = _uiState.value.copy(
                selectedTimeRange = days,
                weeklyHistory = history
            )
        }
    }
}
