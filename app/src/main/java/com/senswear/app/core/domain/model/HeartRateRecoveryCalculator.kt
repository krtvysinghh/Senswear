package com.senswear.app.core.domain.model

data class HrrResult(
    val dropBpm: Int,
    val recoveryRating: String
)

object HeartRateRecoveryCalculator {
    fun calculate1MinRecovery(peakHrBpm: Int, post1MinHrBpm: Int): HrrResult {
        val drop = (peakHrBpm - post1MinHrBpm).coerceAtLeast(0)
        val rating = when {
            drop >= 35 -> "Elite Recovery (> 35 BPM drop)"
            drop >= 25 -> "Excellent Recovery (25–34 BPM drop)"
            drop >= 18 -> "Good / Normal Recovery (18–24 BPM drop)"
            drop >= 12 -> "Fair (12–17 BPM drop)"
            else -> "Delayed Recovery (< 12 BPM drop)"
        }
        return HrrResult(dropBpm = drop, recoveryRating = rating)
    }
}
