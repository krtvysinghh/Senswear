package com.senswear.app.ble

import com.senswear.app.core.ble.UniversalBleDecoder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UniversalBleDecoderTest {

    @Test
    fun `decodeHeartRate decodes standard 8-bit HR with RR-intervals`() {
        // Flags: 0x10 (has RR intervals, 8-bit HR)
        // BPM: 72 (0x48)
        // RR 1: 853 ms (~873 in 1/1024s -> 0x0369 -> 0x69, 0x03)
        // RR 2: 833 ms (~853 in 1/1024s -> 0x0355 -> 0x55, 0x03)
        val data = byteArrayOf(
            0x10.toByte(),
            0x48.toByte(),
            0x69.toByte(), 0x03.toByte(),
            0x55.toByte(), 0x03.toByte()
        )

        val result = UniversalBleDecoder.decodeHeartRate(data)
        assertNotNull(result)
        assertEquals(72, result!!.reading.bpm)
        assertEquals(2, result.rrIntervalsMs.size)
        assertNotNull(result.hrvReading)
        assertTrue(result.hrvReading!!.rmssdMs > 0)
    }

    @Test
    fun `decodeHeartRate decodes standard 16-bit HR measurement`() {
        // Flags: 0x01 (16-bit HR)
        // BPM: 165 (0x00A5 -> 0xA5, 0x00)
        val data = byteArrayOf(0x01.toByte(), 0xA5.toByte(), 0x00.toByte())

        val result = UniversalBleDecoder.decodeHeartRate(data)
        assertNotNull(result)
        assertEquals(165, result!!.reading.bpm)
    }

    @Test
    fun `decodeRunningSpeedCadence decodes speed and SPM accurately`() {
        // Flags: 0x00 (basic speed + cadence)
        // Speed: 2.5 m/s -> 2.5 * 256 = 640 (0x0280 -> 0x80, 0x02)
        // Cadence: 172 SPM (0xAC)
        val data = byteArrayOf(0x00.toByte(), 0x80.toByte(), 0x02.toByte(), 0xAC.toByte())

        val result = UniversalBleDecoder.decodeRunningSpeedCadence(data)
        assertNotNull(result)
        assertEquals(2.5, result!!.speedMetersPerSecond, 0.01)
        assertEquals(172, result.cadenceSpm)
    }

    @Test
    fun `decodeBatteryLevel parses uint8 percentage`() {
        val data = byteArrayOf(88.toByte())
        val level = UniversalBleDecoder.decodeBatteryLevel(data)
        assertEquals(88, level)
    }

    @Test
    fun `decodeTemperature decodes IEEE 11073 32-bit float in Celsius`() {
        // 36.6 C -> 366 * 10^-1 -> mantissa: 366 (0x00016E), exponent: -1 (0xFF)
        val data = byteArrayOf(0x00.toByte(), 0x6E.toByte(), 0x01.toByte(), 0x00.toByte(), 0xFF.toByte())

        val temp = UniversalBleDecoder.decodeTemperature(data)
        assertNotNull(temp)
        assertEquals(36.6, temp!!.temperatureCelsius, 0.1)
    }
}
