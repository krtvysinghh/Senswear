package com.senswear.app.core.ble

import com.senswear.app.core.domain.model.BatteryState
import com.senswear.app.core.domain.model.ConnectionState
import com.senswear.app.core.domain.model.FitnessSnapshot
import com.senswear.app.core.domain.model.WearableDevice
import com.senswear.app.core.domain.model.WorkoutSession
import com.senswear.app.core.domain.model.WorkoutType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * WearableConnector
 * Clean abstraction for hardware/wearable communication.
 * Allows Senswear to support multiple wearable hardware backends
 * (Pebble Qore 2, Fake Simulated Qore 2, future wearables) seamlessly.
 */
interface WearableConnector {
    val connectionState: StateFlow<ConnectionState>
    val liveMetrics: StateFlow<FitnessSnapshot>
    val currentDevice: StateFlow<WearableDevice?>
    val rawPacketLogs: StateFlow<List<String>>

    suspend fun connect(macAddress: String? = null)
    suspend fun disconnect()
    suspend fun syncHistory(): Result<Int>
    suspend fun getDeviceInfo(): WearableDevice?
    suspend fun getBattery(): BatteryState
    suspend fun startWorkout(type: WorkoutType): Result<WorkoutSession>
    suspend fun stopWorkout(): Result<WorkoutSession?>
    suspend fun triggerHapticAlert(type: Int = 1)
}
