package com.senswear.app.core.domain.model

data class BatteryHealthReport(
    val percentage: Int,
    val estimatedHoursRemaining: Int,
    val dischargeRatePercentPerDay: Double,
    val isLowBatteryWarning: Boolean
)

object BatteryDischargeEstimator {
    fun calculateHealth(percentage: Int, isCharging: Boolean): BatteryHealthReport {
        val days = (percentage * 45.0 / 100.0).coerceAtLeast(0.5)
        val hours = (days * 24).toInt()
        val isLow = percentage <= 15 && !isCharging
        return BatteryHealthReport(
            percentage = percentage,
            estimatedHoursRemaining = hours,
            dischargeRatePercentPerDay = 2.22, // ~45 days per 100%
            isLowBatteryWarning = isLow
        )
    }
}
