package com.senswear.app.feature.sleep

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senswear.app.core.designsystem.components.SensGlassCard
import com.senswear.app.core.designsystem.components.SensLineChart
import com.senswear.app.core.designsystem.components.SensMetricLarge
import com.senswear.app.core.designsystem.components.SensSleepHypnogram
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
fun SleepScreen(
    viewModel: SleepViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val session = uiState.latestSession

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SensObsidian)
    ) {
        SensTopBar(
            title = "Sleep & Recovery",
            subtitle = "Architecture & Restorative Cycles"
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Sleep Duration & Score Card
            item {
                SensGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    accentGlow = SensIndigo
                ) {
                    val durationMin = session?.durationMinutes ?: 462
                    SensMetricLarge(
                        value = "${durationMin / 60}h ${durationMin % 60}m",
                        unit = "Score ${session?.sleepScore ?: 88}",
                        label = "Total Nightly Sleep",
                        accentColor = SensIndigo
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bedtime & Wake-time chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TimeChip(label = "Asleep", time = "11:18 PM", icon = Icons.Default.Nightlight, modifier = Modifier.weight(1f))
                        TimeChip(label = "Awake", time = "7:00 AM", icon = Icons.Default.WbSunny, modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Sleep Architecture Timeline (Hypnogram)",
                        style = SensTypography.labelSmall,
                        color = SensTextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    SensSleepHypnogram(
                        stages = session?.stages ?: emptyList(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Sleep Stage Breakdown Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StageStatCard("Deep Sleep", "${session?.deepMinutes ?: 104} min", "22% • Physical recovery", SensIndigo, Modifier.weight(1f))
                    StageStatCard("REM Sleep", "${session?.remMinutes ?: 112} min", "24% • Cognitive memory", SensViolet, Modifier.weight(1f))
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StageStatCard("Light Sleep", "${session?.lightMinutes ?: 216} min", "48% • Core rest", SensCyan, Modifier.weight(1f))
                    StageStatCard("Awake Time", "${session?.awakeMinutes ?: 30} min", "6% • Brief micro-arousals", SensAmber, Modifier.weight(1f))
                }
            }

            // 7-Day Sleep Score Trend
            item {
                SensGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    accentGlow = SensViolet
                ) {
                    Text(
                        text = "7-Day Sleep Score Trend",
                        style = SensTypography.titleMedium,
                        color = SensTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    val scores = uiState.recentSessions.map { it.sleepScore.toFloat() }.reversed()
                    SensLineChart(
                        dataPoints = if (scores.isNotEmpty()) scores else listOf(88f, 85f, 91f, 84f, 89f, 82f, 88f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        lineColor = SensViolet,
                        minVal = 60f,
                        maxVal = 100f
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(84.dp)) }
        }
    }
}

@Composable
private fun TimeChip(label: String, time: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x18FFFFFF))
            .border(1.dp, Color(0x28FFFFFF), RoundedCornerShape(14.dp))
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = SensIndigo, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = label, style = SensTypography.labelSmall, color = SensTextTertiary, fontSize = 10.sp)
                Text(text = time, style = SensTypography.titleMedium, color = SensTextPrimary, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun StageStatCard(title: String, duration: String, desc: String, color: Color, modifier: Modifier = Modifier) {
    SensGlassCard(modifier = modifier, accentGlow = color) {
        Text(text = title, style = SensTypography.labelSmall, color = color, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = duration, style = SensTypography.headlineMedium, color = SensTextPrimary, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = desc, style = SensTypography.bodyMedium, color = SensTextSecondary, fontSize = 11.sp)
    }
}
