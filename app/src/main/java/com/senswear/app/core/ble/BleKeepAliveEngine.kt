package com.senswear.app.core.ble

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Periodically pings connected wearables via lightweight characteristic reads to defeat
 * aggressive OEM battery management task-killers (MIUI, EMUI, OneUI).
 */
class BleKeepAliveEngine(
    private val pingIntervalMs: Long = 15 * 60 * 1000L, // 15 minutes
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) {
    private var keepAliveJob: Job? = null
    private val _lastPingTimestamp = MutableStateFlow<Long>(0L)
    val lastPingTimestamp: StateFlow<Long> = _lastPingTimestamp.asStateFlow()

    fun start(onPing: suspend () -> Boolean) {
        stop()
        keepAliveJob = scope.launch {
            while (isActive) {
                delay(pingIntervalMs)
                val success = try {
                    onPing()
                } catch (e: Exception) {
                    false
                }
                if (success) {
                    _lastPingTimestamp.value = System.currentTimeMillis()
                }
            }
        }
    }

    fun stop() {
        keepAliveJob?.cancel()
        keepAliveJob = null
    }
}
