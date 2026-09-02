package com.senswear.app.feature.home

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senswear.app.core.designsystem.components.SensGlassButton
import com.senswear.app.core.designsystem.components.SensGlassCard
import com.senswear.app.core.designsystem.components.SensGlassSurface
import com.senswear.app.core.designsystem.components.SensHourlyBarChart
import com.senswear.app.core.designsystem.components.SensMetricLarge
import com.senswear.app.core.designsystem.components.SensProgressRing
import com.senswear.app.core.designsystem.components.SensTopBar
import com.senswear.app.core.designsystem.theme.SensAmber
import com.senswear.app.core.designsystem.theme.SensCyan
import com.senswear.app.core.designsystem.theme.SensCyanGlow
import com.senswear.app.core.designsystem.theme.SensEmerald
import com.senswear.app.core.designsystem.theme.SensGlassBg
import com.senswear.app.core.designsystem.theme.SensGlassBorder
import com.senswear.app.core.designsystem.theme.SensIndigo
import com.senswear.app.core.designsystem.theme.SensObsidian
import com.senswear.app.core.designsystem.theme.SensRose
import com.senswear.app.core.designsystem.theme.SensTextPrimary
import com.senswear.app.core.designsystem.theme.SensTextSecondary
import com.senswear.app.core.designsystem.theme.SensTextTertiary
import com.senswear.app.core.designsystem.theme.SensTypography
import com.senswear.app.core.designsystem.theme.SensViolet
import com.senswear.app.core.domain.model.ConnectionState

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToActivity: () -> Unit,
    onNavigateToHealth: () -> Unit,
    onNavigateToSleep: () -> Unit,
    onNavigateToWorkouts: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val liveMetrics by viewModel.liveMetrics.collectAsState()

    val currentSteps = liveMetrics.steps.takeIf { it > 0 } ?: uiState.dailyActivity?.steps ?: 8421
    val stepGoal = uiState.dailyActivity?.stepGoal ?: 10000
    val progress = (currentSteps.toFloat() / stepGoal.toFloat()).coerceIn(0f, 1.5f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SensObsidian)
    ) {
        SensTopBar(
            title = "Senswear",
            subtitle = "Pebble Qore 2 Companion",
            connectionState = connectionState,
            actionIcon = Icons.Default.Refresh,
            onActionClick = { viewModel.syncNow() }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Hero Step Progress Ring & Live HR
            item {
                SensGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    accentGlow = SensCyan
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TODAY'S ACTIVITY",
                                style = SensTypography.labelSmall,
                                color = SensTextSecondary,
                                letterSpacing = 1.5.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "%,d".format(currentSteps),
                                style = SensTypography.displayMedium,
                                color = SensTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${(progress * 100).toInt()}% of $stepGoal step goal",
                                style = SensTypography.bodyMedium,
                                color = SensCyan
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                MiniMetric(
                                    label = "Distance",
                                    value = "%.1f km".format(uiState.dailyActivity?.distanceKm ?: 6.4),
                                    icon = Icons.Default.DirectionsRun,
                                    tint = SensCyan
                                )
                                MiniMetric(
                                    label = "Active Cal",
                                    value = "${uiState.dailyActivity?.activeCaloriesKcal ?: 342} kcal",
                                    icon = Icons.Default.LocalFireDepartment,
                                    tint = SensAmber
                                )
                            }
                        }

                        SensProgressRing(
                            progress = progress,
                            modifier = Modifier.size(110.dp),
                            strokeWidth = 10.dp
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                LiveHeartRateBadge(bpm = liveMetrics.liveHeartRateBpm ?: 76)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Hourly Activity Bar Preview
                    Text(
                        text = "Hourly Step Distribution",
                        style = SensTypography.labelSmall,
                        color = SensTextTertiary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SensHourlyBarChart(
                        hourlyValues = uiState.dailyActivity?.hourlySteps ?: List(24) { 0 },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    )
                }
            }

            // Quick Health Metric Grid (SpO2, HRV, Stress, Skin Temp)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickHealthCard(
                        title = "SpO₂",
                        value = "${liveMetrics.spo2Percent ?: 98}%",
                        status = "Optimal",
                        icon = Icons.Default.WaterDrop,
                        accentColor = SensCyan,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToHealth
                    )
                    QuickHealthCard(
                        title = "HRV",
                        value = "${liveMetrics.hrvRmssdMs ?: 54} ms",
                        status = "Balanced",
                        icon = Icons.Default.Speed,
                        accentColor = SensViolet,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToHealth
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickHealthCard(
                        title = "Stress",
                        value = "${liveMetrics.stressScore ?: 22}",
                        status = "Relaxed",
                        icon = Icons.Default.Bolt,
                        accentColor = SensEmerald,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToHealth
                    )
                    QuickHealthCard(
                        title = "Skin Temp",
                        value = "%.1f°C".format(liveMetrics.skinTemperatureCelsius ?: 36.6),
                        status = "Normal baseline",
                        icon = Icons.Default.Thermostat,
                        accentColor = SensAmber,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToHealth
                    )
                }
            }

            // Sleep Summary Card
            item {
                uiState.latestSleep?.let { sleep ->
                    SensGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onNavigateToSleep,
                        accentGlow = SensIndigo
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(SensIndigo.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bedtime,
                                        contentDescription = null,
                                        tint = SensIndigo,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "LAST NIGHT SLEEP",
                                        style = SensTypography.labelSmall,
                                        color = SensTextSecondary
                                    )
                                    Text(
                                        text = "${sleep.durationMinutes / 60}h ${sleep.durationMinutes % 60}m",
                                        style = SensTypography.titleLarge,
                                        color = SensTextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SensIndigo.copy(alpha = 0.2f))
                                    .border(1.dp, SensIndigo.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Score ${sleep.sleepScore}",
                                    style = SensTypography.labelLarge,
                                    color = SensIndigo,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SleepStageMini("Deep", "${sleep.deepMinutes}m", SensIndigo)
                            SleepStageMini("REM", "${sleep.remMinutes}m", SensViolet)
                            SleepStageMini("Light", "${sleep.lightMinutes}m", SensCyan)
                            SleepStageMini("Awake", "${sleep.awakeMinutes}m", SensAmber)
                        }
                    }
                }
            }

            // Quick Start Workout CTA
            item {
                SensGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onNavigateToWorkouts,
                    accentGlow = SensRose
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(SensRose.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FitnessCenter,
                                    contentDescription = null,
                                    tint = SensRose,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Start Workout Tracking",
                                    style = SensTypography.titleMedium,
                                    color = SensTextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Record live GPS, HR zones & cadence",
                                    style = SensTypography.bodyMedium,
                                    color = SensTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Spacing for Floating Nav Bar
            item {
                Spacer(modifier = Modifier.height(84.dp))
            }
        }
    }
}

