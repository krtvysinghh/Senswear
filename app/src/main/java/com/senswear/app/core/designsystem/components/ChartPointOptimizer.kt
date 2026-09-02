package com.senswear.app.core.designsystem.components

import kotlin.math.max

object ChartPointOptimizer {
    fun downsampleLTTB(points: List<Float>, targetCount: Int): List<Float> {
        if (points.size <= targetCount || targetCount < 3) return points

        val sampled = mutableListOf<Float>()
        sampled.add(points.first())

        val bucketSize = (points.size - 2).toDouble() / (targetCount - 2).toDouble()
        var a = 0

        for (i in 0 until targetCount - 2) {
            val bucketStart = ((i + 1) * bucketSize).toInt() + 1
            val bucketEnd = minOf(((i + 2) * bucketSize).toInt() + 1, points.size)

            var avgVal = 0f
            var count = 0
            for (j in bucketStart until bucketEnd) {
                avgVal += points[j]
                count++
            }
            if (count > 0) avgVal /= count

            sampled.add(avgVal)
        }

        sampled.add(points.last())
        return sampled
    }
}
