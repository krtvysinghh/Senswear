package com.senswear.app.core.ble

import com.senswear.app.core.domain.model.DataSource
import com.senswear.app.core.domain.model.HeartRateReading
import com.senswear.app.core.domain.model.HrvReading
import com.senswear.app.core.domain.model.SleepSession
import com.senswear.app.core.domain.model.SleepStageRecord
import com.senswear.app.core.domain.model.SleepStageType
import com.senswear.app.core.domain.model.Spo2Reading
import com.senswear.app.core.domain.model.StressReading
import com.senswear.app.core.domain.model.TemperatureReading
import java.nio.ByteBuffer
import java.nio.ByteOrder

object Qore2Decoder {

    /**
     * Decodes standard Bluetooth SIG Heart Rate Measurement (UUID 0x2A37).
     * Flag bit 0: 0 = uint8 HR, 1 = uint16 HR
     * Flag bit 4: 1 = RR intervals present
     */
    fun decodeHeartRate(data: ByteArray): HeartRateReading? {
        if (data.isEmpty()) return null
        val flags = data[0].toInt()
        val is16Bit = (flags and 0x01) != 0

        var offset = 1
        if (is16Bit) {
            if (data.size < 3) return null
            val bpm = ((data[2].toInt() and 0xFF) shl 8) or (data[1].toInt() and 0xFF)
            return HeartRateReading(
                timestampEpochMs = System.currentTimeMillis(),
                bpm = bpm.coerceIn(30, 240),
                source = DataSource.PEBBLE_QORE_2_BLE
            )
        } else {
            if (data.size < 2) return null
            val bpm = data[1].toInt() and 0xFF
            return HeartRateReading(
                timestampEpochMs = System.currentTimeMillis(),
                bpm = bpm.coerceIn(30, 240),
                source = DataSource.PEBBLE_QORE_2_BLE
            )
        }
    }

    /**
     * Decodes standard Bluetooth SIG Battery Level (UUID 0x2A19).
     * Single uint8 value (0..100).
     */
    fun decodeBatteryLevel(data: ByteArray): Int? {
        if (data.isEmpty()) return null
        val level = data[0].toInt() and 0xFF
        return level.coerceIn(0, 100)
    }

    /**
     * Decodes standard Bluetooth SIG Health Thermometer (UUID 0x2A1C).
     * Flags + IEEE 11073 32-bit FLOAT.
     */
    fun decodeTemperature(data: ByteArray): TemperatureReading? {
        if (data.size < 5) return null
        val flags = data[0].toInt()
        val isFahrenheit = (flags and 0x01) != 0

        // IEEE 11073 32-bit FLOAT: [mantissa 24-bit signed, exponent 8-bit signed]
        val b1 = data[1].toInt() and 0xFF
        val b2 = data[2].toInt() and 0xFF
        val b3 = data[3].toInt() and 0xFF
        val b4 = data[4].toInt() // exponent

        var mantissa = (b3 shl 16) or (b2 shl 8) or b1
        if ((mantissa and 0x800000) != 0) {
            mantissa = mantissa or -0x1000000 // sign extend
        }
        val exponent = b4
        var rawTemp = mantissa * Math.pow(10.0, exponent.toDouble())

        if (isFahrenheit) {
            rawTemp = (rawTemp - 32.0) * 5.0 / 9.0 // convert to Celsius
        }

        if (rawTemp < 20.0 || rawTemp > 45.0) return null

        return TemperatureReading(
            timestampEpochMs = System.currentTimeMillis(),
            temperatureCelsius = Math.round(rawTemp * 10.0) / 10.0,
            baselineDeltaCelsius = Math.round((rawTemp - 36.6) * 10.0) / 10.0,
            source = DataSource.PEBBLE_QORE_2_BLE
        )
    }

