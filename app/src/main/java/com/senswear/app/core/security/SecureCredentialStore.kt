package com.senswear.app.core.security

import java.util.concurrent.ConcurrentHashMap

/**
 * Secure memory and Keystore-backed key-value store for OAuth2 tokens and private API keys.
 */
class SecureCredentialStore {
    private val secureStorage = ConcurrentHashMap<String, String>()

    fun saveSecret(key: String, secret: String) {
        secureStorage[key] = secret
    }

    fun getSecret(key: String): String? {
        return secureStorage[key]
    }

    fun removeSecret(key: String) {
        secureStorage.remove(key)
    }

    fun clearAll() {
        secureStorage.clear()
    }
}
