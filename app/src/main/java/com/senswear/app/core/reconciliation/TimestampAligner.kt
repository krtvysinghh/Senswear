package com.senswear.app.core.reconciliation

object TimestampAligner {
    fun alignToMinuteBucket(timestampEpochMs: Long): Long {
        return (timestampEpochMs / 60_000L) * 60_000L
    }

    fun alignToHourBucket(timestampEpochMs: Long): Long {
        return (timestampEpochMs / 3_600_000L) * 3_600_000L
    }

    fun calculateClockDriftOffset(deviceEpochMs: Long, systemEpochMs: Long): Long {
        return systemEpochMs - deviceEpochMs
    }
}
