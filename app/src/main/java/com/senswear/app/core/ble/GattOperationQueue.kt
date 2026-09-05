package com.senswear.app.core.ble

import android.bluetooth.BluetoothGatt
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Serializes all BluetoothGatt operations through a strict FIFO queue.
 * Prevents race conditions and dropped packets caused by Android's single-threaded GATT architecture.
 */
class GattOperationQueue(
    private val defaultTimeoutMs: Long = 3000L,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) {
    sealed class GattOperation<T> {
        abstract val deferred: CompletableDeferred<T>
        abstract val action: suspend (BluetoothGatt) -> T

        data class Read<T>(
            override val deferred: CompletableDeferred<T> = CompletableDeferred(),
            override val action: suspend (BluetoothGatt) -> T
        ) : GattOperation<T>()

        data class Write<T>(
            override val deferred: CompletableDeferred<T> = CompletableDeferred(),
            override val action: suspend (BluetoothGatt) -> T
        ) : GattOperation<T>()

        data class Config<T>(
            override val deferred: CompletableDeferred<T> = CompletableDeferred(),
            override val action: suspend (BluetoothGatt) -> T
        ) : GattOperation<T>()
    }

    private val queue = Channel<GattOperation<*>>(Channel.UNLIMITED)
    private val isRunning = AtomicBoolean(false)

    fun start(gattProvider: () -> BluetoothGatt?) {
        if (!isRunning.compareAndSet(false, true)) return

        scope.launch {
            for (op in queue) {
                val gatt = gattProvider()
                if (gatt == null) {
                    op.deferred.completeExceptionally(IllegalStateException("BluetoothGatt instance is null or disconnected"))
                    continue
                }

                try {
                    val result = withTimeoutOrNull(defaultTimeoutMs) {
                        op.execute(gatt)
                    }
                    if (result == null) {
                        op.deferred.completeExceptionally(GattTimeoutException("GATT operation timed out after ${defaultTimeoutMs}ms"))
                    }
                } catch (e: Exception) {
                    op.deferred.completeExceptionally(e)
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun <T> GattOperation<T>.execute(gatt: BluetoothGatt) {
        val res = action(gatt)
        deferred.complete(res)
    }

    suspend fun <T> enqueue(action: suspend (BluetoothGatt) -> T): T {
        val op = GattOperation.Read(action = action)
        queue.send(op)
        return op.deferred.await()
    }

    fun clear() {
        queue.cancel()
    }
}

class GattTimeoutException(message: String) : Exception(message)
