package com.senswear.app.core.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.os.Build

/**
 * Handles dynamic MTU and PHY negotiation to maximize throughput and minimize radio power consumption.
 */
class BleMtuNegotiator(
    private val preferredMtu: Int = 512,
    private val fallbackMtu: Int = 247
) {
    fun requestOptimalMtu(gatt: BluetoothGatt, onResult: (Boolean) -> Unit = {}) {
        val success = gatt.requestMtu(preferredMtu)
        if (!success) {
            gatt.requestMtu(fallbackMtu)
        }
        onResult(success)
    }

    fun requestHighSpeedPhy(gatt: BluetoothGatt) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            gatt.setPreferredPhy(
                BluetoothDevice.PHY_LE_2M_MASK,
                BluetoothDevice.PHY_LE_2M_MASK,
                BluetoothDevice.PHY_OPTION_NO_PREFERRED
            )
        }
    }
}
