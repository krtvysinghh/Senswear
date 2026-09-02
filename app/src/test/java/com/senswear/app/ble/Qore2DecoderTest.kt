package com.senswear.app.ble

import com.senswear.app.core.ble.Qore2Decoder
import com.senswear.app.core.ble.Qore2Protocol
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Qore2DecoderTest {

    @Test
    fun `decodeHeartRate correctly decodes 8-bit standard GATT HR payload`() {
        // Flags: 0x00 (8-bit HR), BPM: 76 (0x4C)
        val payload = byteArrayOf(0x00, 0x4C)
        val reading = Qore2Decoder.decodeHeartRate(payload)

        assertNotNull(reading)
        assertEquals(76, reading?.bpm)
    }

    @Test
    fun `decodeHeartRate correctly decodes 16-bit standard GATT HR payload`() {
        // Flags: 0x01 (16-bit HR), BPM: 135 (0x0087 LE -> 0x87, 0x00)
        val payload = byteArrayOf(0x01, 0x87.toByte(), 0x00)
        val reading = Qore2Decoder.decodeHeartRate(payload)

        assertNotNull(reading)
        assertEquals(135, reading?.bpm)
    }

    @Test
    fun `decodeBatteryLevel decodes single byte battery percentage`() {
        val payload = byteArrayOf(84.toByte())
        val battery = Qore2Decoder.decodeBatteryLevel(payload)

        assertEquals(84, battery)
    }

    @Test
    fun `decodeTemperature decodes IEEE 11073 32-bit float in Celsius`() {
        // Flag: 0x00 (Celsius)
        // Mantissa: 366 (0x00016E -> 0x6E, 0x01, 0x00), Exponent: -1 (0xFF) => 36.6°C
        val payload = byteArrayOf(0x00, 0x6E, 0x01, 0x00, 0xFF.toByte())
        val reading = Qore2Decoder.decodeTemperature(payload)

        assertNotNull(reading)
        assertEquals(36.6, reading?.temperatureCelsius ?: 0.0, 0.05)
    }

    @Test
    fun `decodeVendorLiveTelemetry decodes live multi-sensor packet with valid checksum`() {
        val buffer = ByteBuffer.allocate(18).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put(Qore2Protocol.OPCODE_LIVE_METRICS_REPORT) // 0x10
        buffer.putInt(8421) // Steps
        buffer.putShort(342.toShort()) // Calories
        buffer.putShort(6400.toShort()) // Distance
        buffer.put(76.toByte()) // HR
        buffer.put(98.toByte()) // SpO2
        buffer.putShort(54.toShort()) // HRV
        buffer.put(22.toByte()) // Stress
        buffer.putShort(366.toShort()) // Temp 36.6°C
        buffer.put(84.toByte()) // Battery

        val array = buffer.array()
        var checksum: Byte = 0
        for (i in 0 until 17) {
            checksum = (checksum.toInt() xor array[i].toInt()).toByte()
        }
        array[17] = checksum

        val telemetry = Qore2Decoder.decodeVendorLiveTelemetry(array)

        assertNotNull(telemetry)
        assertEquals(8421, telemetry?.steps)
        assertEquals(342, telemetry?.activeCalories)
        assertEquals(76, telemetry?.heartRate)
        assertEquals(98, telemetry?.spo2)
        assertEquals(54, telemetry?.hrv)
        assertEquals(22, telemetry?.stress)
        assertEquals(36.6, telemetry?.skinTempCelsius ?: 0.0, 0.05)
        assertEquals(84, telemetry?.batteryPercent)
    }

    @Test
    fun `decodeVendorLiveTelemetry rejects packet with corrupted checksum`() {
        val array = ByteArray(18)
        array[0] = Qore2Protocol.OPCODE_LIVE_METRICS_REPORT
        array[17] = 0x55 // invalid checksum

        val telemetry = Qore2Decoder.decodeVendorLiveTelemetry(array)
        assertNull(telemetry)
    }
}
