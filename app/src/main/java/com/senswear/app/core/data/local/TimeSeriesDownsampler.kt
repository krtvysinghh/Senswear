package com.senswear.app.core.data.local

/**
 * Downsamples high-frequency second-by-second physiological metrics (e.g. 25Hz PPG)
 * into compact 1-minute and 1-hour averages for long-term database storage efficiency.
 */
class TimeSeriesDownsampler {

    data class RawDataPoint(
        val timestampMs: Long,
        val value: Double
    )

    data class AggregatedBucket(
        val bucketStartMs: Long,
        val min: Double,
        val max: Double,
        val avg: Double,
        val sampleCount: Int
    )

    fun downsampleToMinutes(dataPoints: List<RawDataPoint>, bucketIntervalMs: Long = 60_000L): List<AggregatedBucket> {
        if (dataPoints.isEmpty()) return emptyList()

        return dataPoints
            .groupBy { it.timestampMs / bucketIntervalMs }
            .map { (bucketIndex, points) ->
                val values = points.map { it.value }
                AggregatedBucket(
                    bucketStartMs = bucketIndex * bucketIntervalMs,
                    min = values.minOrNull() ?: 0.0,
                    max = values.maxOrNull() ?: 0.0,
                    avg = values.average(),
                    sampleCount = points.size
                )
            }
            .sortedBy { it.bucketStartMs }
    }
}
