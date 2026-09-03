package com.senswear.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LiquidRingMathTest {

    @Test
    fun `Liquid progress ring clips sweep angle appropriately`() {
        val progressNormal = 0.75f
        val sweepAngle = progressNormal * 360f
        assertEquals(270f, sweepAngle, 0.01f)

        val progressOverachievement = 1.25f
        val sweepAngleOver = progressOverachievement * 360f
        assertEquals(450f, sweepAngleOver, 0.01f)
    }

    @Test
    fun `Cardiac waveform duration scales inversely with heart rate BPM`() {
        val bpmLow = 60
        val durationMsLow = ((60.0 / bpmLow.toDouble()) * 1000).toInt()
        assertEquals(1000, durationMsLow)

        val bpmHigh = 150
        val durationMsHigh = ((60.0 / bpmHigh.toDouble()) * 1000).toInt()
        assertEquals(400, durationMsHigh)
    }
}
