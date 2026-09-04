package com.senswear.app.core.wearable.cloud

import com.senswear.app.core.domain.model.DailyActivity
import com.senswear.app.core.domain.model.DataSource
import com.senswear.app.core.domain.model.HeartRateReading
import com.senswear.app.core.domain.model.StressReading
import com.senswear.app.core.wearable.DataProvenance
import com.senswear.app.core.wearable.DataQuality
import com.senswear.app.core.wearable.WearableProtocol
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

data class GarminNormalizedSyncResult(
    val activities: List<DailyActivity>,
    val restingHeartRates: List<HeartRateReading>,
    val stressReadings: List<StressReading>,
    val provenanceRecords: List<DataProvenance>
)

object GarminConnectSyncPlugin {

    const val AUTH_ENDPOINT = "https://connect.garmin.com/oauthConfirm"
    const val TOKEN_ENDPOINT = "https://connectapi.garmin.com/oauth-service/oauth/token"
    const val HEALTH_API_BASE_URL = "https://healthapi.garmin.com/wellness-api/rest"

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Parses Garmin Daily Summaries JSON Array.
     */
    fun parseDailiesPayload(jsonArrayString: String): GarminNormalizedSyncResult {
        val activities = mutableListOf<DailyActivity>()
        val heartRates = mutableListOf<HeartRateReading>()
        val stressList = mutableListOf<StressReading>()
        val provenanceList = mutableListOf<DataProvenance>()

        val array = json.parseToJsonElement(jsonArrayString).jsonArray
        for ((i, element) in array.withIndex()) {
            val item = element.jsonObject
            val summaryId = item["summaryId"]?.jsonPrimitive?.content ?: "garmin_summary_$i"
            val startTimeGmt = item["startTimeGMT"]?.jsonPrimitive?.content
            val epochDay = try {
                if (startTimeGmt != null) {
                    val instant = java.time.Instant.parse(startTimeGmt)
                    instant.toEpochMilli() / (1000 * 60 * 60 * 24)
                } else {
                    System.currentTimeMillis() / (1000 * 60 * 60 * 24)
                }
            } catch (e: Exception) {
                System.currentTimeMillis() / (1000 * 60 * 60 * 24)
            }
            val timeEpochMs = epochDay * 86400000L

            val steps = item["steps"]?.jsonPrimitive?.intOrNull ?: 0
            val stepGoal = item["stepGoal"]?.jsonPrimitive?.intOrNull ?: 10000
            val distanceMeters = item["distanceInMeters"]?.jsonPrimitive?.doubleOrNull ?: 0.0
            val activeCalories = item["activeKilocalories"]?.jsonPrimitive?.intOrNull ?: 0
            val totalCalories = item["totalKilocalories"]?.jsonPrimitive?.intOrNull ?: 2000
            val moderate = item["moderateIntensityDurationInSeconds"]?.jsonPrimitive?.intOrNull ?: 0
            val vigorous = item["vigorousIntensityDurationInSeconds"]?.jsonPrimitive?.intOrNull ?: 0
            val activeMinutes = (moderate + vigorous) / 60

            val activity = DailyActivity(
                epochDay = epochDay,
                steps = steps,
                stepGoal = stepGoal,
                distanceMeters = distanceMeters,
                activeCaloriesKcal = activeCalories,
                totalCaloriesKcal = totalCalories,
                activeMinutes = activeMinutes,
                source = DataSource.HEALTH_CONNECT
            )
            activities.add(activity)

            provenanceList.add(
                DataProvenance(
                    metricName = "daily_steps",
                    canonicalValue = steps.toDouble(),
                    canonicalUnit = "steps",
                    timestampEpochMs = timeEpochMs,
                    sourceDeviceName = "Garmin Watch",
                    sourceDeviceId = summaryId,
                    sourceVendor = "Garmin",
                    sourceProtocol = WearableProtocol.VENDOR_CLOUD_API,
                    dataQuality = DataQuality.EXCELLENT
                )
            )

            val rhr = item["restingHeartRateInBeatsPerMinute"]?.jsonPrimitive?.intOrNull
            if (rhr != null && rhr in 30..220) {
                heartRates.add(
                    HeartRateReading(
                        timestampEpochMs = timeEpochMs,
                        bpm = rhr,
                        restingHeartRateBpm = rhr,
                        source = DataSource.HEALTH_CONNECT
                    )
                )
            }

            val avgStress = item["averageStressLevel"]?.jsonPrimitive?.intOrNull
            if (avgStress != null && avgStress in 0..100) {
                stressList.add(
                    StressReading(
                        timestampEpochMs = timeEpochMs,
                        score = avgStress,
                        source = DataSource.HEALTH_CONNECT
                    )
                )
            }
        }

        return GarminNormalizedSyncResult(
            activities = activities,
            restingHeartRates = heartRates,
            stressReadings = stressList,
            provenanceRecords = provenanceList
        )
    }
}
