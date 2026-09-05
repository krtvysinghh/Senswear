package com.senswear.app.core.reconciliation

import com.senswear.app.core.wearable.DataProvenance
import java.security.MessageDigest

/**
 * Ensures cloud REST sync items are ingested idempotently without triggering duplicate records
 * or corrupting rolling daily aggregations.
 */
class IdempotentCloudIngestor {
    private val processedHashes = mutableSetOf<String>()

    fun generateRecordFingerprint(metricName: String, timestampMs: Long, value: Double, sourceDeviceId: String): String {
        val raw = "$metricName:$timestampMs:$value:$sourceDeviceId"
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(raw.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun shouldIngest(fingerprint: String): Boolean {
        return if (processedHashes.contains(fingerprint)) {
            false
        } else {
            processedHashes.add(fingerprint)
            true
        }
    }

    fun clear() {
        processedHashes.clear()
    }
}
