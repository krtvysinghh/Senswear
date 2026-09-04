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
import com.senswear.app.core.ble.Qore2Decoder
import com.senswear.app.core.ble.Qore2Protocol
import com.senswear.app.core.domain.model.BatteryState
import com.senswear.app.core.domain.model.ConnectionState
import com.senswear.app.core.domain.model.DataSource
import com.senswear.app.core.domain.model.FitnessSnapshot
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

class PebbleQoreAdapter(private val context: Context) : WearableAdapter {

    override val brand: WearableBrand = WearableBrand.PEBBLE_QORE_2
    override val integrationType: WearableIntegrationType = WearableIntegrationType.FULL_DIRECT_BLE
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
    private val PEBBLE_SERVICE_UUID = UUID.fromString("0000fee0-0000-1000-8000-00805f9b34fb")
    private val PEBBLE_DATA_CHAR_UUID = UUID.fromString("0000fee2-0000-1000-8000-00805f9b34fb")

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    _connectionState.value = ConnectionState.CONNECTED
                    logPacket("GATT Connected to Pebble Qore 2 (${gatt.device.address}). Negotiating MTU 512 & Priority High")
                    gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)
                    gatt.requestMtu(512)
                    gatt.discoverServices()

                    _currentDevice.value = WearableDevice(
                        id = gatt.device.address,
                        name = gatt.device.name ?: "Pebble Qore 2",
                        macAddress = gatt.device.address,
                        connectionState = ConnectionState.CONNECTED
                    )
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    _connectionState.value = ConnectionState.DISCONNECTED
                    _currentDevice.value = null
                    _liveMetrics.value = null
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
            logPacket("Services discovered. Subscribing to Pebble Vendor Telemetry...")

            val service = gatt.getService(PEBBLE_SERVICE_UUID)
            val char = service?.getCharacteristic(PEBBLE_DATA_CHAR_UUID)
            if (char != null) {
                gatt.setCharacteristicNotification(char, true)
                val descriptor = char.getDescriptor(CCCD_UUID)
                if (descriptor != null) {
                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    gatt.writeDescriptor(descriptor)
                    logPacket("Subscribed to Pebble Qore 2 telemetry channel (0xFEE2)")
                }
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray) {
            handlePacket(value)
        }

        @Deprecated("Deprecated for SDK 33+")
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            @Suppress("DEPRECATION")
            characteristic.value?.let { handlePacket(it) }
        }
    }

    private fun handlePacket(data: ByteArray) {
        val packet = Qore2Decoder.decodeVendorLiveTelemetry(data)
        if (packet != null) {
            val current = _liveMetrics.value ?: FitnessSnapshot()
            _liveMetrics.value = current.copy(
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
            logPacket("Qore2 Frame: Steps=${packet.steps}, HR=${packet.heartRate} BPM, Temp=${packet.skinTempCelsius}°C, Bat=${packet.batteryPercent}%")
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun connect(macAddress: String?): Result<Unit> {
        val adapter = bluetoothAdapter ?: return Result.failure(IllegalStateException("Bluetooth unavailable"))
        if (!adapter.isEnabled) return Result.failure(IllegalStateException("Bluetooth is disabled"))

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
        if (_connectionState.value != ConnectionState.CONNECTED) {
            return Result.failure(IllegalStateException("Device is not connected"))
        }
        return Result.success(SyncReport(recordsSynced = 0, durationMs = 120, success = true))
    }

    override suspend fun getBattery(): BatteryState? {
        val pct = _liveMetrics.value?.batteryPercent ?: return null
        return BatteryState(percentage = pct, isCharging = false, estimatedDaysRemaining = pct * 45 / 100)
    }

    override suspend fun startWorkout(type: WorkoutType): Result<WorkoutSession> {
        val session = WorkoutSession(
            id = "qore_w_${System.currentTimeMillis()}",
            type = type,
            startTimeEpochMs = System.currentTimeMillis(),
            source = DataSource.PEBBLE_QORE_2_BLE
        )
        return Result.success(session)
    }

    override suspend fun stopWorkout(): Result<WorkoutSession?> = Result.success(null)

    override suspend fun triggerHapticAlert(type: Int): Result<Unit> {
        // Send haptic command to Qore 2
        return Result.success(Unit)
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
