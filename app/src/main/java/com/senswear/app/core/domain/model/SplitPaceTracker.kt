package com.senswear.app.core.domain.model

data class KmSplit(
    val splitNumber: Int,
    val splitDurationSeconds: Long,
    val avgHeartRateBpm: Int
)

class SplitPaceTracker {
    private val splits = mutableListOf<KmSplit>()
    private var lastSplitDistanceMeters = 0.0
    private var lastSplitTimeEpochMs = 0L

    fun processSample(distanceMeters: Double, timestampEpochMs: Long, hrBpm: Int): KmSplit? {
        val totalKm = (distanceMeters / 1000.0).toInt()
        val currentKm = (lastSplitDistanceMeters / 1000.0).toInt()

        if (totalKm > currentKm) {
            val splitDuration = if (lastSplitTimeEpochMs > 0) (timestampEpochMs - lastSplitTimeEpochMs) / 1000 else 0
            val split = KmSplit(
                splitNumber = totalKm,
                splitDurationSeconds = splitDuration,
                avgHeartRateBpm = hrBpm
            )
            splits.add(split)
            lastSplitDistanceMeters = totalKm * 1000.0
            lastSplitTimeEpochMs = timestampEpochMs
            return split
        }
        return null
    }

    fun getSplits(): List<KmSplit> = splits.toList()
}
