package com.senswear.app.core.wearable.cloud

import com.senswear.app.core.domain.model.DataSource
import com.senswear.app.core.domain.model.HeartRateReading
import com.senswear.app.core.domain.model.HrvReading
import com.senswear.app.core.domain.model.SleepSession
import com.senswear.app.core.domain.model.Spo2Reading
import com.senswear.app.core.domain.model.TemperatureReading
import com.senswear.app.core.wearable.DataProvenance
import com.senswear.app.core.wearable.DataQuality
import com.senswear.app.core.wearable.WearableProtocol
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

data class WhoopNormalizedSyncResult(
    val heartRates: List<HeartRateReading>,
    val hrvs: List<HrvReading>,
    val spo2s: List<Spo2Reading>,
    val temperatures: List<TemperatureReading>,
    val sleepSessions: List<SleepSession>,
    val provenanceRecords: List<DataProvenance>
)

object WhoopCloudSyncPlugin {

    const val AUTH_ENDPOINT = "https://api.prod.whoop.com/oauth/oauth2/auth"
    const val TOKEN_ENDPOINT = "https://api.prod.whoop.com/oauth/oauth2/token"
    const val API_BASE_URL = "https://api.prod.whoop.com/developer/v1"

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Parses Whoop Recovery API response (JSON Object containing records array).
     */
    fun parseRecoveryPayload(jsonString: String): WhoopNormalizedSyncResult {
        val hrList = mutableListOf<HeartRateReading>()
        val hrvList = mutableListOf<HrvReading>()
        val spo2List = mutableListOf<Spo2Reading>()
        val tempList = mutableListOf<TemperatureReading>()
        val sleepList = mutableListOf<SleepSession>()
        val provenanceList = mutableListOf<DataProvenance>()

        val root = json.parseToJsonElement(jsonString).jsonObject
        val records = root["records"]?.jsonArray ?: kotlinx.serialization.json.buildJsonArray { }

        for ((i, element) in records.withIndex()) {
            val item = element.jsonObject
            val scoreObj = item["score"]?.jsonObject ?: continue
            val cycleId = item["cycle_id"]?.jsonPrimitive?.content ?: "whoop_cycle_$i"
            val updatedAtStr = item["updated_at"]?.jsonPrimitive?.content
            val timeEpochMs = try {
                if (updatedAtStr != null) java.time.Instant.parse(updatedAtStr).toEpochMilli() else System.currentTimeMillis()
            } catch (e: Exception) {
                System.currentTimeMillis()
            }

            // Resting Heart Rate
            val rhr = scoreObj["resting_heart_rate"]?.jsonPrimitive?.intOrNull
            if (rhr != null && rhr in 30..220) {
                val hr = HeartRateReading(
                    timestampEpochMs = timeEpochMs,
                    bpm = rhr,
                    restingHeartRateBpm = rhr,
                    source = DataSource.PEBBLE_QORE_2_BLE
                )
                hrList.add(hr)
                provenanceList.add(
                    DataProvenance(
                        metricName = "resting_heart_rate",
                        canonicalValue = rhr.toDouble(),
                        canonicalUnit = "bpm",
                        timestampEpochMs = timeEpochMs,
                        sourceDeviceName = "Whoop 4.0",
                        sourceDeviceId = cycleId,
                        sourceVendor = "Whoop",
                        sourceProtocol = WearableProtocol.VENDOR_CLOUD_API,
                        dataQuality = DataQuality.EXCELLENT
                    )
                )
            }

            // HRV rMSSD
            val hrvRmssd = scoreObj["hrv_rmssd_milli"]?.jsonPrimitive?.doubleOrNull
            if (hrvRmssd != null && hrvRmssd > 0.0) {
                val roundedHrv = hrvRmssd.toInt()
                hrvList.add(
                    HrvReading(
                        timestampEpochMs = timeEpochMs,
                        rmssdMs = roundedHrv,
                        sdnnMs = null,
                        source = DataSource.PEBBLE_QORE_2_BLE
                    )
                )
                provenanceList.add(
                    DataProvenance(
                        metricName = "hrv_rmssd",
                        canonicalValue = hrvRmssd,
                        canonicalUnit = "ms",
                        timestampEpochMs = timeEpochMs,
                        sourceDeviceName = "Whoop 4.0",
                        sourceDeviceId = cycleId,
                        sourceVendor = "Whoop",
                        sourceProtocol = WearableProtocol.VENDOR_CLOUD_API,
                        dataQuality = DataQuality.EXCELLENT
                    )
                )
            }

            // SpO2 Percentage
            val spo2 = scoreObj["spo2_percentage"]?.jsonPrimitive?.doubleOrNull
            if (spo2 != null && spo2 in 70.0..100.0) {
                val roundedSpo2 = spo2.toInt()
                spo2List.add(Spo2Reading(timestampEpochMs = timeEpochMs, percentage = roundedSpo2))
                provenanceList.add(
                    DataProvenance(
                        metricName = "spo2",
                        canonicalValue = spo2,
                        canonicalUnit = "%",
                        timestampEpochMs = timeEpochMs,
                        sourceDeviceName = "Whoop 4.0",
                        sourceDeviceId = cycleId,
                        sourceVendor = "Whoop",
                        sourceProtocol = WearableProtocol.VENDOR_CLOUD_API,
                        dataQuality = DataQuality.EXCELLENT
                    )
                )
            }

            // Skin Temp Celsius
            val skinTemp = scoreObj["skin_temp_celsius"]?.jsonPrimitive?.doubleOrNull
            if (skinTemp != null && skinTemp in 20.0..45.0) {
                tempList.add(
                    TemperatureReading(
                        timestampEpochMs = timeEpochMs,
                        temperatureCelsius = skinTemp,
                        baselineDeltaCelsius = 0.0
                    )
                )
                provenanceList.add(
                    DataProvenance(
                        metricName = "skin_temperature",
                        canonicalValue = skinTemp,
                        canonicalUnit = "celsius",
                        timestampEpochMs = timeEpochMs,
                        sourceDeviceName = "Whoop 4.0",
                        sourceDeviceId = cycleId,
                        sourceVendor = "Whoop",
                        sourceProtocol = WearableProtocol.VENDOR_CLOUD_API,
                        dataQuality = DataQuality.EXCELLENT
                    )
                )
            }
        }

        return WhoopNormalizedSyncResult(
            heartRates = hrList,
            hrvs = hrvList,
            spo2s = spo2List,
            temperatures = tempList,
            sleepSessions = sleepList,
            provenanceRecords = provenanceList
        )
    }
}
