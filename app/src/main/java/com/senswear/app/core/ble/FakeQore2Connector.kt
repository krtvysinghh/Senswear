package com.senswear.app.core.ble

import com.senswear.app.core.domain.model.BatteryState
import com.senswear.app.core.domain.model.ConnectionState
import com.senswear.app.core.domain.model.DataSource
import com.senswear.app.core.domain.model.FitnessSnapshot
import com.senswear.app.core.domain.model.WearableDevice
import com.senswear.app.core.domain.model.WorkoutSample
import com.senswear.app.core.domain.model.WorkoutSession
import com.senswear.app.core.domain.model.WorkoutType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

class FakeQore2Connector(
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) : WearableConnector {

    private val _connectionState = MutableStateFlow(ConnectionState.CONNECTED)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _currentDevice = MutableStateFlow<WearableDevice?>(
        WearableDevice(
            id = "E4:5F:01:A8:2B:99",
            name = "Pebble Qore 2",
            macAddress = "E4:5F:01:A8:2B:99",
            firmwareVersion = "v2.4.1-rc3",
            modelNumber = "PB-Q2-STEEL",
            hardwareRevision = "Rev. C",
            rssi = -58,
            connectionState = ConnectionState.CONNECTED,
            batteryState = BatteryState(percentage = 84),
            lastSyncEpochMs = System.currentTimeMillis() - 120_000L,
            isPaired = true
        )
    )
    override val currentDevice: StateFlow<WearableDevice?> = _currentDevice.asStateFlow()

    private val _liveMetrics = MutableStateFlow(
        FitnessSnapshot(
            steps = 8421,
            distanceMeters = 6400.0,
            activeCaloriesKcal = 342,
            totalCaloriesKcal = 1942,
            liveHeartRateBpm = 76,
            restingHeartRateBpm = 62,
            spo2Percent = 98,
            hrvRmssdMs = 54,
            stressScore = 22,
            skinTemperatureCelsius = 36.6,
            batteryPercent = 84,
            connectionState = ConnectionState.CONNECTED,
            lastSyncEpochMs = System.currentTimeMillis() - 120_000L
        )
    )
    override val liveMetrics: StateFlow<FitnessSnapshot> = _liveMetrics.asStateFlow()

    private val _rawPacketLogs = MutableStateFlow<List<String>>(
        listOf(
            "[INIT] FakeQore2Connector initialized with simulated hardware telemetry",
            "[BLE] Discovered GATT Service 0x180D (Heart Rate)",
            "[BLE] Discovered GATT Service 0x180F (Battery)",
            "[BLE] Discovered GATT Service 0xFEE0 (Pebble Vendor)",
            "[BLE] Subscribed to notifications on 0x2A37 and 0xFEE2"
        )
    )
    override val rawPacketLogs: StateFlow<List<String>> = _rawPacketLogs.asStateFlow()

    private var simulationJob: Job? = null
    private var activeWorkout: WorkoutSession? = null

    init {
        startSimulation()
    }

    private fun log(message: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.US).format(java.util.Date())
        val logLine = "[$timestamp] $message"
        val current = _rawPacketLogs.value.toMutableList()
        if (current.size > 100) current.removeAt(0)
        current.add(logLine)
        _rawPacketLogs.value = current
    }

    private fun startSimulation() {
        simulationJob?.cancel()
        simulationJob = coroutineScope.launch {
            var stepAccumulator = _liveMetrics.value.steps
            var currentHr = 74

            while (isActive) {
                delay(2500)
                if (_connectionState.value == ConnectionState.CONNECTED) {
                    val isWorkoutActive = activeWorkout != null

                    // Heart rate simulation: 120-155 during workout, 68-82 during rest
                    val targetHr = if (isWorkoutActive) {
                        Random.nextInt(128, 148)
                    } else {
                        Random.nextInt(68, 82)
                    }
                    currentHr = (currentHr * 0.7 + targetHr * 0.3).toInt()

                    // Increment steps
                    val stepDelta = if (isWorkoutActive) Random.nextInt(8, 20) else Random.nextInt(0, 5)
                    stepAccumulator += stepDelta
                    val distMeters = stepAccumulator * 0.76
                    val activeCals = (stepAccumulator * 0.042).toInt()
                    val totalCals = 1600 + activeCals

                    val hrv = if (isWorkoutActive) Random.nextInt(28, 42) else Random.nextInt(50, 62)
                    val stress = if (isWorkoutActive) Random.nextInt(45, 65) else Random.nextInt(18, 30)
                    val skinTemp = 36.5 + (Random.nextInt(-2, 3) / 10.0)

                    _liveMetrics.value = _liveMetrics.value.copy(
                        timestampEpochMs = System.currentTimeMillis(),
                        steps = stepAccumulator,
                        distanceMeters = distMeters,
                        activeCaloriesKcal = activeCals,
                        totalCaloriesKcal = totalCals,
                        liveHeartRateBpm = currentHr,
                        spo2Percent = if (Random.nextInt(10) > 2) 98 else 99,
                        hrvRmssdMs = hrv,
                        stressScore = stress,
                        skinTemperatureCelsius = skinTemp,
                        batteryPercent = 84,
                        connectionState = ConnectionState.CONNECTED
                    )

                    // Update live workout if running
                    if (isWorkoutActive && activeWorkout != null) {
                        val session = activeWorkout!!
                        val elapsedSec = ((System.currentTimeMillis() - session.startTimeEpochMs) / 1000).coerceAtLeast(1)
                        val sample = WorkoutSample(
                            timestampEpochMs = System.currentTimeMillis(),
                            heartRateBpm = currentHr,
                            speedKmh = 5.2,
                            currentDistanceMeters = (elapsedSec * 1.44),
                            caloriesAccumulated = (elapsedSec * 0.15).toInt()
                        )
                        val updatedWorkout = session.copy(
                            durationSeconds = elapsedSec,
                            totalDistanceMeters = sample.currentDistanceMeters,
                            totalCaloriesKcal = sample.caloriesAccumulated,
                            avgHeartRateBpm = ((session.avgHeartRateBpm * 4 + currentHr) / 5).coerceAtLeast(60),
                            maxHeartRateBpm = maxOf(session.maxHeartRateBpm, currentHr),
                            samples = (session.samples + sample).takeLast(100)
                        )
                        activeWorkout = updatedWorkout
                        _liveMetrics.value = _liveMetrics.value.copy(activeWorkout = updatedWorkout)
                    }

                    log("RX [0x2A37] HR: $currentHr BPM | Steps: $stepAccumulator | SpO2: 98% | HRV: ${hrv}ms")
                }
            }
        }
    }

    override suspend fun connect(macAddress: String?) {
        _connectionState.value = ConnectionState.CONNECTING
        log("Connecting to Pebble Qore 2 ($macAddress)...")
        delay(800)
        _connectionState.value = ConnectionState.CONNECTED
        _currentDevice.value = _currentDevice.value?.copy(connectionState = ConnectionState.CONNECTED)
        log("Connected to Pebble Qore 2 (Signal -54 dBm)")
    }

    override suspend fun disconnect() {
        _connectionState.value = ConnectionState.DISCONNECTED
        _currentDevice.value = _currentDevice.value?.copy(connectionState = ConnectionState.DISCONNECTED)
        _liveMetrics.value = _liveMetrics.value.copy(
            connectionState = ConnectionState.DISCONNECTED,
            liveHeartRateBpm = null
        )
        log("Disconnected from Pebble Qore 2")
    }

    override suspend fun syncHistory(): Result<Int> {
        _connectionState.value = ConnectionState.SYNCING
        log("Syncing historical packet telemetry from Qore 2 flash memory...")
        delay(1500)
        _connectionState.value = ConnectionState.CONNECTED
        _liveMetrics.value = _liveMetrics.value.copy(lastSyncEpochMs = System.currentTimeMillis())
        _currentDevice.value = _currentDevice.value?.copy(lastSyncEpochMs = System.currentTimeMillis())
        log("Sync completed: 1,420 step records, 2,840 HR data points, 1 sleep session processed.")
        return Result.success(4260)
    }

    override suspend fun getDeviceInfo(): WearableDevice? = _currentDevice.value

    override suspend fun getBattery(): BatteryState = BatteryState(percentage = 84, isCharging = false)

    override suspend fun startWorkout(type: WorkoutType): Result<WorkoutSession> {
        val session = WorkoutSession(
            id = "workout_${System.currentTimeMillis()}",
            type = type,
            startTimeEpochMs = System.currentTimeMillis(),
            isLive = true,
            source = DataSource.PEBBLE_QORE_2_BLE
        )
        activeWorkout = session
        _liveMetrics.value = _liveMetrics.value.copy(activeWorkout = session)
        log("Started live workout: ${type.displayName}")
        return Result.success(session)
    }

    override suspend fun stopWorkout(): Result<WorkoutSession?> {
        val current = activeWorkout ?: return Result.success(null)
        val ended = current.copy(
            endTimeEpochMs = System.currentTimeMillis(),
            isLive = false,
            durationSeconds = ((System.currentTimeMillis() - current.startTimeEpochMs) / 1000).coerceAtLeast(1)
        )
        activeWorkout = null
        _liveMetrics.value = _liveMetrics.value.copy(activeWorkout = null)
        log("Stopped workout ${ended.type.displayName}. Duration: ${ended.durationSeconds}s, Calories: ${ended.totalCaloriesKcal} kcal")
        return Result.success(ended)
    }

    override suspend fun triggerHapticAlert(type: Int) {
        log("TX Trigger Haptic Feedback (Pattern #$type)")
    }
}
