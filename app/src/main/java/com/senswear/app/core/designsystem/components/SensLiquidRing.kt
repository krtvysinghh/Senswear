package com.senswear.app.core.designsystem.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.senswear.app.core.designsystem.theme.SensCyan
import com.senswear.app.core.designsystem.theme.SensEmerald
import com.senswear.app.core.designsystem.theme.SensRose
import kotlin.math.cos
import kotlin.math.sin

/**
 * Apple Fitness-style Multi-Layered Liquid Progress Ring.
 * Supports single or concentric triple rings (Move, Exercise, Stand/Steps).
 */
@Composable
fun SensLiquidProgressRing(
    progress: Float, // 0.0f .. 1.0f+
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    strokeWidth: Dp = 14.dp,
    startColor: Color = Color(0xFF00F0FF),
    endColor: Color = Color(0xFF00E676),
    content: @Composable (() -> Unit)? = null
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceAtLeast(0f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "ring_progress"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val sWidth = strokeWidth.toPx()
            val arcSize = Size(this.size.width - sWidth, this.size.height - sWidth)
            val topLeft = Offset(sWidth / 2f, sWidth / 2f)

            // Background track
            drawArc(
                color = startColor.copy(alpha = 0.12f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = sWidth, cap = StrokeCap.Round)
            )

            // Ambient outer glow on active progress
            if (animatedProgress > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(startColor.copy(alpha = 0.4f), endColor.copy(alpha = 0.4f))
                    ),
                    startAngle = -90f,
                    sweepAngle = (animatedProgress * 360f).coerceAtMost(360f),
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = sWidth + 6.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Foreground liquid sweep gradient
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(startColor, endColor, startColor)
                ),
                startAngle = -90f,
                sweepAngle = (animatedProgress * 360f),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = sWidth, cap = StrokeCap.Round)
            )
        }

        if (content != null) {
            content()
        }
    }
}
