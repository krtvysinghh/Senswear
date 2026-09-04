package com.senswear.app.core.wearable.adapters

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import com.senswear.app.core.ble.UniversalBleDecoder
import com.senswear.app.core.domain.model.BatteryState
import com.senswear.app.core.domain.model.ConnectionState
import com.senswear.app.core.domain.model.DataSource
import com.senswear.app.core.domain.model.FitnessSnapshot
import com.senswear.app.core.domain.model.PhysiologicalDerivationEngine
import com.senswear.app.core.domain.model.WearableBrand
import com.senswear.app.core.domain.model.WearableDevice
import com.senswear.app.core.domain.model.WorkoutSession
import com.senswear.app.core.domain.model.WorkoutType
import com.senswear.app.core.wearable.CapabilityRegistry
import com.senswear.app.core.wearable.CapabilityState
import com.senswear.app.core.wearable.SyncReport
import com.senswear.app.core.wearable.WearableAdapter
import com.senswear.app.core.wearable.WearableCapability
import com.senswear.app.core.wearable.WearableIntegrationType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class StandardBleHeartRateAdapter(
    private val context: Context,
    override val brand: WearableBrand = WearableBrand.POLAR
) : WearableAdapter {

    override val integrationType: WearableIntegrationType = WearableIntegrationType.STANDARD_GATT_BLE
    override val capabilities: Map<WearableCapability, CapabilityState> = CapabilityRegistry.getCapabilities(brand)

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private var bluetoothGatt: BluetoothGatt? = null
    private var connectedDevice: BluetoothDevice? = null

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _liveMetrics = MutableStateFlow<FitnessSnapshot?>(null)
    override val liveMetrics: StateFlow<FitnessSnapshot?> = _liveMetrics.asStateFlow()

    private val _currentDevice = MutableStateFlow<WearableDevice?>(null)
    override val currentDevice: StateFlow<WearableDevice?> = _currentDevice.asStateFlow()

    private val _rawPacketLogs = MutableStateFlow<List<String>>(emptyList())
    override val rawPacketLogs: StateFlow<List<String>> = _rawPacketLogs.asStateFlow()

    private val CCCD_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    _connectionState.value = ConnectionState.CONNECTED
                    logPacket("Standard GATT Connected to ${gatt.device.name ?: gatt.device.address}")
                    gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)
                    gatt.discoverServices()

                    _currentDevice.value = WearableDevice(
                        id = gatt.device.address,
                        name = gatt.device.name ?: brand.displayName,
                        macAddress = gatt.device.address,
                        connectionState = ConnectionState.CONNECTED
                    )
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    _connectionState.value = ConnectionState.DISCONNECTED
                    _currentDevice.value = null
                    _liveMetrics.value = null
                    logPacket("Standard GATT Disconnected")
                    gatt.close()
                    bluetoothGatt = null
                }
                BluetoothProfile.STATE_CONNECTING -> {
                    _connectionState.value = ConnectionState.CONNECTING
                }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) return

            // Subscribe to Heart Rate 0x180D -> 0x2A37
            val hrService = gatt.getService(UniversalBleDecoder.HR_SERVICE_UUID)
            val hrChar = hrService?.getCharacteristic(UniversalBleDecoder.HR_MEASUREMENT_UUID)
            if (hrChar != null) {
                gatt.setCharacteristicNotification(hrChar, true)
                val descriptor = hrChar.getDescriptor(CCCD_UUID)
                if (descriptor != null) {
                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    gatt.writeDescriptor(descriptor)
                    logPacket("Subscribed to Standard BLE Heart Rate (0x180D)")
                }
            }

            // Read Battery 0x180F -> 0x2A19
            val batService = gatt.getService(UniversalBleDecoder.BATTERY_SERVICE_UUID)
            val batChar = batService?.getCharacteristic(UniversalBleDecoder.BATTERY_LEVEL_UUID)
            if (batChar != null) {
                gatt.readCharacteristic(batChar)
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray) {
            handleCharacteristicData(characteristic.uuid, value)
        }

        @Deprecated("Deprecated for SDK 33+")
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            @Suppress("DEPRECATION")
            characteristic.value?.let { handleCharacteristicData(characteristic.uuid, it) }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                handleCharacteristicData(characteristic.uuid, value)
            }
        }
    }

    private fun handleCharacteristicData(uuid: UUID, data: ByteArray) {
        when (uuid) {
            UniversalBleDecoder.HR_MEASUREMENT_UUID -> {
                val hrResult = UniversalBleDecoder.decodeHeartRate(data)
                if (hrResult != null) {
                    val bpm = hrResult.reading.bpm
                    val hrv = hrResult.hrvReading?.rmssdMs
                    val stress = PhysiologicalDerivationEngine.deriveStressFromHrv(hrv)

                    val current = _liveMetrics.value ?: FitnessSnapshot()
                    _liveMetrics.value = current.copy(
                        liveHeartRateBpm = bpm,
                        hrvRmssdMs = hrv ?: current.hrvRmssdMs,
                        stressScore = stress ?: current.stressScore,
                        lastSyncEpochMs = System.currentTimeMillis()
                    )
                    logPacket("Standard BLE HR: $bpm BPM | HRV: ${hrv ?: "--"} ms")
                }
            }
            UniversalBleDecoder.BATTERY_LEVEL_UUID -> {
                val level = UniversalBleDecoder.decodeBatteryLevel(data)
                if (level != null) {
                    val current = _liveMetrics.value ?: FitnessSnapshot()
                    _liveMetrics.value = current.copy(batteryPercent = level)
                    logPacket("Battery Level: $level%")
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun connect(macAddress: String?): Result<Unit> {
        val adapter = bluetoothAdapter ?: return Result.failure(IllegalStateException("Bluetooth unavailable"))
        if (!adapter.isEnabled) return Result.failure(IllegalStateException("Bluetooth disabled"))

        _connectionState.value = ConnectionState.CONNECTING
        val device = if (macAddress != null) adapter.getRemoteDevice(macAddress) else connectedDevice
        if (device != null) {
            connectedDevice = device
            bluetoothGatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
            return Result.success(Unit)
        }
        _connectionState.value = ConnectionState.DISCONNECTED
        return Result.failure(IllegalArgumentException("No target device MAC provided"))
    }

    @SuppressLint("MissingPermission")
    override suspend fun disconnect(): Result<Unit> {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        _connectionState.value = ConnectionState.DISCONNECTED
        _currentDevice.value = null
        _liveMetrics.value = null
        return Result.success(Unit)
    }

    override suspend fun syncHistory(): Result<SyncReport> {
        return Result.failure(UnsupportedOperationException("Standard Bluetooth SIG HR Profile does not support proprietary historical batch dump"))
    }

    override suspend fun getBattery(): BatteryState? {
        val pct = _liveMetrics.value?.batteryPercent ?: return null
        return BatteryState(percentage = pct, isCharging = false, estimatedDaysRemaining = (pct * 45 / 100).coerceAtLeast(1))
    }

    override suspend fun startWorkout(type: WorkoutType): Result<WorkoutSession> {
        val session = WorkoutSession(
            id = "ble_w_${System.currentTimeMillis()}",
            type = type,
            startTimeEpochMs = System.currentTimeMillis(),
            source = DataSource.PEBBLE_QORE_2_BLE
        )
        return Result.success(session)
    }

    override suspend fun stopWorkout(): Result<WorkoutSession?> = Result.success(null)

    override suspend fun triggerHapticAlert(type: Int): Result<Unit> {
        return Result.failure(UnsupportedOperationException("Standard BLE HR profile does not support remote haptic triggering"))
    }

    private fun logPacket(message: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.US).format(java.util.Date())
        val log = "[$timestamp] $message"
        val current = _rawPacketLogs.value.toMutableList()
        if (current.size > 200) current.removeAt(0)
        current.add(log)
        _rawPacketLogs.value = current
    }
}
