package com.senswear.app.domain

import com.senswear.app.core.ble.AdaptiveBleScanScheduler
import com.senswear.app.core.ble.MockGattServerTestBed
import com.senswear.app.core.ble.PacketBufferCompactor
import com.senswear.app.core.data.local.TimeSeriesDownsampler
import com.senswear.app.core.data.repository.FitGpxExporter
import com.senswear.app.core.domain.model.*
import com.senswear.app.core.reconciliation.*
import com.senswear.app.core.security.BiometricLockManager
import com.senswear.app.core.security.SecureCredentialStore
import com.senswear.app.core.wearable.DataProvenance
import com.senswear.app.core.wearable.DataQuality
import com.senswear.app.core.wearable.DynamicConfidenceDecay
import com.senswear.app.core.wearable.WearableProtocol
import com.senswear.app.core.wearable.cloud.OAuth2TokenRefreshMutex
import com.senswear.app.core.wearable.cloud.TokenBucketRateLimiter
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ArchitectureMaturitySuiteTest {

    @Test
    fun testHrvFrequencyDomainCalculation() {
        val calc = HrvFrequencyDomainCalculator()
        val rrList = (0 until 50).map { i -> 850.0 + 40.0 * kotlin.math.sin(i * 0.2) }
        val result = calc.calculate(rrList)

        assertNotNull(result)
        assertTrue(result!!.totalPower > 0.0)
        assertTrue(result.lfHfRatio > 0.0)
        assertEquals(100.0, result.normalizedLf + result.normalizedHf, 0.01)
    }

    @Test
    fun testPpgMotionArtifactRejection() {
        val filter = PpgMotionArtifactFilter()
        val clean1 = filter.filterPulse(75, PpgMotionArtifactFilter.MotionVector(0.0, 1.0, 0.0))
        assertFalse(clean1.isArtifactRejected)
        assertEquals(75, clean1.filteredBpm)

        val artifact = filter.filterPulse(150, PpgMotionArtifactFilter.MotionVector(2.0, 2.5, 1.8))
        assertTrue(artifact.isArtifactRejected)
        assertTrue(artifact.filteredBpm < 150)
        assertTrue(artifact.confidenceScore < 0.5f)
    }

    @Test
    fun testRestingHeartRateDeepSleepWindow() {
        val rhrCalc = RestingHeartRateCalculator()
        val samples = listOf(
            RestingHeartRateCalculator.NocturnalHeartRateSample(1000L, 85, RestingHeartRateCalculator.SleepStage.AWAKE),
            RestingHeartRateCalculator.NocturnalHeartRateSample(2000L, 62, RestingHeartRateCalculator.SleepStage.LIGHT),
            RestingHeartRateCalculator.NocturnalHeartRateSample(3000L, 51, RestingHeartRateCalculator.SleepStage.DEEP),
            RestingHeartRateCalculator.NocturnalHeartRateSample(4000L, 49, RestingHeartRateCalculator.SleepStage.DEEP),
            RestingHeartRateCalculator.NocturnalHeartRateSample(5000L, 50, RestingHeartRateCalculator.SleepStage.DEEP)
        )
        val rhr = rhrCalc.calculateCircadianRhr(samples)
        assertNotNull(rhr)
        assertEquals(49, rhr)
    }

    @Test
    fun testCardiovascularDriftCompensation() {
        val compensator = CardiovascularDriftCompensator()
        val effort30 = compensator.compensate(25.0, 150)
        assertEquals(150, effort30.compensatedAerobicBpm)
        assertEquals(0.0, effort30.driftPercentage, 0.01)

        val effort90 = compensator.compensate(90.0, 165, ambientTempCelsius = 28.0)
        assertTrue(effort90.compensatedAerobicBpm < 165)
        assertTrue(effort90.driftPercentage > 5.0)
    }

    @Test
    fun testNocturnalTemperatureAnomalyDetection() {
        val baselineEngine = NocturnalTemperatureBaseline()
        val past7Days = listOf(36.4, 36.5, 36.6, 36.5, 36.4, 36.5, 36.6)
        val feverNight = 37.4

        val result = baselineEngine.evaluateNightlyTemperature(past7Days, feverNight)
        assertTrue(result.isSignificantAnomaly)
        assertEquals(NocturnalTemperatureBaseline.AnomalyIndication.ELEVATED_POSSIBLE_ILLNESS, result.indication)
        assertEquals(36.5, result.baselineCelsius, 0.01)
    }

    @Test
    fun testClockSkewLinearRegression() {
        val skew = ClockSkewEstimator()
        skew.recordSyncPoint(1000L, 1000L)
        skew.recordSyncPoint(2010L, 2000L)
        skew.recordSyncPoint(3020L, 3000L)

        val corrected = skew.estimateTrueTimestamp(4030L)
        assertEquals(4000L, corrected)
    }

    @Test
    fun testWearableHierarchyArbitration() {
        val arbitrator = WearableHierarchyArbitrator()

        val polarEcg = DataProvenance(
            metricName = "heart_rate",
            canonicalValue = 75.0,
            canonicalUnit = "bpm",
            timestampEpochMs = 1000L,
            sourceDeviceName = "Polar H10",
            sourceDeviceId = "polar_h10",
            sourceVendor = "Polar Electro",
            sourceProtocol = WearableProtocol.BLE_GATT_STANDARD,
            confidenceScore = 1.0f
        )
        val galaxyWatch = DataProvenance(
            metricName = "heart_rate",
            canonicalValue = 74.0,
            canonicalUnit = "bpm",
            timestampEpochMs = 1000L,
            sourceDeviceName = "Galaxy Watch 6",
            sourceDeviceId = "galaxy_watch_6",
            sourceVendor = "Samsung",
            sourceProtocol = WearableProtocol.HEALTH_CONNECT_AGGREGATION,
            confidenceScore = 0.9f
        )

        val authoritative = arbitrator.selectAuthoritativeProvenance(polarEcg, galaxyWatch)
        assertEquals("polar_h10", authoritative.sourceDeviceId)
    }

    @Test
    fun testDynamicConfidenceDecayOnPacketDrops() {
        val decay = DynamicConfidenceDecay(windowSizeSamples = 10, maxAllowedDropRate = 0.10f)
        repeat(5) { decay.recordPacketEvent(true) }
        repeat(5) { decay.recordPacketEvent(false) }

        val adjusted = decay.computeAdjustedConfidence(1.0f)
        assertTrue(adjusted < 1.0f)
        assertTrue(adjusted >= 0.1f)
    }

    @Test
    fun testIdempotentCloudIngestor() {
        val ingestor = IdempotentCloudIngestor()
        val fp = ingestor.generateRecordFingerprint("heart_rate", 1700000000L, 72.0, "whoop_4")

        assertTrue(ingestor.shouldIngest(fp))
        assertFalse(ingestor.shouldIngest(fp))
    }

    @Test
    fun testSleepSessionStitching() {
        val stitcher = SleepSessionStitcher(maxBreakDurationMs = 30 * 60 * 1000L)

        val s1 = SleepSessionStitcher.SleepInterval(1000L, 1000L + 2 * 3600 * 1000L, "DEEP", 90)
        val s2 = SleepSessionStitcher.SleepInterval(s1.endTimestampMs + 10 * 60 * 1000L, s1.endTimestampMs + 4 * 3600 * 1000L, "LIGHT", 80)

        val merged = stitcher.stitch(listOf(s1, s2))
        assertEquals(1, merged.size)
        assertEquals(1000L, merged[0].startTimestampMs)
        assertEquals(s2.endTimestampMs, merged[0].endTimestampMs)
    }

    @Test
    fun testTimeSeriesDownsampling() {
        val downsampler = TimeSeriesDownsampler()
        val points = (0 until 120).map { i ->
            TimeSeriesDownsampler.RawDataPoint(timestampMs = i * 1000L, value = 60.0 + (i % 10))
        }

        val buckets = downsampler.downsampleToMinutes(points, bucketIntervalMs = 60_000L)
        assertEquals(2, buckets.size)
        assertEquals(60, buckets[0].sampleCount)
        assertEquals(60, buckets[1].sampleCount)
    }

    @Test
    fun testGpxWorkoutExporter() {
        val exporter = FitGpxExporter()
        val trackpoints = listOf(
            FitGpxExporter.Trackpoint(37.7749, -122.4194, 15.0, 145, 88, 1700000000000L),
            FitGpxExporter.Trackpoint(37.7750, -122.4195, 16.0, 150, 90, 1700000010000L)
        )

        val gpx = exporter.exportToGpx("Morning Tempo Run", trackpoints)
        assertTrue(gpx.contains("<gpx version=\"1.1\""))
        assertTrue(gpx.contains("<gpxtpx:hr>145</gpxtpx:hr>"))
        assertTrue(gpx.contains("<name>Morning Tempo Run</name>"))
    }

    @Test
    fun testOAuth2TokenMutexAndRateLimiter() = runBlocking {
        val mutex = OAuth2TokenRefreshMutex()
        var count = 0
        mutex.withRefreshLock {
            count++
        }
        assertEquals(1, count)

        val rateLimiter = TokenBucketRateLimiter(capacity = 5, refillTokensPerSecond = 10.0)
        rateLimiter.acquire()
        val backoff = rateLimiter.calculateBackoffDelayMs(2)
        assertTrue(backoff >= 4000L)
    }

    @Test
    fun testSecureCredentialStore() {
        val store = SecureCredentialStore()
        store.saveSecret("whoop_token", "secret_abc_123")
        assertEquals("secret_abc_123", store.getSecret("whoop_token"))
        store.removeSecret("whoop_token")
        assertNull(store.getSecret("whoop_token"))
    }

    @Test
    fun testAdaptiveBleScanDutyCycler() {
        val scheduler = AdaptiveBleScanScheduler(baseScanDurationMs = 10_000L, baseSleepDurationMs = 30_000L)
        val cycle0 = scheduler.computeNextCycle(0)
        assertEquals(30_000L, cycle0.sleepDurationMs)

        val cycle3 = scheduler.computeNextCycle(3)
        assertEquals(240_000L, cycle3.sleepDurationMs)
    }

    @Test
    fun testPrimitiveTelemetryBuffer() {
        val buffer = PrimitiveTelemetryBuffer(capacity = 5)
        buffer.append(10.0f, 100L)
        buffer.append(20.0f, 200L)
        buffer.append(30.0f, 300L)

        assertEquals(3, buffer.size)
        assertEquals(30.0f, buffer.getLatestValue()!!, 0.01f)

        val snapshot = buffer.copySnapshot()
        assertEquals(3, snapshot.size)
        assertEquals(10.0f, snapshot[0], 0.01f)
    }

    @Test
    fun testBiometricLockManager() {
        val lockManager = BiometricLockManager(lockTimeoutMs = 1000L)
        assertTrue(lockManager.isAuthenticationRequired())

        lockManager.onAuthenticationSuccessful()
        assertFalse(lockManager.isAuthenticationRequired())
    }

    @Test
    fun testKalmanGpsFilter() {
        val kalman = KalmanGpsFilter()
        val (lat1, lng1) = kalman.filter(37.7749, -122.4194, 5.0, 1000L)
        assertEquals(37.7749, lat1, 0.0001)

        val (lat2, lng2) = kalman.filter(37.7752, -122.4190, 10.0, 2000L)
        assertTrue(lat2 > lat1)
        assertTrue(lng2 > lng1)
    }

    @Test
    fun testBaroGpsElevationFusion() {
        val fusion = BaroGpsElevationFusion()
        val alt1 = fusion.fuse(100.0, 1013.25)
        assertEquals(100.0, alt1, 0.1)

        val alt2 = fusion.fuse(120.0, 1010.0)
        assertTrue(alt2 > 100.0)
    }

    @Test
    fun testHeartRateZoneAnnouncer() {
        val announcer = HeartRateZoneAnnouncer()
        val t1 = announcer.onPulseUpdate(120, maxHr = 190)
        assertNull(t1)

        val t2 = announcer.onPulseUpdate(175, maxHr = 190)
        assertNotNull(t2)
        assertEquals(5, t2!!.newZone)
        assertTrue(t2.shouldAlertUser)
    }

    @Test
    fun testMultiSportSwolfAndRowingEngine() {
        val engine = MultiSportEngine()
        val swolf = engine.calculateSwolf(lapTimeSeconds = 25, strokeCount = 15)
        assertEquals(40, swolf.swolfScore)
        assertEquals("Excellent", swolf.efficiencyRating)

        val row = engine.calculateRowingSplit(distanceMeters = 500.0, elapsedSeconds = 100.0, strokeCount = 50)
        assertEquals(30.0, row.strokeRateSpm, 0.01)
        assertEquals(100.0, row.split500mSeconds, 0.01)
    }

    @Test
    fun testMockGattServerTestBed() {
        val mock = MockGattServerTestBed(simulatedPacketDropRate = 0.0f)
        assertFalse(mock.transmitPacket(byteArrayOf(0x01)))

        mock.simulateConnect()
        assertTrue(mock.transmitPacket(byteArrayOf(0x01)))
        assertEquals(1, mock.getReceivedCount())
    }
}
