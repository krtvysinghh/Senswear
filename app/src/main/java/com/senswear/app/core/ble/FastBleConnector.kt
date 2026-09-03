package com.senswear.app.core.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FastBleConnector(
    private val context: Context,
    private val targetMacAddress: String? = null
) {
    private val _connectionLatencyMs = MutableStateFlow<Long>(0)
    val connectionLatencyMs: StateFlow<Long> = _connectionLatencyMs.asStateFlow()

    private val _isHighPriorityActive = MutableStateFlow(false)
    val isHighPriorityActive: StateFlow<Boolean> = _isHighPriorityActive.asStateFlow()

    @SuppressLint("MissingPermission")
    fun requestInstantHighPriority(gatt: BluetoothGatt): Boolean {
        return try {
            val success = gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)
            _isHighPriorityActive.value = success
            success
        } catch (e: Exception) {
            false
        }
    }

    fun recordConnectionLatency(startTimeEpochMs: Long) {
        val latency = System.currentTimeMillis() - startTimeEpochMs
        _connectionLatencyMs.value = latency
    }
}
