package com.senswear.app.core.designsystem.components

/**
 * Dynamically adjusts UI chart and waveform animation frequency according to device display refresh rates (60Hz, 90Hz, 120Hz).
 */
class FramePacingManager {

    enum class TargetFps(val frameIntervalMs: Long) {
        FPS_60(16L),
        FPS_90(11L),
        FPS_120(8L)
    }

    fun determineTargetFps(refreshRateHz: Float): TargetFps {
        return when {
            refreshRateHz >= 115.0f -> TargetFps.FPS_120
            refreshRateHz >= 85.0f -> TargetFps.FPS_90
            else -> TargetFps.FPS_60
        }
    }
}
