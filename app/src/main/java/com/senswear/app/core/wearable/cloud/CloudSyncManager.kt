package com.senswear.app.core.wearable.cloud

import com.senswear.app.core.data.repository.ActivityRepository
import com.senswear.app.core.data.repository.DataProvenanceRepository
import com.senswear.app.core.data.repository.HealthRepository
import com.senswear.app.core.domain.model.WearableBrand
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CloudSyncManager(
    private val healthRepository: HealthRepository,
    private val activityRepository: ActivityRepository,
    private val provenanceRepository: DataProvenanceRepository
) {
    private val credentials = mutableMapOf<WearableBrand, CloudSyncCredential>()

    private val _syncState = MutableStateFlow<Map<WearableBrand, Boolean>>(emptyMap())
    val syncState: StateFlow<Map<WearableBrand, Boolean>> = _syncState.asStateFlow()

    fun registerCredential(credential: CloudSyncCredential) {
        credentials[credential.vendor] = credential
    }

    fun isConfigured(vendor: WearableBrand): Boolean {
        return credentials[vendor]?.let { !it.isExpired } ?: false
    }

    suspend fun ingestWhoopRecovery(jsonPayload: String): Int {
        val result = WhoopCloudSyncPlugin.parseRecoveryPayload(jsonPayload)
        for (hr in result.heartRates) healthRepository.saveHeartRate(hr)
        for (hrv in result.hrvs) healthRepository.saveHrv(hrv)
        for (spo2 in result.spo2s) healthRepository.saveSpo2(spo2)
        for (temp in result.temperatures) healthRepository.saveTemperature(temp)
        for (prov in result.provenanceRecords) provenanceRepository.recordProvenance(prov)
        return result.provenanceRecords.size
    }

    suspend fun ingestGarminDailies(jsonArrayPayload: String): Int {
        val result = GarminConnectSyncPlugin.parseDailiesPayload(jsonArrayPayload)
        for (act in result.activities) activityRepository.saveDailyActivity(act)
        for (hr in result.restingHeartRates) healthRepository.saveHeartRate(hr)
        for (stress in result.stressReadings) healthRepository.saveStress(stress)
        for (prov in result.provenanceRecords) provenanceRepository.recordProvenance(prov)
        return result.provenanceRecords.size
    }
}
