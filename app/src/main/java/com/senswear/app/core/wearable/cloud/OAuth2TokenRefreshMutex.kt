package com.senswear.app.core.wearable.cloud

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Thread-safe coroutine mutex preventing duplicate concurrent token refresh calls
 * when multiple requests detect an expired access token simultaneously.
 */
class OAuth2TokenRefreshMutex {
    private val mutex = Mutex()

    suspend fun <T> withRefreshLock(block: suspend () -> T): T {
        return mutex.withLock {
            block()
        }
    }
}