@Composable
private fun LiveHeartRateBadge(bpm: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 60000 / bpm.coerceIn(50, 180)),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hr_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = "Live Heart Rate",
            tint = SensRose,
            modifier = Modifier
                .size(20.dp)
                .scale(scale)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "$bpm",
            style = SensTypography.titleLarge,
            color = SensTextPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "BPM",
            style = SensTypography.labelSmall,
            color = SensTextSecondary,
            fontSize = 9.sp
        )
    }
}

@Composable
private fun QuickHealthCard(
    title: String,
    value: String,
    status: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    SensGlassCard(
        modifier = modifier,
        onClick = onClick,
        accentGlow = accentColor
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = SensTypography.labelSmall,
                color = SensTextSecondary,
                fontWeight = FontWeight.SemiBold
            )
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = SensTypography.headlineMedium,
            color = SensTextPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = status,
            style = SensTypography.bodyMedium,
            color = accentColor,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun MiniMetric(
    label: String,
    value: String,
    icon: ImageVector,
    tint: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(text = label, style = SensTypography.labelSmall, color = SensTextTertiary, fontSize = 10.sp)
            Text(text = value, style = SensTypography.titleMedium, color = SensTextPrimary, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SleepStageMini(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .width(28.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, style = SensTypography.labelSmall, color = SensTextTertiary, fontSize = 10.sp)
        Text(text = value, style = SensTypography.bodyMedium, color = SensTextPrimary, fontWeight = FontWeight.Medium)
    }
}
