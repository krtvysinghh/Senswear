package com.senswear.app.core.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import com.senswear.app.core.domain.model.WearableBrand
import com.senswear.app.core.domain.model.WearableDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ScannedWearable(
    val device: WearableDevice,
    val brand: WearableBrand,
    val rssi: Int,
    val lastSeenEpochMs: Long = System.currentTimeMillis()
)

class UniversalWearableScanner(context: Context) {

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter
    private val scanner = bluetoothAdapter?.bluetoothLeScanner

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<ScannedWearable>>(emptyList())
    val discoveredDevices: StateFlow<List<ScannedWearable>> = _discoveredDevices.asStateFlow()

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device ?: return
            val name = device.name ?: result.scanRecord?.deviceName
            val mac = device.address

            val brand = WearableBrand.classifyDevice(name)
            val wearable = WearableDevice(
                id = mac,
                name = name ?: "${brand.displayName} ($mac)",
                macAddress = mac,
                rssi = result.rssi
            )

            val current = _discoveredDevices.value.toMutableList()
            val existingIndex = current.indexOfFirst { it.device.macAddress == mac }
            val item = ScannedWearable(device = wearable, brand = brand, rssi = result.rssi)

            if (existingIndex >= 0) {
                current[existingIndex] = item
            } else {
                current.add(item)
            }
            _discoveredDevices.value = current.sortedByDescending { it.rssi }
        }

        override fun onScanFailed(errorCode: Int) {
            _isScanning.value = false
        }
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        val adapter = bluetoothAdapter
        if (scanner == null || adapter == null || !adapter.isEnabled) return
        _discoveredDevices.value = emptyList()
        _isScanning.value = true

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        scanner.startScan(null, settings, scanCallback)
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        if (scanner != null && _isScanning.value) {
            scanner.stopScan(scanCallback)
            _isScanning.value = false
        }
    }
}
