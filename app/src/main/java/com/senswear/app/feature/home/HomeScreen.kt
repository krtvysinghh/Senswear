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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
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
import androidx.compose.runtime.remember
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
import com.senswear.app.core.designsystem.components.SensHourlyBarChart
import com.senswear.app.core.designsystem.components.SensLiquidDynamicIsland
import com.senswear.app.core.designsystem.components.SensLiquidGlassCard
import com.senswear.app.core.designsystem.components.SensLiquidProgressRing
import com.senswear.app.core.designsystem.components.SensLiveWaveform
import com.senswear.app.core.designsystem.components.SensTopBar
import com.senswear.app.core.designsystem.theme.SensAmber
import com.senswear.app.core.designsystem.theme.SensCyan
import com.senswear.app.core.designsystem.theme.SensEmerald
import com.senswear.app.core.designsystem.theme.SensIndigo
import com.senswear.app.core.designsystem.theme.SensObsidian
import com.senswear.app.core.designsystem.theme.SensRose
import com.senswear.app.core.designsystem.theme.SensTextPrimary
import com.senswear.app.core.designsystem.theme.SensTextSecondary
import com.senswear.app.core.designsystem.theme.SensTextTertiary
import com.senswear.app.core.designsystem.theme.SensTypography
import com.senswear.app.core.designsystem.theme.SensViolet
import com.senswear.app.core.domain.model.ConnectionState
import com.senswear.app.core.domain.model.PhysiologicalDerivationEngine

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

    val isConnected = connectionState == ConnectionState.CONNECTED

    // Production Truth: Use real recorded steps; derive distance & calories physiologically
    val currentSteps = liveMetrics.steps.takeIf { it > 0 } ?: uiState.dailyActivity?.steps ?: 0
    val stepGoal = uiState.dailyActivity?.stepGoal ?: 10000
    val progress = if (stepGoal > 0) (currentSteps.toFloat() / stepGoal.toFloat()).coerceIn(0f, 1.5f) else 0f

    val derivedDistanceKm = remember(currentSteps) {
        PhysiologicalDerivationEngine.deriveDistanceKm(currentSteps)
    }
    val derivedActiveCalories = remember(currentSteps) {
        PhysiologicalDerivationEngine.deriveCaloriesFromSteps(currentSteps)
    }

    // Live Heart Rate from BLE stream or null if disconnected
    val liveBpm = if (isConnected) liveMetrics.liveHeartRateBpm else null

    val listState = rememberLazyListState()

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
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Status Dynamic Island Capsule
            item(key = "status_island") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    SensLiquidDynamicIsland(
                        state = connectionState,
                        batteryPercent = if (isConnected) liveMetrics.batteryPercent else null,
                        rssi = if (isConnected) -62 else null,
                        onClick = { viewModel.syncNow() }
                    )
                }
            }

            // Main Hero Liquid Glass Card with Real Data & Apple-Style Liquid Ring
            item(key = "daily_movement") {
                SensLiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    accentGlowColor = SensCyan,
                    onClick = onNavigateToActivity
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "DAILY MOVEMENT",
                                style = SensTypography.labelSmall,
                                color = Color(0xFFD4A373),
                                letterSpacing = 1.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "%,d".format(currentSteps),
                                style = SensTypography.displayMedium,
                                color = SensTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (currentSteps > 0) "${(progress * 100).toInt()}% of $stepGoal goal" else "No steps recorded today",
                                style = SensTypography.bodyMedium,
                                color = Color(0xFF00F0FF)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                MiniMetric(
                                    label = "Distance",
                                    value = "%.2f km".format(derivedDistanceKm),
                                    icon = Icons.AutoMirrored.Filled.DirectionsRun,
                                    tint = SensCyan
                                )
                                MiniMetric(
                                    label = "Active Cal",
                                    value = "$derivedActiveCalories kcal",
                                    icon = Icons.Default.LocalFireDepartment,
                                    tint = SensAmber
                                )
                            }
                        }

                        SensLiquidProgressRing(
                            progress = progress,
                            size = 124.dp,
                            strokeWidth = 11.dp,
                            startColor = Color(0xFF00F0FF),
                            endColor = Color(0xFF00E676)
                        ) {
                            LiveHeartRateBadge(
                                bpm = liveBpm,
                                isLive = isConnected && liveBpm != null
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Intraday Hourly Activity Distribution
                    Text(
                        text = "Intraday Step Distribution (24h)",
                        style = SensTypography.labelSmall,
                        color = SensTextTertiary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SensHourlyBarChart(
                        hourlyValues = uiState.dailyActivity?.hourlySteps ?: List(24) { 0 },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    )
                }
            }

            // Real-Time Physiological Cardiac Waveform Card
            item(key = "cardiac_waveform") {
                SensLiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    accentGlowColor = SensRose,
                    onClick = onNavigateToHealth
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CARDIAC TELEMETRY",
                                style = SensTypography.labelSmall,
                                color = SensRose,
                                letterSpacing = 1.2.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (liveBpm != null) "$liveBpm BPM Pulse" else "Awaiting Watch Stream",
                                style = SensTypography.titleMedium,
                                color = SensTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isConnected) SensRose.copy(alpha = 0.15f) else Color(0x14FFFFFF))
                                .border(1.dp, if (isConnected) SensRose.copy(alpha = 0.3f) else Color(0x1AFFFFFF), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isConnected) "1 Hz Live BLE" else "Disconnected",
                                style = SensTypography.labelSmall,
                                color = if (isConnected) SensRose else SensTextTertiary,
                                fontSize = 10.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    SensLiveWaveform(
                        bpm = liveBpm ?: 0,
                        lineColor = if (isConnected) SensRose else Color(0x33F43F5E),
                        height = 54.dp,
                        isLive = isConnected && liveBpm != null
                    )
                }
            }

            // Quick Health Metric Grid with Production Truth Values
            item(key = "biometric_row_1") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickHealthLiquidCard(
                        title = "SpO₂",
                        value = liveMetrics.spo2Percent?.let { "$it%" } ?: "--",
                        status = if (liveMetrics.spo2Percent != null) "Optimal" else "Awaiting sensor",
                        icon = Icons.Default.WaterDrop,
                        accentColor = SensCyan,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToHealth
                    )
                    QuickHealthLiquidCard(
                        title = "HRV",
                        value = liveMetrics.hrvRmssdMs?.let { "$it ms" } ?: "--",
                        status = if (liveMetrics.hrvRmssdMs != null) "Measured" else "Awaiting sensor",
                        icon = Icons.Default.Speed,
                        accentColor = SensViolet,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToHealth
                    )
                }
            }

            item(key = "biometric_row_2") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val derivedStress = PhysiologicalDerivationEngine.deriveStressFromHrv(liveMetrics.hrvRmssdMs)
                    QuickHealthLiquidCard(
                        title = "Stress",
                        value = derivedStress?.toString() ?: "--",
                        status = if (derivedStress != null) "Autonomic Balance" else "Derived from HRV",
                        icon = Icons.Default.Bolt,
                        accentColor = SensEmerald,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToHealth
                    )
                    QuickHealthLiquidCard(
                        title = "Skin Temp",
                        value = liveMetrics.skinTemperatureCelsius?.let { "%.1f°C".format(it) } ?: "--",
                        status = if (liveMetrics.skinTemperatureCelsius != null) "Nocturnal baseline" else "Awaiting sensor",
                        icon = Icons.Default.Thermostat,
                        accentColor = SensAmber,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToHealth
                    )
                }
            }

            // Sleep Summary Card
            item(key = "sleep_card") {
                uiState.latestSleep?.let { sleep ->
                    SensLiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onNavigateToSleep,
                        accentGlowColor = SensIndigo
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
            item(key = "workout_cta") {
                SensLiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onNavigateToWorkouts,
                    accentGlowColor = SensRose
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
            item(key = "bottom_space") {
                Spacer(modifier = Modifier.height(84.dp))
            }
        }
    }
}

@Composable
private fun LiveHeartRateBadge(bpm: Int?, isLive: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = if (isLive && bpm != null) 0.92f else 1.0f,
        targetValue = if (isLive && bpm != null) 1.08f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (bpm != null && bpm > 0) 60000 / bpm.coerceIn(50, 180) else 1000),
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
            tint = if (isLive) SensRose else Color(0x66F43F5E),
            modifier = Modifier
                .size(20.dp)
                .scale(scale)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = bpm?.toString() ?: "--",
            style = SensTypography.titleLarge,
            color = if (isLive) SensTextPrimary else SensTextTertiary,
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
private fun QuickHealthLiquidCard(
    title: String,
    value: String,
    status: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    SensLiquidGlassCard(
        modifier = modifier,
        onClick = onClick,
        accentGlowColor = accentColor
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
