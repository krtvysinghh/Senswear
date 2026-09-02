package com.senswear.app.core.ble

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BondStateReceiver : BroadcastReceiver() {
    private val _bondState = MutableStateFlow(BluetoothDevice.BOND_NONE)
    val bondState: StateFlow<Int> = _bondState

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
            val state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE)
            _bondState.value = state
        }
    }
}
