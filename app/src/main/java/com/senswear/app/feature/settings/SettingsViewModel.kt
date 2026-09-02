package com.senswear.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senswear.app.core.data.repository.GoalRepository
import com.senswear.app.core.domain.model.Goal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class UnitSystem(val label: String) {
    METRIC("Metric (km, °C, kg)"),
    IMPERIAL("Imperial (mi, °F, lbs)")
}

data class SettingsUiState(
    val stepGoal: Int = 10000,
    val calorieGoal: Int = 450,
    val sleepGoalHours: Float = 8.0f,
    val unitSystem: UnitSystem = UnitSystem.METRIC,
    val isHealthConnectConnected: Boolean = true,
    val isExportSuccess: Boolean = false,
    val isDataDeleted: Boolean = false
)

class SettingsViewModel(
    private val goalRepository: GoalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadGoals()
    }

    private fun loadGoals() {
        viewModelScope.launch {
            val goals = goalRepository.getGoals()
            val stepGoal = goals.find { it.id == "goal_steps" }?.targetValue?.toInt() ?: 10000
            val calGoal = goals.find { it.id == "goal_calories" }?.targetValue?.toInt() ?: 450
            val sleepGoal = goals.find { it.id == "goal_sleep" }?.targetValue?.toFloat() ?: 8.0f
            _uiState.value = _uiState.value.copy(
                stepGoal = stepGoal,
                calorieGoal = calGoal,
                sleepGoalHours = sleepGoal
            )
        }
    }

    fun updateStepGoal(steps: Int) {
        viewModelScope.launch {
            goalRepository.updateGoal("goal_steps", steps.toDouble())
            _uiState.value = _uiState.value.copy(stepGoal = steps)
        }
    }

    fun toggleUnitSystem() {
        val next = if (_uiState.value.unitSystem == UnitSystem.METRIC) UnitSystem.IMPERIAL else UnitSystem.METRIC
        _uiState.value = _uiState.value.copy(unitSystem = next)
    }

    fun exportDataJson(): String {
        _uiState.value = _uiState.value.copy(isExportSuccess = true)
        return """
        {
          "app": "Senswear",
          "version": "1.0.0",
          "exportTimestamp": ${System.currentTimeMillis()},
          "device": "Pebble Qore 2",
          "status": "Local Encrypted Export Valid"
        }
        """.trimIndent()
    }

    fun deleteAllData() {
        _uiState.value = _uiState.value.copy(isDataDeleted = true)
    }
}
