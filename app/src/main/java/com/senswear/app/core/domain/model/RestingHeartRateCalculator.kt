package com.senswear.app.core.domain.model

/**
 * Calculates true circadian resting heart rate by isolating nocturnal slow-wave sleep
 * windows rather than simply taking the absolute lowest active daytime spike.
 */
class RestingHeartRateCalculator {

    data class NocturnalHeartRateSample(
        val timestampMs: Long,
        val bpm: Int,
        val sleepStage: SleepStage
    )

    enum class SleepStage {
        AWAKE,
        LIGHT,
        DEEP,
        REM
    }

    fun calculateCircadianRhr(samples: List<NocturnalHeartRateSample>): Int? {
        if (samples.isEmpty()) return null

        // 1. Prefer Deep Sleep (Slow-Wave) samples
        val deepSleepSamples = samples.filter { it.sleepStage == SleepStage.DEEP && it.bpm in 35..120 }
        if (deepSleepSamples.size >= 10) {
            // Use 10th percentile to reject momentary micro-arousals while avoiding sensor drops
            val sorted = deepSleepSamples.map { it.bpm }.sorted()
            val index = (sorted.size * 0.10).toInt().coerceIn(0, sorted.size - 1)
            return sorted[index]
        }

        // 2. Fallback to general sleep (Light + Deep + REM)
        val allSleepSamples = samples.filter { it.sleepStage != SleepStage.AWAKE && it.bpm in 35..120 }
        if (allSleepSamples.isNotEmpty()) {
            val sorted = allSleepSamples.map { it.bpm }.sorted()
            val index = (sorted.size * 0.15).toInt().coerceIn(0, sorted.size - 1)
            return sorted[index]
        }

        // 3. Absolute minimum fallback
        return samples.map { it.bpm }.filter { it in 35..120 }.minOrNull()
    }
}
