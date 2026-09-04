package com.senswear.app.core.wearable.cloud

import com.senswear.app.core.domain.model.WearableBrand

data class CloudSyncCredential(
    val vendor: WearableBrand,
    val clientId: String,
    val accessToken: String,
    val refreshToken: String? = null,
    val tokenType: String = "Bearer",
    val expiresAtEpochMs: Long = System.currentTimeMillis() + (3600 * 1000L),
    val scopes: List<String> = emptyList()
) {
    val isExpired: Boolean
        get() = System.currentTimeMillis() >= expiresAtEpochMs - 60000L
}
