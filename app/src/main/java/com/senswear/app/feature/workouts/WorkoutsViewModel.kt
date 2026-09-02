package com.senswear.app.feature.workouts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senswear.app.core.ble.WearableConnector
import com.senswear.app.core.data.repository.WorkoutRepository
import com.senswear.app.core.domain.model.WorkoutSession
import com.senswear.app.core.domain.model.WorkoutType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WorkoutsUiState(
    val recentWorkouts: List<WorkoutSession> = emptyList(),
    val activeSession: WorkoutSession? = null,
    val selectedWorkoutType: WorkoutType = WorkoutType.OUTDOOR_WALK,
    val isRecording: Boolean = false
)

class WorkoutsViewModel(
    private val wearableConnector: WearableConnector,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutsUiState())
    val uiState: StateFlow<WorkoutsUiState> = _uiState.asStateFlow()

    val liveMetrics = wearableConnector.liveMetrics

    init {
        loadWorkouts()
        observeActiveWorkout()
    }

    private fun loadWorkouts() {
        viewModelScope.launch {
            val list = workoutRepository.getRecentWorkouts()
            _uiState.value = _uiState.value.copy(recentWorkouts = list)
        }
    }

    private fun observeActiveWorkout() {
        viewModelScope.launch {
            wearableConnector.liveMetrics.collect { snapshot ->
                _uiState.value = _uiState.value.copy(
                    activeSession = snapshot.activeWorkout,
                    isRecording = snapshot.activeWorkout != null
                )
            }
        }
    }

    fun selectWorkoutType(type: WorkoutType) {
        _uiState.value = _uiState.value.copy(selectedWorkoutType = type)
    }

    fun startWorkout() {
        viewModelScope.launch {
            wearableConnector.startWorkout(_uiState.value.selectedWorkoutType)
        }
    }

    fun stopWorkout() {
        viewModelScope.launch {
            val finished = wearableConnector.stopWorkout().getOrNull()
            if (finished != null) {
                workoutRepository.saveWorkout(finished)
                loadWorkouts()
            }
        }
    }
}