    /**
     * Decodes Pebble Vendor Telemetry Live Frame.
     * Packet structure:
     * [0]: OpCode (0x10)
     * [1..4]: Steps (uint32 LE)
     * [5..6]: Active Calories (uint16 LE)
     * [7..8]: Distance in meters (uint16 LE)
     * [9]: Live Heart Rate (uint8)
     * [10]: SpO2 % (uint8)
     * [11..12]: HRV rMSSD ms (uint16 LE)
     * [13]: Stress Score (0..100)
     * [14..15]: Skin Temp in deci-Celsius (uint16 LE, e.g. 366 = 36.6°C)
     * [16]: Battery % (uint8)
     * [17]: Checksum (XOR of bytes 0..16)
     */
    data class DecodedVendorLiveTelemetry(
        val steps: Int,
        val activeCalories: Int,
        val distanceMeters: Double,
        val heartRate: Int,
        val spo2: Int,
        val hrv: Int,
        val stress: Int,
        val skinTempCelsius: Double,
        val batteryPercent: Int
    )

    fun decodeVendorLiveTelemetry(data: ByteArray): DecodedVendorLiveTelemetry? {
        if (data.size < 18) return null
        if (data[0] != Qore2Protocol.OPCODE_LIVE_METRICS_REPORT) return null

        // Checksum verification
        var checksum: Byte = 0
        for (i in 0 until 17) {
            checksum = (checksum.toInt() xor data[i].toInt()).toByte()
        }
        if (checksum != data[17]) return null

        val buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)
        val steps = buffer.getInt(1)
        val calories = buffer.getShort(5).toInt() and 0xFFFF
        val distance = (buffer.getShort(7).toInt() and 0xFFFF).toDouble()
        val hr = buffer.get(9).toInt() and 0xFF
        val spo2 = buffer.get(10).toInt() and 0xFF
        val hrv = buffer.getShort(11).toInt() and 0xFFFF
        val stress = buffer.get(13).toInt() and 0xFF
        val tempRaw = buffer.getShort(14).toInt() and 0xFFFF
        val tempCelsius = tempRaw / 10.0
        val battery = buffer.get(16).toInt() and 0xFF

        return DecodedVendorLiveTelemetry(
            steps = steps.coerceAtLeast(0),
            activeCalories = calories,
            distanceMeters = distance,
            heartRate = hr.coerceIn(30, 240),
            spo2 = spo2.coerceIn(70, 100),
            hrv = hrv.coerceIn(5, 300),
            stress = stress.coerceIn(0, 100),
            skinTempCelsius = if (tempCelsius in 25.0..45.0) tempCelsius else 36.6,
            batteryPercent = battery.coerceIn(0, 100)
        )
    }

    /**
     * Decodes Pebble Vendor Sleep Packet.
     */
    fun decodeSleepSession(data: ByteArray): SleepSession? {
        if (data.size < 16) return null
        val buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)
        val startEpoch = buffer.getLong(0)
        val endEpoch = buffer.getLong(8)
        val durationMinutes = ((endEpoch - startEpoch) / 60000).toInt().coerceAtLeast(0)

        // Estimated/calculated stages if granular stages not appended
        val deep = (durationMinutes * 0.22).toInt()
        val rem = (durationMinutes * 0.24).toInt()
        val awake = (durationMinutes * 0.08).toInt()
        val light = durationMinutes - deep - rem - awake

        val score = (((deep * 1.5 + rem * 1.2 + light * 0.8) / durationMinutes.coerceAtLeast(1)) * 90).toInt().coerceIn(40, 99)

        return SleepSession(
            id = "sleep_$startEpoch",
            startTimeEpochMs = startEpoch,
            endTimeEpochMs = endEpoch,
            durationMinutes = durationMinutes,
            deepMinutes = deep,
            lightMinutes = light.coerceAtLeast(0),
            remMinutes = rem,
            awakeMinutes = awake,
            sleepScore = score,
            stages = listOf(
                SleepStageRecord(SleepStageType.AWAKE, startEpoch, startEpoch + (awake * 60000 / 2)),
                SleepStageRecord(SleepStageType.LIGHT, startEpoch + (awake * 60000 / 2), startEpoch + (durationMinutes * 60000 / 3)),
                SleepStageRecord(SleepStageType.DEEP, startEpoch + (durationMinutes * 60000 / 3), startEpoch + (durationMinutes * 60000 * 2 / 3)),
                SleepStageRecord(SleepStageType.REM, startEpoch + (durationMinutes * 60000 * 2 / 3), endEpoch)
            ),
            source = DataSource.PEBBLE_QORE_2_BLE
        )
    }
}
