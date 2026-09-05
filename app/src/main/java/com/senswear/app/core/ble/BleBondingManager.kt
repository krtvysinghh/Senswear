package com.senswear.app.core.ble

import android.bluetooth.BluetoothDevice
import java.lang.reflect.Method

/**
 * Manages Bluetooth bonding lifecycle, recovers from GATT_AUTH_FAIL (Error 137),
 * and handles unpairing/re-bonding cleanly.
 */
class BleBondingManager {
    enum class BondState {
        NONE,
        BONDING,
        BONDED,
        FAILED
    }

    fun getBondState(device: BluetoothDevice): BondState {
        return when (device.bondState) {
            BluetoothDevice.BOND_BONDED -> BondState.BONDED
            BluetoothDevice.BOND_BONDING -> BondState.BONDING
            else -> BondState.NONE
        }
    }

    fun createBond(device: BluetoothDevice): Boolean {
        return device.createBond()
    }

    fun removeBond(device: BluetoothDevice): Boolean {
        return try {
            val method: Method = device.javaClass.getMethod("removeBond")
            (method.invoke(device) as? Boolean) ?: false
        } catch (e: Exception) {
            false
        }
    }

    fun handleAuthFailure(device: BluetoothDevice): Boolean {
        // Recover from corrupted key state
        removeBond(device)
        return createBond(device)
    }
}
