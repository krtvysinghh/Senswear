package com.senswear.app.core.security

import java.io.File
import java.security.SecureRandom

/**
 * Securely overwrites and deletes sensitive local databases, logs, and telemetry caches.
 */
class DataShredder {

    fun shredFile(file: File, passes: Int = 3): Boolean {
        if (!file.exists()) return true
        if (file.isDirectory) {
            file.listFiles()?.forEach { shredFile(it, passes) }
            return file.delete()
        }

        try {
            val length = file.length()
            val random = SecureRandom()
            val buffer = ByteArray(4096)

            file.outputStream().use { out ->
                repeat(passes) {
                    var written = 0L
                    while (written < length) {
                        random.nextBytes(buffer)
                        val toWrite = minOf(buffer.size.toLong(), length - written).toInt()
                        out.write(buffer, 0, toWrite)
                        written += toWrite
                    }
                    out.flush()
                }
            }
            return file.delete()
        } catch (e: Exception) {
            return file.delete() // Fallback to normal delete
        }
    }
}
