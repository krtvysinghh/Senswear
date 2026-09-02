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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(dataPoints) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(800))
    }

    Canvas(modifier = modifier) {
        if (dataPoints.size < 2) return@Canvas

        val w = size.width
        val h = size.height
        val effectiveRange = (maxVal - minVal).coerceAtLeast(1f)
        val stepX = w / (dataPoints.size - 1)

        val points = dataPoints.mapIndexed { index, value ->
            val normY = (value - minVal) / effectiveRange
            val x = index * stepX
            val y = h - (normY * h * animProgress.value)
            Offset(x, y.coerceIn(0f, h))
        }

        val path = Path()
        val fillPath = Path()

        path.moveTo(points.first().x, points.first().y)
        fillPath.moveTo(points.first().x, h)
        fillPath.lineTo(points.first().x, points.first().y)

        for (i in 0 until points.size - 1) {
            val p0 = points[i]
            val p1 = points[i + 1]
            val controlPoint1 = Offset((p0.x + p1.x) / 2f, p0.y)
            val controlPoint2 = Offset((p0.x + p1.x) / 2f, p1.y)
            path.cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, p1.x, p1.y)
            fillPath.cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, p1.x, p1.y)
        }

        fillPath.lineTo(points.last().x, h)
        fillPath.close()

        drawPath(fillPath, fillBrush)
        drawPath(path, lineColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))

        // Draw last endpoint circle highlight
        val lastPoint = points.last()
        drawCircle(Color.White, radius = 5.dp.toPx(), center = lastPoint)
        drawCircle(lineColor, radius = 3.dp.toPx(), center = lastPoint)
    }
}

@Composable
fun SensHourlyBarChart(
    hourlyValues: List<Int>,
    modifier: Modifier = Modifier,
    barColor: Color = SensCyan
) {
    val maxVal = (hourlyValues.maxOrNull() ?: 1).coerceAtLeast(100)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        hourlyValues.forEachIndexed { hour, value ->
            val heightFraction = (value.toFloat() / maxVal.toFloat()).coerceIn(0.04f, 1f)
            val isPeak = value == maxVal && value > 0

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f).padding(horizontal = 1.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(heightFraction)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(
                            if (isPeak) Brush.verticalGradient(listOf(SensCyan, SensEmerald))
                            else Brush.verticalGradient(listOf(barColor.copy(alpha = 0.8f), barColor.copy(alpha = 0.3f)))
                        )
                )
            }
        }
    }
}

@Composable
fun SensSleepHypnogram(
    stages: List<SleepStageRecord>,
    modifier: Modifier = Modifier
) {
    if (stages.isEmpty()) return

    val totalDurationMs = (stages.last().endTimeEpochMs - stages.first().startTimeEpochMs).coerceAtLeast(1)

    Column(modifier = modifier) {
        // Stage Legend
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LegendItem("Deep", SensIndigo)
            LegendItem("Light", SensCyan)
            LegendItem("REM", SensViolet)
            LegendItem("Awake", SensAmber)
        }

        // Timeline visualization bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0x14FFFFFF))
        ) {
            stages.forEach { record ->
                val durationMs = record.endTimeEpochMs - record.startTimeEpochMs
                val weight = (durationMs.toFloat() / totalDurationMs.toFloat()).coerceAtLeast(0.01f)
                val color = when (record.stage) {
                    SleepStageType.DEEP -> SensIndigo
                    SleepStageType.LIGHT -> SensCyan
                    SleepStageType.REM -> SensViolet
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

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(8.dp)
                .height(8.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, style = SensTypography.labelSmall, color = SensTextSecondary)
    }
}

@Composable
fun SensHeartRateZonesChart(
    zoneMinutes: List<Int>, // 5 zones
    modifier: Modifier = Modifier
) {
    val totalMinutes = zoneMinutes.sum().coerceAtLeast(1)
    val colors = listOf(SensCyan, SensEmerald, SensAmber, SensRose, SensViolet)
    val zoneNames = listOf("Zone 1 Warm Up", "Zone 2 Fat Burn", "Zone 3 Cardio", "Zone 4 Threshold", "Zone 5 Max")

    Column(modifier = modifier) {
        // Multi-segment horizontal bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(Color(0x14FFFFFF))
        ) {
            zoneMinutes.forEachIndexed { i, mins ->
                val weight = (mins.toFloat() / totalMinutes.toFloat()).coerceAtLeast(0.01f)
                Box(
                    modifier = Modifier
                        .weight(weight)
                        .fillMaxHeight()
                        .background(colors[i % colors.size])
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Breakdown items
        zoneMinutes.forEachIndexed { i, mins ->
            val pct = ((mins.toFloat() / totalMinutes.toFloat()) * 100).toInt()
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(8.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(colors[i % colors.size])
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = zoneNames[i], style = SensTypography.bodyMedium, color = SensTextPrimary)
                }
                Text(text = "${mins}m ($pct%)", style = SensTypography.bodyMedium, color = SensTextSecondary)
            }
        }
    }
}
