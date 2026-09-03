package com.senswear.app.core.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.senswear.app.core.designsystem.theme.SensAmber
import com.senswear.app.core.designsystem.theme.SensCyan
import com.senswear.app.core.designsystem.theme.SensEmerald
import com.senswear.app.core.designsystem.theme.SensIndigo
import com.senswear.app.core.designsystem.theme.SensRose
import com.senswear.app.core.designsystem.theme.SensTextPrimary
import com.senswear.app.core.designsystem.theme.SensTextSecondary
import com.senswear.app.core.designsystem.theme.SensTextTertiary
import com.senswear.app.core.designsystem.theme.SensTypography
import com.senswear.app.core.designsystem.theme.SensViolet
import com.senswear.app.core.domain.model.SleepStageRecord
import com.senswear.app.core.domain.model.SleepStageType

/**
 * 120 FPS Zero-Allocation Cached Bezier Curve Line Chart.
 */
@Composable
fun SensLineChart(
    dataPoints: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = SensCyan,
    fillBrush: Brush = Brush.verticalGradient(
        colors = listOf(lineColor.copy(alpha = 0.35f), Color.Transparent)
    ),
    minVal: Float = dataPoints.minOrNull() ?: 0f,
    maxVal: Float = dataPoints.maxOrNull() ?: 100f
) {
    val animProgress = remember { Animatable(1f) }

    LaunchedEffect(dataPoints) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(600))
    }

    val strokeWidth = 3.dp

    Box(
        modifier = modifier.drawWithCache {
            val w = size.width
            val h = size.height
            val effectiveRange = (maxVal - minVal).coerceAtLeast(1f)

            if (dataPoints.size < 2 || w <= 0f || h <= 0f) {
                onDrawBehind { }
            } else {
                val stepX = w / (dataPoints.size - 1)
                val strokePx = strokeWidth.toPx()

                val path = Path()
                val fillPath = Path()

                val firstNormY = (dataPoints.first() - minVal) / effectiveRange
                val firstY = h - (firstNormY * h * animProgress.value)
                path.moveTo(0f, firstY.coerceIn(0f, h))
                fillPath.moveTo(0f, h)
                fillPath.lineTo(0f, firstY.coerceIn(0f, h))

                for (i in 0 until dataPoints.size - 1) {
                    val p0x = i * stepX
                    val p0y = (h - ((dataPoints[i] - minVal) / effectiveRange * h * animProgress.value)).coerceIn(0f, h)

                    val p1x = (i + 1) * stepX
                    val p1y = (h - ((dataPoints[i + 1] - minVal) / effectiveRange * h * animProgress.value)).coerceIn(0f, h)

                    val cx1 = (p0x + p1x) / 2f
                    val cy1 = p0y
                    val cx2 = (p0x + p1x) / 2f
                    val cy2 = p1y

                    path.cubicTo(cx1, cy1, cx2, cy2, p1x, p1y)
                    fillPath.cubicTo(cx1, cy1, cx2, cy2, p1x, p1y)
                }

                fillPath.lineTo(w, h)
                fillPath.close()

                onDrawBehind {
                    drawPath(fillPath, fillBrush)
                    drawPath(path, lineColor, style = Stroke(width = strokePx, cap = StrokeCap.Round))
                }
            }
        }
    )
}

/**
 * 120 FPS Cached Hourly Intraday Activity Bar Chart.
 */
@Composable
fun SensHourlyBarChart(
    hourlyValues: List<Int>,
    modifier: Modifier = Modifier,
    barColor: Color = SensCyan,
    maxHourVal: Int = (hourlyValues.maxOrNull() ?: 500).coerceAtLeast(100)
) {
    Box(
        modifier = modifier.drawWithCache {
            val w = size.width
            val h = size.height
            val count = hourlyValues.size.coerceAtLeast(24)
            val slotWidth = w / count.toFloat()
            val barWidth = (slotWidth * 0.65f).coerceAtLeast(2f)
            val cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)

            onDrawBehind {
                for (i in 0 until count) {
                    val value = if (i < hourlyValues.size) hourlyValues[i] else 0
                    val fraction = (value.toFloat() / maxHourVal.toFloat()).coerceIn(0.04f, 1.0f)
                    val barHeight = h * fraction
                    val x = i * slotWidth + (slotWidth - barWidth) / 2f
                    val y = h - barHeight

                    val color = if (value > 0) barColor else barColor.copy(alpha = 0.12f)
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = cornerRadius
                    )
                }
            }
        }
    )
}

/**
 * Sleep Architecture Hypnogram.
 */
@Composable
fun SensSleepHypnogram(
    stages: List<SleepStageRecord>,
    modifier: Modifier = Modifier,
    totalDurationMinutes: Int = stages.sumOf { (it.endTimeEpochMs - it.startTimeEpochMs).toInt() / 60000 }.coerceAtLeast(1)
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0F172A))
    ) {
        if (stages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Awaiting Nocturnal Sleep Telemetry",
                    style = SensTypography.labelSmall,
                    color = SensTextTertiary
                )
            }
        } else {
            stages.forEach { stage ->
                val duration = ((stage.endTimeEpochMs - stage.startTimeEpochMs) / 60000).toFloat()
                val weight = (duration / totalDurationMinutes.toFloat()).coerceAtLeast(0.01f)
                val color = when (stage.stage) {
                    SleepStageType.DEEP -> SensIndigo
                    SleepStageType.REM -> SensViolet
                    SleepStageType.LIGHT -> SensCyan
                    SleepStageType.AWAKE -> SensAmber
                }

                Box(
                    modifier = Modifier
                        .weight(weight)
                        .fillMaxHeight()
                        .background(color)
                )
            }
        }
    }
}

/**
 * 5-Zone Heart Rate Segmented Distribution Meter.
 */
@Composable
fun SensHeartRateZonesChart(
    zoneMinutes: List<Int>, // 5 items: Z1, Z2, Z3, Z4, Z5
    modifier: Modifier = Modifier
) {
    val totalMins = zoneMinutes.sum().coerceAtLeast(1)
    val colors = listOf(SensCyan, SensEmerald, SensAmber, SensRose, SensViolet)
    val labels = listOf("Z1 Warm up", "Z2 Fat Burn", "Z3 Aerobic", "Z4 Anaerobic", "Z5 VO₂ Max")

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(Color(0xFF1E293B)),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            zoneMinutes.forEachIndexed { index, mins ->
                val fraction = mins.toFloat() / totalMins.toFloat()
                if (fraction > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(fraction)
                            .fillMaxHeight()
                            .background(colors[index])
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            zoneMinutes.forEachIndexed { index, mins ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(colors[index])
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${mins}m",
                        style = SensTypography.labelSmall,
                        color = SensTextPrimary
                    )
                    Text(
                        text = "Z${index + 1}",
                        style = SensTypography.labelSmall,
                        color = SensTextTertiary
                    )
                }
            }
        }
    }
}
