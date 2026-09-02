package com.senswear.app.domain

import com.senswear.app.core.domain.model.BedtimeRecommender
import com.senswear.app.core.domain.model.CadenceAnalyzer
import com.senswear.app.core.domain.model.HeartRateRecoveryCalculator
import com.senswear.app.core.domain.model.KarvonenFormula
import com.senswear.app.core.domain.model.SleepRegularityIndex
import com.senswear.app.core.domain.model.SleepSession
import com.senswear.app.core.domain.model.Vo2MaxEstimator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdvancedMetricsTest {

    @Test
    fun `Vo2MaxEstimator calculates correct aerobic capacity`() {
        val vo2 = Vo2MaxEstimator.estimateFromRestingHeartRate(restingBpm = 60, maxBpm = 190)
        assertEquals(48.5, vo2.scoreMlKgMin, 0.5)
        assertEquals("Excellent", vo2.fitnessCategory)
    }

    @Test
    fun `HeartRateRecoveryCalculator grades post-workout drop accurately`() {
        val hrr = HeartRateRecoveryCalculator.calculate1MinRecovery(peakHrBpm = 165, post1MinHrBpm = 138)
        assertEquals(27, hrr.dropBpm)
        assertTrue(hrr.recoveryRating.contains("Excellent"))
    }

    @Test
    fun `KarvonenFormula calculates personalized target heart rate zones`() {
        val zones = KarvonenFormula.computeZones(restingHr = 60, maxHr = 190)
        assertEquals(125, zones.zone1Range.first)
        assertEquals(138, zones.zone2Range.first)
    }

    @Test
    fun `CadenceAnalyzer categorizes step cadence zones`() {
        val analysis = CadenceAnalyzer.analyze(listOf(170, 175, 180), durationMinutes = 3)
        assertEquals(175, analysis.averageSpm)
        assertTrue(analysis.cadenceZone.contains("Optimal"))
    }

    @Test
    fun `BedtimeRecommender computes target bedtime from wake time`() {
        val window = BedtimeRecommender.calculateOptimalBedtime(desiredWakeHour = 7, desiredWakeMinute = 0, sleepGoalHours = 8.0)
        assertTrue(window.idealBedtimeString.contains("10:45 PM"))
    }
}
