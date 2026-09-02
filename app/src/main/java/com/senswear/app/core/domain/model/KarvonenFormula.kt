package com.senswear.app.core.domain.model

data class PersonalizedHeartRateZones(
    val zone1Range: IntRange,
    val zone2Range: IntRange,
    val zone3Range: IntRange,
    val zone4Range: IntRange,
    val zone5Range: IntRange
)

object KarvonenFormula {
    fun computeZones(restingHr: Int, maxHr: Int): PersonalizedHeartRateZones {
        val hrr = (maxHr - restingHr).coerceAtLeast(40)

        fun targetHr(pct: Double): Int = (restingHr + (hrr * pct)).toInt()

        return PersonalizedHeartRateZones(
            zone1Range = targetHr(0.50)..targetHr(0.60),
            zone2Range = targetHr(0.60)..targetHr(0.70),
            zone3Range = targetHr(0.70)..targetHr(0.80),
            zone4Range = targetHr(0.80)..targetHr(0.90),
            zone5Range = targetHr(0.90)..maxHr
        )
    }
}
