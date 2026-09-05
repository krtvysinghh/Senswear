package com.senswear.app.core.healthconnect

import java.util.concurrent.ConcurrentHashMap

/**
 * Persistently tracks Health Connect ChangesTokens to enable incremental sync
 * instead of scanning full historical timeframes repeatedly.
 */
class HealthConnectChangeTracker {
    private val tokenStore = ConcurrentHashMap<String, String>()

    fun getLatestChangesToken(recordType: String): String? {
        return tokenStore[recordType]
    }

    fun updateChangesToken(recordType: String, token: String) {
        tokenStore[recordType] = token
    }

    fun clear(recordType: String) {
        tokenStore.remove(recordType)
    }
}
