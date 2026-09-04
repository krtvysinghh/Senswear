package com.senswear.app.core.ble

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
import com.senswear.app.core.domain.model.BatteryState
import com.senswear.app.core.domain.model.ConnectionState
import com.senswear.app.core.domain.model.DataSource
import com.senswear.app.core.domain.model.FitnessSnapshot
import com.senswear.app.core.domain.model.PhysiologicalDerivationEngine
import com.senswear.app.core.domain.model.WearableBrand
import com.senswear.app.core.domain.model.WearableDevice
import com.senswear.app.core.domain.model.WorkoutSession
import com.senswear.app.core.domain.model.WorkoutType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class UniversalBleConnector(private val context: Context) : WearableConnector {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private var bluetoothGatt: BluetoothGatt? = null
    private var connectedDevice: BluetoothDevice? = null

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _liveMetrics = MutableStateFlow(FitnessSnapshot())
    override val liveMetrics: StateFlow<FitnessSnapshot> = _liveMetrics.asStateFlow()

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
                    logPacket("GATT Connected to ${gatt.device.name ?: gatt.device.address}. Requesting MTU 512 & Priority High")
                    gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)
                    gatt.requestMtu(512)
                    gatt.discoverServices()

                    val brand = WearableBrand.classifyDevice(gatt.device.name)
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
                    logPacket("GATT Disconnected (status: $status)")
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
            logPacket("Services discovered (${gatt.services.size} services found). Subscribing to telemetry...")

            // Subscribe to standard Heart Rate Service (0x180D -> 0x2A37)
            val hrService = gatt.getService(UniversalBleDecoder.HR_SERVICE_UUID)
            val hrChar = hrService?.getCharacteristic(UniversalBleDecoder.HR_MEASUREMENT_UUID)
            if (hrChar != null) {
                subscribeCharacteristic(gatt, hrChar)
                logPacket("Subscribed to Heart Rate Service (0x180D)")
            }

            // Subscribe to Running Speed & Cadence (0x1814 -> 0x2A53)
            val rscService = gatt.getService(UniversalBleDecoder.RSC_SERVICE_UUID)
            val rscChar = rscService?.getCharacteristic(UniversalBleDecoder.RSC_MEASUREMENT_UUID)
            if (rscChar != null) {
                subscribeCharacteristic(gatt, rscChar)
                logPacket("Subscribed to Running Speed & Cadence (0x1814)")
            }

            // Subscribe to Health Thermometer (0x1809 -> 0x2A1C)
            val thermoService = gatt.getService(UniversalBleDecoder.THERMO_SERVICE_UUID)
            val thermoChar = thermoService?.getCharacteristic(UniversalBleDecoder.THERMO_MEASUREMENT_UUID)
            if (thermoChar != null) {
                subscribeCharacteristic(gatt, thermoChar)
                logPacket("Subscribed to Health Thermometer (0x1809)")
            }

            // Read Battery Service (0x180F -> 0x2A19)
            val batteryService = gatt.getService(UniversalBleDecoder.BATTERY_SERVICE_UUID)
            val batteryChar = batteryService?.getCharacteristic(UniversalBleDecoder.BATTERY_LEVEL_UUID)
            if (batteryChar != null) {
                gatt.readCharacteristic(batteryChar)
                subscribeCharacteristic(gatt, batteryChar)
            }

            // Subscribe to Pebble Vendor Telemetry if present (0xFEE0 -> 0xFEE2)
            val pebbleService = gatt.getService(UUID.fromString("0000fee0-0000-1000-8000-00805f9b34fb"))
            val pebbleChar = pebbleService?.getCharacteristic(UUID.fromString("0000fee2-0000-1000-8000-00805f9b34fb"))
            if (pebbleChar != null) {
                subscribeCharacteristic(gatt, pebbleChar)
                logPacket("Subscribed to Pebble Vendor Telemetry (0xFEE0)")
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
                    val hrv = hrResult.hrvReading?.rmssdMs ?: _liveMetrics.value.hrvRmssdMs
                    val stress = PhysiologicalDerivationEngine.deriveStressFromHrv(hrv)

                    _liveMetrics.value = _liveMetrics.value.copy(
                        liveHeartRateBpm = bpm,
                        hrvRmssdMs = hrv,
                        stressScore = stress,
                        lastSyncEpochMs = System.currentTimeMillis()
                    )
                    logPacket("HR: $bpm BPM | HRV: ${hrv ?: "--"} ms | RR: ${hrResult.rrIntervalsMs.size} intervals")
                }
            }
            UniversalBleDecoder.RSC_MEASUREMENT_UUID -> {
                val rsc = UniversalBleDecoder.decodeRunningSpeedCadence(data)
                if (rsc != null) {
                    logPacket("RSC -> Speed: ${"%.2f".format(rsc.speedMetersPerSecond)} m/s, Cadence: ${rsc.cadenceSpm} SPM")
                }
            }
            UniversalBleDecoder.BATTERY_LEVEL_UUID -> {
                val level = UniversalBleDecoder.decodeBatteryLevel(data)
                if (level != null) {
                    _liveMetrics.value = _liveMetrics.value.copy(batteryPercent = level)
                    logPacket("Battery: $level%")
                }
            }
            UniversalBleDecoder.THERMO_MEASUREMENT_UUID -> {
                val temp = UniversalBleDecoder.decodeTemperature(data)
                if (temp != null) {
                    _liveMetrics.value = _liveMetrics.value.copy(skinTemperatureCelsius = temp.temperatureCelsius)
                    logPacket("Temp: ${temp.temperatureCelsius}°C")
                }
            }
            UUID.fromString("0000fee2-0000-1000-8000-00805f9b34fb") -> {
                val packet = Qore2Decoder.decodeVendorLiveTelemetry(data)
                if (packet != null) {
                    _liveMetrics.value = _liveMetrics.value.copy(
                        steps = packet.steps,
                        distanceMeters = packet.distanceMeters,
                        activeCaloriesKcal = packet.activeCalories,
                        liveHeartRateBpm = packet.heartRate,
                        spo2Percent = packet.spo2,
                        hrvRmssdMs = packet.hrv,
                        stressScore = packet.stress,
                        skinTemperatureCelsius = packet.skinTempCelsius,
                        batteryPercent = packet.batteryPercent,
                        lastSyncEpochMs = System.currentTimeMillis()
                    )
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun subscribeCharacteristic(gatt: BluetoothGatt, char: BluetoothGattCharacteristic) {
        gatt.setCharacteristicNotification(char, true)
        val descriptor = char.getDescriptor(CCCD_UUID)
        if (descriptor != null) {
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            gatt.writeDescriptor(descriptor)
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun connect(macAddress: String?) {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) return
        _connectionState.value = ConnectionState.CONNECTING

        val device = if (macAddress != null) {
            bluetoothAdapter.getRemoteDevice(macAddress)
        } else {
            connectedDevice
        }

        if (device != null) {
            connectedDevice = device
            bluetoothGatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
        } else {
            _connectionState.value = ConnectionState.DISCONNECTED
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun disconnect() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        _connectionState.value = ConnectionState.DISCONNECTED
        _currentDevice.value = null
    }

    override suspend fun syncHistory(): Result<Int> {
        return Result.success(0)
    }

    override suspend fun getDeviceInfo(): WearableDevice? = _currentDevice.value

    override suspend fun getBattery(): BatteryState {
        val pct = _liveMetrics.value.batteryPercent ?: 100
        return BatteryState(percentage = pct, isCharging = false, estimatedDaysRemaining = pct * 45 / 100)
    }

    override suspend fun startWorkout(type: WorkoutType): Result<WorkoutSession> {
        val session = WorkoutSession(
            id = "w_${System.currentTimeMillis()}",
            type = type,
            startTimeEpochMs = System.currentTimeMillis(),
            source = DataSource.PEBBLE_QORE_2_BLE
        )
        return Result.success(session)
    }

    override suspend fun stopWorkout(): Result<WorkoutSession?> = Result.success(null)

    override suspend fun triggerHapticAlert(type: Int) {
        // Universal vibration / alert command
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
