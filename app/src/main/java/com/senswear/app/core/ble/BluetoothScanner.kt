package com.senswear.app.core.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import com.senswear.app.core.domain.model.WearableDevice
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class BluetoothScanner(private val context: Context) {

    private val bluetoothManager: BluetoothManager? by lazy {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    }

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        bluetoothManager?.adapter
    }

    val isBluetoothEnabled: Boolean
        get() = bluetoothAdapter?.isEnabled == true

    @SuppressLint("MissingPermission")
    fun scanForDevices(): Flow<List<WearableDevice>> = callbackFlow {
        val scanner = bluetoothAdapter?.bluetoothLeScanner
        if (scanner == null || !isBluetoothEnabled) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val discoveredMap = mutableMapOf<String, WearableDevice>()

        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val device = result.device ?: return
                val name = result.scanRecord?.deviceName ?: device.name ?: "Unknown BLE Device"
                val address = device.address ?: return
                val rssi = result.rssi

                val isQore2 = Qore2Protocol.PEBBLE_DEVICE_NAMES.any { name.contains(it, ignoreCase = true) }

                val wearable = WearableDevice(
                    id = address,
                    name = if (isQore2) "Pebble Qore 2" else name,
                    macAddress = address,
                    rssi = rssi,
                    isPaired = isQore2
                )

                discoveredMap[address] = wearable
                trySend(discoveredMap.values.toList().sortedByDescending { it.rssi })
            }

            override fun onScanFailed(errorCode: Int) {
                close(Exception("BLE Scan Failed with error code: $errorCode"))
            }
        }

        val filters = listOf(
            ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(Qore2Protocol.HEART_RATE_SERVICE_UUID))
                .build(),
            ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(Qore2Protocol.PEBBLE_VENDOR_SERVICE_UUID))
                .build()
        )

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        try {
            scanner.startScan(null, settings, scanCallback)
        } catch (e: SecurityException) {
            close(e)
        }

        awaitClose {
            try {
                scanner.stopScan(scanCallback)
            } catch (ignored: Exception) {}
        }
    }
}
