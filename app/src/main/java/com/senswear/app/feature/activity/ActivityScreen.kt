package com.senswear.app.feature.activity

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timeline
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
import com.senswear.app.core.designsystem.components.SensGlassChip
import com.senswear.app.core.designsystem.components.SensHourlyBarChart
import com.senswear.app.core.designsystem.components.SensLineChart
import com.senswear.app.core.designsystem.components.SensMetricLarge
import com.senswear.app.core.designsystem.components.SensProgressRing
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

@Composable
fun ActivityScreen(
    viewModel: ActivityViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val activity = uiState.todayActivity

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SensObsidian)
    ) {
        SensTopBar(
            title = "Activity",
            subtitle = "Movement & Caloric Burn"
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Daily Step Target
            item {
                SensGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    accentGlow = SensCyan
                ) {
                    SensMetricLarge(
                        value = "%,d".format(activity?.steps ?: 8421),
                        unit = "/ %,d steps".format(activity?.stepGoal ?: 10000),
                        label = "Daily Step Count",
                        accentColor = SensCyan
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "24-Hour Intraday Movement",
                        style = SensTypography.labelSmall,
                        color = SensTextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SensHourlyBarChart(
                        hourlyValues = activity?.hourlySteps ?: List(24) { 0 },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                    )
                }
            }

            // Metric Breakdown Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Distance",
                        value = "%.2f".format(activity?.distanceKm ?: 6.4),
                        unit = "km",
                        icon = Icons.Default.DirectionsRun,
                        color = SensCyan,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Active Burn",
                        value = "${activity?.activeCaloriesKcal ?: 342}",
                        unit = "kcal",
                        icon = Icons.Default.LocalFireDepartment,
                        color = SensAmber,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Active Time",
                        value = "${activity?.activeMinutes ?: 48}",
                        unit = "mins",
                        icon = Icons.Default.Schedule,
                        color = SensEmerald,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Total Burn",
                        value = "${activity?.totalCaloriesKcal ?: 1942}",
                        unit = "kcal",
                        icon = Icons.Default.Timeline,
                        color = SensRose,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 7-Day / 30-Day Step Trend
            item {
                SensGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    accentGlow = SensIndigo
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Historical Step Trend",
                            style = SensTypography.titleMedium,
                            color = SensTextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SensGlassChip(
                                text = "7D",
                                isSelected = uiState.selectedTimeRange == 7,
                                onClick = { viewModel.setTimeRange(7) }
                            )
                            SensGlassChip(
                                text = "30D",
                                isSelected = uiState.selectedTimeRange == 30,
                                onClick = { viewModel.setTimeRange(30) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val trendPoints = uiState.weeklyHistory.map { it.steps.toFloat() }.reversed()
                    SensLineChart(
                        dataPoints = if (trendPoints.isNotEmpty()) trendPoints else listOf(8421f, 10450f, 9120f, 7890f, 11300f, 6400f, 9850f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        lineColor = SensIndigo
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(84.dp))
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    unit: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    SensGlassCard(modifier = modifier, accentGlow = color) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, style = SensTypography.labelSmall, color = SensTextSecondary)
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(text = value, style = SensTypography.headlineLarge, color = SensTextPrimary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = unit, style = SensTypography.bodyMedium, color = color, modifier = Modifier.padding(bottom = 4.dp))
        }
    }
}
