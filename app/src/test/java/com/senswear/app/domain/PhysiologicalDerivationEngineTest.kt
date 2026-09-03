package com.senswear.app.domain

import com.senswear.app.core.domain.model.HeartRateZone
import com.senswear.app.core.domain.model.PhysiologicalDerivationEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PhysiologicalDerivationEngineTest {

    @Test
    fun `deriveDistanceKm accurately computes distance from steps`() {
        val distance = PhysiologicalDerivationEngine.deriveDistanceKm(steps = 10000, strideLengthMeters = 0.762)
        assertEquals(7.62, distance, 0.01)

        val zeroDistance = PhysiologicalDerivationEngine.deriveDistanceKm(0)
        assertEquals(0.0, zeroDistance, 0.01)
    }

    @Test
    fun `deriveCaloriesFromSteps calculates realistic caloric expenditure`() {
        val cals = PhysiologicalDerivationEngine.deriveCaloriesFromSteps(steps = 10000, userWeightKg = 70.0)
        assertEquals(400, cals)

        val zeroCals = PhysiologicalDerivationEngine.deriveCaloriesFromSteps(0)
        assertEquals(0, zeroCals)
    }

    @Test
    fun `deriveCaloriesFromHeartRate implements Keytel formula`() {
        val cals = PhysiologicalDerivationEngine.deriveCaloriesFromHeartRate(
            heartRateBpm = 145,
            durationMinutes = 30.0,
            userWeightKg = 75.0,
            userAgeYears = 26,
            isMale = true
        )
        assertTrue(cals in 250..500)
    }

    @Test
    fun `deriveStressFromHrv maps parasympathetic balance correctly`() {
        val relaxedStress = PhysiologicalDerivationEngine.deriveStressFromHrv(hrvRmssdMs = 85)
        assertTrue(relaxedStress!! < 35)

        val highStress = PhysiologicalDerivationEngine.deriveStressFromHrv(hrvRmssdMs = 22)
        assertTrue(highStress!! > 70)

        assertNull(PhysiologicalDerivationEngine.deriveStressFromHrv(null))
    }

    @Test
    fun `deriveHeartRateZone calculates correct zone ranges`() {
        val restZone = PhysiologicalDerivationEngine.deriveHeartRateZone(bpm = 65, restingHr = 60, maxHr = 190)
        assertEquals(HeartRateZone.REST, restZone)

        val aerobicZone = PhysiologicalDerivationEngine.deriveHeartRateZone(bpm = 155, restingHr = 60, maxHr = 190)
        assertEquals(HeartRateZone.ZONE_3, aerobicZone)

        val maxZone = PhysiologicalDerivationEngine.deriveHeartRateZone(bpm = 185, restingHr = 60, maxHr = 190)
        assertEquals(HeartRateZone.ZONE_5, maxZone)
    }

    @Test
    fun `deriveSleepScore evaluates restorative sleep architecture`() {
        val score = PhysiologicalDerivationEngine.deriveSleepScore(
            durationMinutes = 480, // 8 hrs
            deepMinutes = 100,
            remMinutes = 110,
            awakeMinutes = 20
        )
        assertTrue(score in 85..98)

        val poorScore = PhysiologicalDerivationEngine.deriveSleepScore(
            durationMinutes = 240, // 4 hrs
            deepMinutes = 20,
            remMinutes = 20,
            awakeMinutes = 60
        )
        assertTrue(poorScore < 60)
    }

    @Test
    fun `deriveTemperatureDelta calculates baseline deviation accurately`() {
        val delta = PhysiologicalDerivationEngine.deriveTemperatureDelta(currentTempCelsius = 37.4, baselineCelsius = 36.6)
        assertEquals(0.8, delta!!, 0.01)

        assertNull(PhysiologicalDerivationEngine.deriveTemperatureDelta(null))
    }
}
