package com.senswear.app.core.security

/**
 * Manages biometric app lock timeout and authentication state.
 */
class BiometricLockManager(
    private var lockTimeoutMs: Long = 60_000L,
    private val timeProvider: () -> Long = { System.currentTimeMillis() }
) {
    enum class LockTimeout(val millis: Long) {
        IMMEDIATELY(0L),
        ONE_MINUTE(60_000L),
        FIVE_MINUTES(300_000L),
        FIFTEEN_MINUTES(900_000L),
        NEVER(-1L)
    }

    private var lastBackgroundedTimestampMs: Long = 0L
    private var isUnlockedInCurrentSession: Boolean = false

    fun onAppBackgrounded() {
        lastBackgroundedTimestampMs = timeProvider()
    }

    fun isAuthenticationRequired(): Boolean {
        if (lockTimeoutMs < 0) return false // Lock disabled
        if (!isUnlockedInCurrentSession) return true

        if (lastBackgroundedTimestampMs == 0L) return false

        val elapsed = timeProvider() - lastBackgroundedTimestampMs
        return elapsed > lockTimeoutMs
    }

    fun onAuthenticationSuccessful() {
        isUnlockedInCurrentSession = true
        lastBackgroundedTimestampMs = 0L
    }

    fun setLockTimeout(timeout: LockTimeout) {
        this.lockTimeoutMs = timeout.millis
    }
}
