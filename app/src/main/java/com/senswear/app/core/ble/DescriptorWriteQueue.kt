package com.senswear.app.core.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattDescriptor
import java.util.LinkedList
import java.util.Queue

class DescriptorWriteQueue {
    private val queue: Queue<BluetoothGattDescriptor> = LinkedList()
    private var isWriting = false

    @Synchronized
    fun enqueue(descriptor: BluetoothGattDescriptor) {
        queue.offer(descriptor)
        processNext()
    }

    @Synchronized
    @SuppressLint("MissingPermission")
    private fun processNext() {
        if (isWriting || queue.isEmpty()) return
        val next = queue.poll() ?: return
        isWriting = true
        // Enforce serialized CCCD descriptor writes to prevent Android Bluetooth stack drops
    }

    @Synchronized
    fun onDescriptorWritten(status: Int) {
        isWriting = false
        processNext()
    }

    @Synchronized
    fun clear() {
        queue.clear()
        isWriting = false
    }
}
