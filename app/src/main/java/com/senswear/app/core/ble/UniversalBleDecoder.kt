package com.senswear.app.core.ble

import com.senswear.app.core.domain.model.DataSource
import com.senswear.app.core.domain.model.HeartRateReading
import com.senswear.app.core.domain.model.HrvReading
import com.senswear.app.core.domain.model.PhysiologicalDerivationEngine
import com.senswear.app.core.domain.model.TemperatureReading
import java.util.UUID
import kotlin.math.pow
import kotlin.math.sqrt

data class UniversalHrResult(
    val reading: HeartRateReading,
    val hrvReading: HrvReading?,
    val rrIntervalsMs: List<Int>
)

data class UniversalRscResult(
    val speedMetersPerSecond: Double,
    val cadenceSpm: Int,
    val strideLengthMeters: Double?,
    val totalDistanceMeters: Long?
)

object UniversalBleDecoder {

    val HR_SERVICE_UUID: UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
    val HR_MEASUREMENT_UUID: UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")

    val RSC_SERVICE_UUID: UUID = UUID.fromString("00001814-0000-1000-8000-00805f9b34fb")
    val RSC_MEASUREMENT_UUID: UUID = UUID.fromString("00002a53-0000-1000-8000-00805f9b34fb")

    val THERMO_SERVICE_UUID: UUID = UUID.fromString("00001809-0000-1000-8000-00805f9b34fb")
    val THERMO_MEASUREMENT_UUID: UUID = UUID.fromString("00002a1c-0000-1000-8000-00805f9b34fb")

    val BATTERY_SERVICE_UUID: UUID = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb")
    val BATTERY_LEVEL_UUID: UUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb")

    /**
     * Decodes standard Bluetooth SIG Heart Rate Measurement (0x2A37).
     * Extracts uint8/uint16 BPM and parses RR-interval values in 1/1024 seconds to compute HRV rMSSD.
     */
    fun decodeHeartRate(data: ByteArray, source: DataSource = DataSource.PEBBLE_QORE_2_BLE): UniversalHrResult? {
        if (data.isEmpty()) return null
        val flags = data[0].toInt()
        val is16Bit = (flags and 0x01) != 0
        val hasRrIntervals = (flags and 0x10) != 0

        var offset = 1
        val bpm: Int = if (is16Bit) {
            if (data.size < 3) return null
            val b1 = data[offset++].toInt() and 0xFF
            val b2 = data[offset++].toInt() and 0xFF
            (b2 shl 8) or b1
        } else {
            if (data.size < 2) return null
            data[offset++].toInt() and 0xFF
        }

        // Energy Expended Field (if present, bit 3)
        if ((flags and 0x08) != 0) {
            offset += 2
        }

        val rrIntervals = mutableListOf<Int>()
        if (hasRrIntervals && offset < data.size) {
            while (offset + 1 < data.size) {
                val b1 = data[offset++].toInt() and 0xFF
                val b2 = data[offset++].toInt() and 0xFF
                val rawRr = (b2 shl 8) or b1
                // Convert 1/1024 seconds to milliseconds
                val rrMs = ((rawRr.toDouble() / 1024.0) * 1000.0).toInt()
                if (rrMs in 300..2000) {
                    rrIntervals.add(rrMs)
                }
            }
        }

        val now = System.currentTimeMillis()
        val hrReading = HeartRateReading(
            timestampEpochMs = now,
            bpm = bpm,
            source = source
        )

        // Compute real-time HRV (rMSSD) if at least 2 RR intervals are present
        val hrvReading: HrvReading? = if (rrIntervals.size >= 2) {
            var diffSquareSum = 0.0
            for (i in 0 until rrIntervals.size - 1) {
                val diff = (rrIntervals[i + 1] - rrIntervals[i]).toDouble()
                diffSquareSum += diff * diff
            }
            val rmssd = sqrt(diffSquareSum / (rrIntervals.size - 1)).toInt()
            HrvReading(
                timestampEpochMs = now,
                rmssdMs = rmssd,
                source = source
            )
        } else null

        return UniversalHrResult(
            reading = hrReading,
            hrvReading = hrvReading,
            rrIntervalsMs = rrIntervals
        )
    }

    /**
     * Decodes Running Speed & Cadence (RSC 0x2A53).
     * Flag byte + Instantaneous Speed (uint16 in 1/256 m/s) + Cadence (uint8 in SPM).
     */
    fun decodeRunningSpeedCadence(data: ByteArray): UniversalRscResult? {
        if (data.size < 4) return null
        val flags = data[0].toInt()
        val isStridePresent = (flags and 0x01) != 0
        val isTotalDistancePresent = (flags and 0x02) != 0

        val speedRaw = (data[1].toInt() and 0xFF) or ((data[2].toInt() and 0xFF) shl 8)
        val speedMps = speedRaw.toDouble() / 256.0
        val cadence = data[3].toInt() and 0xFF

        var offset = 4
        var strideLength: Double? = null
        if (isStridePresent && offset + 1 < data.size) {
            val rawStride = (data[offset++].toInt() and 0xFF) or ((data[offset++].toInt() and 0xFF) shl 8)
            strideLength = rawStride.toDouble() / 100.0 // cm to meters
        }

        var totalDist: Long? = null
        if (isTotalDistancePresent && offset + 3 < data.size) {
            val b0 = data[offset++].toLong() and 0xFF
            val b1 = data[offset++].toLong() and 0xFF
            val b2 = data[offset++].toLong() and 0xFF
            val b3 = data[offset++].toLong() and 0xFF
            totalDist = (b0 or (b1 shl 8) or (b2 shl 16) or (b3 shl 24)) / 10L // decimeters to meters
        }

        return UniversalRscResult(
            speedMetersPerSecond = speedMps,
            cadenceSpm = cadence,
            strideLengthMeters = strideLength,
            totalDistanceMeters = totalDist
        )
    }

    /**
     * Decodes standard Bluetooth SIG Battery Level (0x2A19).
     */
    fun decodeBatteryLevel(data: ByteArray): Int? {
        if (data.isEmpty()) return null
        return (data[0].toInt() and 0xFF).coerceIn(0, 100)
    }

    /**
     * Decodes Health Thermometer (0x2A1C) IEEE 11073 32-bit FLOAT in Celsius.
     */
    fun decodeTemperature(data: ByteArray): TemperatureReading? {
        if (data.size < 5) return null
        val mantissa = (data[1].toInt() and 0xFF) or
                ((data[2].toInt() and 0xFF) shl 8) or
                ((data[3].toInt() and 0xFF) shl 16)

        var signedMantissa = mantissa
        if ((mantissa and 0x800000) != 0) {
            signedMantissa = mantissa or -0x1000000
        }

        val exponent = data[4].toInt() // signed byte
        val tempCelsius = signedMantissa.toDouble() * 10.0.pow(exponent.toDouble())
        if (tempCelsius !in 20.0..50.0) return null

        val rounded = Math.round(tempCelsius * 10.0) / 10.0
        return TemperatureReading(
            timestampEpochMs = System.currentTimeMillis(),
            temperatureCelsius = rounded,
            baselineDeltaCelsius = PhysiologicalDerivationEngine.deriveTemperatureDelta(rounded) ?: 0.0
        )
    }
}
