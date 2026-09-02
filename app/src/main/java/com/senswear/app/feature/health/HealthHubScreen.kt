package com.senswear.app.feature.health

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senswear.app.core.designsystem.components.SensGlassCard
import com.senswear.app.core.designsystem.components.SensGlassChip
import com.senswear.app.core.designsystem.components.SensHeartRateZonesChart
import com.senswear.app.core.designsystem.components.SensLineChart
import com.senswear.app.core.designsystem.components.SensMetricLarge
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

@Composable
fun HealthHubScreen(
    viewModel: HealthViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val liveMetrics by viewModel.liveMetrics.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SensObsidian)
    ) {
        SensTopBar(
            title = "Health Hub",
            subtitle = "Biometric Telemetry & Trends"
        )

        // Sub-tabs row (Heart, SpO2, HRV, Stress, Temp)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(HealthSubTab.entries) { tab ->
                SensGlassChip(
                    text = tab.title,
                    isSelected = uiState.selectedTab == tab,
                    onClick = { viewModel.selectTab(tab) }
                )
            }
        }

        AnimatedContent(
            targetState = uiState.selectedTab,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "health_tab"
        ) { tab ->
            when (tab) {
                HealthSubTab.HEART_RATE -> HeartRateSection(uiState, liveMetrics.liveHeartRateBpm)
                HealthSubTab.SPO2 -> Spo2Section(uiState, liveMetrics.spo2Percent)
                HealthSubTab.HRV -> HrvSection(uiState, liveMetrics.hrvRmssdMs)
                HealthSubTab.STRESS -> StressSection(uiState, liveMetrics.stressScore)
                HealthSubTab.TEMPERATURE -> TemperatureSection(uiState, liveMetrics.skinTemperatureCelsius)
            }
        }
    }
}

@Composable
private fun HeartRateSection(uiState: HealthUiState, liveBpm: Int?) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SensGlassCard(
                modifier = Modifier.fillMaxWidth(),
                accentGlow = SensRose
            ) {
                SensMetricLarge(
                    value = "${liveBpm ?: 76}",
                    unit = "BPM",
                    label = "Live Heart Rate",
                    accentColor = SensRose
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "24-Hour Heart Rate Timeline",
                    style = SensTypography.labelSmall,
                    color = SensTextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                val hrPoints = uiState.heartRateHistory.map { it.bpm.toFloat() }
                SensLineChart(
                    dataPoints = if (hrPoints.isNotEmpty()) hrPoints else listOf(62f, 65f, 74f, 82f, 95f, 78f, 64f, 60f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    lineColor = SensRose
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard("Resting HR", "${uiState.restingHr} BPM", SensCyan, Modifier.weight(1f))
                StatCard("Average HR", "${uiState.avgHr} BPM", SensEmerald, Modifier.weight(1f))
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard("Minimum HR", "${uiState.minHr} BPM", SensIndigo, Modifier.weight(1f))
                StatCard("Peak HR", "${uiState.maxHr} BPM", SensRose, Modifier.weight(1f))
            }
        }

        item {
            SensGlassCard(
                modifier = Modifier.fillMaxWidth(),
                accentGlow = SensCyan
            ) {
                Text(
                    text = "Heart Rate Zones Distribution",
                    style = SensTypography.titleMedium,
                    color = SensTextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(14.dp))
                SensHeartRateZonesChart(
                    zoneMinutes = listOf(320, 140, 45, 18, 6),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item { Spacer(modifier = Modifier.height(84.dp)) }
    }
}

@Composable
private fun Spo2Section(uiState: HealthUiState, liveSpo2: Int?) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SensGlassCard(modifier = Modifier.fillMaxWidth(), accentGlow = SensCyan) {
                SensMetricLarge(
                    value = "${liveSpo2 ?: 98}",
                    unit = "%",
                    label = "Blood Oxygen Saturation",
                    accentColor = SensCyan
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Optimal oxygenation range (95% - 100%)",
                    style = SensTypography.bodyMedium,
                    color = SensEmerald
                )
            }
        }

        item {
            SensGlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "7-Day SpO₂ History",
                    style = SensTypography.titleMedium,
                    color = SensTextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                val spo2Points = uiState.spo2Readings.map { it.percentage.toFloat() }.reversed()
                SensLineChart(
                    dataPoints = if (spo2Points.isNotEmpty()) spo2Points else listOf(98f, 99f, 98f, 97f, 98f, 99f, 98f),
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    lineColor = SensCyan,
                    minVal = 90f,
                    maxVal = 100f
                )
            }
        }

        item {
            ClinicalDisclaimerCard()
        }

        item { Spacer(modifier = Modifier.height(84.dp)) }
    }
}

