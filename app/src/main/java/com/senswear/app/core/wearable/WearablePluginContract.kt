package com.senswear.app.core.wearable

import kotlinx.coroutines.flow.Flow

/**
 * Public plugin SPI (Service Provider Interface) allowing third-party hardware manufacturers
 * to create external WearableAdapter plugins for Senswear.
 */
interface WearablePluginContract {
    val pluginId: String
    val pluginVersion: String
    val supportedDeviceBrand: String

    fun createAdapter(deviceAddress: String): WearableAdapter
    fun validateDeviceCompatibility(deviceAddress: String): Boolean
}
