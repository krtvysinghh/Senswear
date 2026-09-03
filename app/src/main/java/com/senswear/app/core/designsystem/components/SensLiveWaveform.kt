package com.senswear.app.core.designsystem.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.senswear.app.core.designsystem.theme.SensRose
import kotlin.math.sin

/**
 * High-performance 60fps Real-Time ECG/PPG Waveform Ticker Canvas.
 * Generates continuous cardiac P-Q-R-S-T wave oscillations synchronized
 * to live heart rate BPM.
 */
@Composable
fun SensLiveWaveform(
    bpm: Int,
    modifier: Modifier = Modifier,
    height: Dp = 60.dp,
    lineColor: Color = SensRose,
    isLive: Boolean = true
) {
    val durationMs = if (bpm > 0) ((60.0 / bpm.toDouble()) * 1000).toInt().coerceIn(400, 1500) else 1000

    val infiniteTransition = rememberInfiniteTransition(label = "ecg_wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase_anim"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val w = size.width
        val h = size.height
        val midY = h / 2f

        val path = Path()
        val numSamples = 120
        var isFirst = true

        for (i in 0..numSamples) {
            val progress = i.toFloat() / numSamples.toFloat()
            val x = progress * w
            val wavePhase = (progress - phase + 1f) % 1f

            // Generate physiological P-Q-R-S-T cardiac wave peak
            val yOffset = when {
                wavePhase in 0.35f..0.38f -> -h * 0.12f // P wave
                wavePhase in 0.44f..0.46f -> h * 0.08f  // Q drop
                wavePhase in 0.46f..0.50f -> -h * 0.45f // R peak (Main spike)
                wavePhase in 0.50f..0.53f -> h * 0.18f  // S valley
                wavePhase in 0.60f..0.66f -> -h * 0.18f // T wave
                else -> sin(progress * 12f) * (h * 0.02f) // Baseline micro-fluctuation
            }

            val y = midY + (if (isLive && bpm > 0) yOffset else 0f)

            if (isFirst) {
                path.moveTo(x, y)
                isFirst = false
            } else {
                path.lineTo(x, y)
            }
        }

        // Draw soft glow underlay
        drawPath(
            path = path,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    lineColor.copy(alpha = 0.05f),
                    lineColor.copy(alpha = 0.35f),
                    lineColor.copy(alpha = 0.9f),
                    lineColor.copy(alpha = 0.1f)
                )
            ),
            style = Stroke(
                width = 5.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Draw sharp specular foreground line
        drawPath(
            path = path,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.2f),
                    Color.White.copy(alpha = 0.8f),
                    lineColor,
                    Color.White.copy(alpha = 0.2f)
                )
            ),
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Draw active pulse cursor dot
        val cursorX = ((phase * w) % w)
        val cursorWavePhase = (phase - phase + 1f) % 1f
        drawCircle(
            color = Color.White,
            radius = 3.5.dp.toPx(),
            center = Offset(cursorX, midY)
        )
        drawCircle(
            color = lineColor.copy(alpha = 0.4f),
            radius = 7.dp.toPx(),
            center = Offset(cursorX, midY)
        )
    }
}