@Composable
private fun HrvSection(uiState: HealthUiState, liveHrv: Int?) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SensGlassCard(modifier = Modifier.fillMaxWidth(), accentGlow = SensViolet) {
                SensMetricLarge(
                    value = "${liveHrv ?: 54}",
                    unit = "ms",
                    label = "Heart Rate Variability (rMSSD)",
                    accentColor = SensViolet
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Autonomic Nervous System Recovery: Balanced baseline",
                    style = SensTypography.bodyMedium,
                    color = SensTextSecondary
                )
            }
        }

        item {
            SensGlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "7-Day Nightly HRV Trend",
                    style = SensTypography.titleMedium,
                    color = SensTextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                val hrvPoints = uiState.hrvReadings.map { it.rmssdMs.toFloat() }.reversed()
                SensLineChart(
                    dataPoints = if (hrvPoints.isNotEmpty()) hrvPoints else listOf(54f, 58f, 51f, 62f, 49f, 56f, 55f),
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    lineColor = SensViolet,
                    minVal = 30f,
                    maxVal = 80f
                )
            }
        }

        item { Spacer(modifier = Modifier.height(84.dp)) }
    }
}

@Composable
private fun StressSection(uiState: HealthUiState, liveStress: Int?) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SensGlassCard(modifier = Modifier.fillMaxWidth(), accentGlow = SensEmerald) {
                SensMetricLarge(
                    value = "${liveStress ?: 22}",
                    unit = "/ 100",
                    label = "Autonomic Stress Index",
                    accentColor = SensEmerald
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Resting State: Low physical stress / high recovery balance",
                    style = SensTypography.bodyMedium,
                    color = SensEmerald
                )
            }
        }

        item {
            SensGlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Intraday Stress Fluctuations",
                    style = SensTypography.titleMedium,
                    color = SensTextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                val stressPoints = uiState.stressReadings.map { it.score.toFloat() }
                SensLineChart(
                    dataPoints = if (stressPoints.isNotEmpty()) stressPoints else listOf(14f, 18f, 28f, 48f, 32f, 42f, 22f),
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    lineColor = SensEmerald,
                    minVal = 0f,
                    maxVal = 100f
                )
            }
        }

        item { Spacer(modifier = Modifier.height(84.dp)) }
    }
}

@Composable
private fun TemperatureSection(uiState: HealthUiState, liveTemp: Double?) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SensGlassCard(modifier = Modifier.fillMaxWidth(), accentGlow = SensAmber) {
                SensMetricLarge(
                    value = "%.1f".format(liveTemp ?: 36.6),
                    unit = "°C",
                    label = "Skin Temperature",
                    accentColor = SensAmber
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Baseline deviation: ±0.0°C (Stable nocturnal thermoregulation)",
                    style = SensTypography.bodyMedium,
                    color = SensTextSecondary
                )
            }
        }

        item {
            SensGlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "7-Day Temperature Variation",
                    style = SensTypography.titleMedium,
                    color = SensTextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                val tempPoints = uiState.temperatureReadings.map { it.temperatureCelsius.toFloat() }.reversed()
                SensLineChart(
                    dataPoints = if (tempPoints.isNotEmpty()) tempPoints else listOf(36.6f, 36.4f, 36.7f, 36.5f, 36.8f, 36.6f, 36.6f),
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    lineColor = SensAmber,
                    minVal = 35.5f,
                    maxVal = 38.0f
                )
            }
        }

        item { Spacer(modifier = Modifier.height(84.dp)) }
    }
}

@Composable
private fun StatCard(title: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    SensGlassCard(modifier = modifier, accentGlow = accent) {
        Text(text = title, style = SensTypography.labelSmall, color = SensTextSecondary)
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = value, style = SensTypography.titleLarge, color = SensTextPrimary, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ClinicalDisclaimerCard() {
    SensGlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = SensTextTertiary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Senswear and Pebble Qore 2 metrics are designed for general fitness and wellness insight. They are not intended for medical diagnosis, treatment, or clinical assessment.",
                style = SensTypography.bodyMedium,
                color = SensTextTertiary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}
