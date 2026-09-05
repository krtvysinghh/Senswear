package com.senswear.app.core.domain.model

import kotlin.math.sqrt

/**
 * 2D Kalman filter that removes GPS multipath drift and jitter from outdoor workout pace and location.
 */
class KalmanGpsFilter(
    private val processNoiseSigmaMeters: Double = 3.0
) {
    private var variance = -1.0
    private var lat = 0.0
    private var lng = 0.0
    private var timestampMs = 0L

    fun filter(
        newLat: Double,
        newLng: Double,
        accuracyMeters: Double,
        newTimestampMs: Long
    ): Pair<Double, Double> {
        val accuracy = accuracyMeters.coerceAtLeast(1.0)

        if (variance < 0.0) {
            lat = newLat
            lng = newLng
            variance = accuracy * accuracy
            timestampMs = newTimestampMs
            return Pair(lat, lng)
        }

        val dt = (newTimestampMs - timestampMs) / 1000.0
        timestampMs = newTimestampMs

        if (dt > 0.0) {
            variance += dt * processNoiseSigmaMeters * processNoiseSigmaMeters
        }

        val kalmanGain = variance / (variance + accuracy * accuracy)
        lat += kalmanGain * (newLat - lat)
        lng += kalmanGain * (newLng - lng)
        variance *= (1.0 - kalmanGain)

        return Pair(lat, lng)
    }

    fun reset() {
        variance = -1.0
        lat = 0.0
        lng = 0.0
        timestampMs = 0L
    }
}
