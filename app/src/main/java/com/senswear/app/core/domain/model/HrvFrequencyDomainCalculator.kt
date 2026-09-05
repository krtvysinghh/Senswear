package com.senswear.app.core.domain.model

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Calculates Frequency-Domain Heart Rate Variability (HRV) metrics:
 * - VLF (Very Low Frequency): < 0.04 Hz
 * - LF (Low Frequency - Sympathetic & Parasympathetic): 0.04 - 0.15 Hz
 * - HF (High Frequency - Parasympathetic / Vagal tone): 0.15 - 0.40 Hz
 * - LF/HF Ratio: Autonomic sympathovagal balance indicator
 */
class HrvFrequencyDomainCalculator {

    data class FrequencyDomainResult(
        val lfPower: Double,
        val hfPower: Double,
        val vlfPower: Double,
        val totalPower: Double,
        val lfHfRatio: Double,
        val normalizedLf: Double,
        val normalizedHf: Double
    )

    /**
     * Estimates power spectral density from unevenly spaced RR intervals using Lomb-Scargle periodogram.
     * @param rrIntervalsMs List of RR intervals in milliseconds.
     */
    fun calculate(rrIntervalsMs: List<Double>): FrequencyDomainResult? {
        if (rrIntervalsMs.size < 30) return null

        // Convert RR intervals to cumulative timestamps in seconds
        val timesSec = mutableListOf<Double>()
        var cumulative = 0.0
        for (rr in rrIntervalsMs) {
            cumulative += rr / 1000.0
            timesSec.add(cumulative)
        }

        val meanRr = rrIntervalsMs.average()
        val centeredRr = rrIntervalsMs.map { it - meanRr }

        var vlfPower = 0.0
        var lfPower = 0.0
        var hfPower = 0.0

        // Evaluate frequencies from 0.01 Hz to 0.40 Hz in steps of 0.005 Hz
        val step = 0.005
        var f = 0.01
        while (f <= 0.40) {
            val omega = 2.0 * Math.PI * f

            var cSum = 0.0
            var sSum = 0.0
            for (i in centeredRr.indices) {
                cSum += centeredRr[i] * cos(omega * timesSec[i])
                sSum += centeredRr[i] * sin(omega * timesSec[i])
            }

            val power = (cSum * cSum + sSum * sSum) / centeredRr.size

            when {
                f < 0.04 -> vlfPower += power * step
                f <= 0.15 -> lfPower += power * step
                else -> hfPower += power * step
            }
            f += step
        }

        val totalPower = vlfPower + lfPower + hfPower
        val lfHfRatio = if (hfPower > 0.0001) lfPower / hfPower else 1.0
        val normalizedLf = if (lfPower + hfPower > 0.0001) (lfPower / (lfPower + hfPower)) * 100.0 else 50.0
        val normalizedHf = if (lfPower + hfPower > 0.0001) (hfPower / (lfPower + hfPower)) * 100.0 else 50.0

        return FrequencyDomainResult(
            lfPower = lfPower,
            hfPower = hfPower,
            vlfPower = vlfPower,
            totalPower = totalPower,
            lfHfRatio = lfHfRatio,
            normalizedLf = normalizedLf,
            normalizedHf = normalizedHf
        )
    }
}
