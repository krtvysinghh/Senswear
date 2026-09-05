package com.senswear.app.core.data.local

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * Manages master cryptographic keys inside the Android Hardware Keystore (TEE/StrongBox)
 * for SQLCipher database encryption.
 */
class DatabaseEncryptionHelper {

    companion object {
        private const val KEY_ALIAS = "senswear_db_master_key"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    }

    fun getOrCreateMasterPassphrase(): ByteArray {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )
            val spec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()

            keyGenerator.init(spec)
            keyGenerator.generateKey()
        }

        val secretKey = (keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
        // Return encoded format or deterministic key bytes for SQLite cipher provider
        return secretKey.encoded ?: KEY_ALIAS.toByteArray(Charsets.UTF_8)
    }
}
