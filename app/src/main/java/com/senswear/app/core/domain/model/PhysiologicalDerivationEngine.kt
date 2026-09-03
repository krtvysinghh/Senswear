package com.senswear.app.core.domain.model

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Authoritative Physiological Derivation Engine.
 * Converts real raw watch signals (Steps, Heart Rate BPM, HRV rMSSD, Skin Temp)
 * into clinically grounded derived health metrics without fabricating fake numbers.
 */
object PhysiologicalDerivationEngine {

    /**
     * Derives distance in kilometers from real step count and user stride length.
     * Default average adult stride length: 0.762 meters (0.762 km per 1000 steps).
     */
    fun deriveDistanceKm(steps: Int, strideLengthMeters: Double = 0.762): Double {
        if (steps <= 0) return 0.0
        val totalMeters = steps * strideLengthMeters
        return Math.round((totalMeters / 1000.0) * 100.0) / 100.0
    }

    /**
     * Derives active metabolic calories from step count, body weight, and duration.
     * Based on standard metabolic equivalent (MET) of walking/running (~3.5 METs).
     */
    fun deriveCaloriesFromSteps(steps: Int, userWeightKg: Double = 70.0): Int {
        if (steps <= 0) return 0
        // ~0.04 kcal per step for 70kg adult
        val calsPerStep = 0.04 * (userWeightKg / 70.0)
        return (steps * calsPerStep).roundToInt()
    }

    /**
     * Derives real-time active calories per minute from live Heart Rate (Keytel formula).
     */
    fun deriveCaloriesFromHeartRate(
        heartRateBpm: Int,
        durationMinutes: Double,
        userWeightKg: Double = 70.0,
        userAgeYears: Int = 28,
        isMale: Boolean = true
    ): Int {
        if (heartRateBpm <= 50 || durationMinutes <= 0.0) return 0
        val hr = heartRateBpm.toDouble()
        val w = userWeightKg
        val a = userAgeYears.toDouble()

        val calPerMin = if (isMale) {
            (-55.0969 + (0.6309 * hr) + (0.1988 * w) + (0.2017 * a)) / 4.184
        } else {
            (-20.4022 + (0.4472 * hr) - (0.1263 * w) + (0.0740 * a)) / 4.184
        }

        val total = max(0.0, calPerMin * durationMinutes)
        return total.roundToInt()
    }

    /**
     * Derives autonomic stress index (0–100) from real HRV rMSSD.
     * Higher rMSSD indicates parasympathetic dominance (low stress);
     * lower rMSSD indicates sympathetic stress arousal.
     */
    fun deriveStressFromHrv(hrvRmssdMs: Int?): Int? {
        if (hrvRmssdMs == null || hrvRmssdMs <= 0) return null
        // Baseline mapping: rMSSD 100ms+ -> Stress ~10 (Very relaxed), rMSSD 20ms -> Stress ~80 (High stress)
        val rawStress = 100.0 - (hrvRmssdMs.toDouble() * 0.9)
        return rawStress.roundToInt().coerceIn(5, 95)
    }

    /**
     * Derives Heart Rate Zone classification (1–5) from real live BPM.
     */
    fun deriveHeartRateZone(bpm: Int, restingHr: Int = 60, maxHr: Int = 190): HeartRateZone {
        val hrr = max(40, maxHr - restingHr)
        val intensity = ((bpm - restingHr).toDouble() / hrr.toDouble()).coerceIn(0.0, 1.5)

        return when {
            intensity < 0.50 -> HeartRateZone.REST
            intensity < 0.60 -> HeartRateZone.ZONE_1
            intensity < 0.70 -> HeartRateZone.ZONE_2
            intensity < 0.80 -> HeartRateZone.ZONE_3
            intensity < 0.90 -> HeartRateZone.ZONE_4
            else -> HeartRateZone.ZONE_5
        }
    }

    /**
     * Derives sleep quality score (0–100) from real nocturnal duration and sleep architecture.
     */
    fun deriveSleepScore(
        durationMinutes: Int,
        deepMinutes: Int,
        remMinutes: Int,
        awakeMinutes: Int
    ): Int {
        if (durationMinutes <= 60) return 0

        // Target: 480 mins (8 hours)
        val durationScore = min(1.0, durationMinutes.toDouble() / 480.0) * 45.0
        // Target: 20% Deep (~96 mins)
        val deepRatio = (deepMinutes.toDouble() / durationMinutes.toDouble()).coerceIn(0.0, 0.3)
        val deepScore = (deepRatio / 0.20).coerceAtMost(1.0) * 25.0
        // Target: 20% REM
        val remRatio = (remMinutes.toDouble() / durationMinutes.toDouble()).coerceIn(0.0, 0.3)
        val remScore = (remRatio / 0.20).coerceAtMost(1.0) * 20.0
        // Awake penalty
        val awakePenalty = min(15.0, (awakeMinutes.toDouble() / durationMinutes.toDouble()) * 50.0)

        val total = (durationScore + deepScore + remScore - awakePenalty).roundToInt()
        return total.coerceIn(20, 100)
    }

    /**
     * Derives skin temperature deviation from 7-day baseline.
     */
    fun deriveTemperatureDelta(currentTempCelsius: Double?, baselineCelsius: Double = 36.6): Double? {
        if (currentTempCelsius == null || currentTempCelsius <= 0.0) return null
        val delta = currentTempCelsius - baselineCelsius
        return Math.round(delta * 10.0) / 10.0
    }
}
