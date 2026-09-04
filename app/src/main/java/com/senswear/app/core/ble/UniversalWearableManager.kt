package com.senswear.app.core.ble

import android.content.Context
import com.senswear.app.core.domain.model.ConnectionState
import com.senswear.app.core.domain.model.FitnessSnapshot
import com.senswear.app.core.domain.model.WearableBrand
import com.senswear.app.core.domain.model.WearableDevice
import com.senswear.app.core.healthconnect.HealthConnectManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UniversalWearableManager(
    private val context: Context,
    val bleConnector: UniversalBleConnector,
    val healthConnectManager: HealthConnectManager
) {
    val scanner = UniversalWearableScanner(context)

    private val _selectedBrand = MutableStateFlow<WearableBrand>(WearableBrand.PEBBLE_QORE_2)
    val selectedBrand: StateFlow<WearableBrand> = _selectedBrand.asStateFlow()

    fun selectBrand(brand: WearableBrand) {
        _selectedBrand.value = brand
    }

    suspend fun connectDevice(macAddress: String) {
        bleConnector.connect(macAddress)
    }

    suspend fun disconnectDevice() {
        bleConnector.disconnect()
    }
}
