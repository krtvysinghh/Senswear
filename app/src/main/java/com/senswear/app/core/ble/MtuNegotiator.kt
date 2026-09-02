package com.senswear.app.core.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt

class MtuNegotiator(private val targetMtu: Int = 512) {
    var currentMtu: Int = 23
        private set

    @SuppressLint("MissingPermission")
    fun requestHighThroughputMtu(gatt: BluetoothGatt): Boolean {
        return try {
            gatt.requestMtu(targetMtu)
        } catch (e: Exception) {
            false
        }
    }

    fun onMtuUpdated(mtu: Int, status: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            currentMtu = mtu
        }
    }
}
