package com.senswear.app.feature.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.senswear.app.core.designsystem.components.SensGlassCard
import com.senswear.app.core.designsystem.components.SensGlassChip
import com.senswear.app.core.designsystem.components.SensLineChart
import com.senswear.app.core.designsystem.components.SensTopBar
import com.senswear.app.core.designsystem.theme.SensCyan
import com.senswear.app.core.designsystem.theme.SensIndigo
import com.senswear.app.core.designsystem.theme.SensObsidian
import com.senswear.app.core.designsystem.theme.SensTextPrimary
import com.senswear.app.core.designsystem.theme.SensTextSecondary
import com.senswear.app.core.designsystem.theme.SensTypography
import com.senswear.app.core.domain.model.DailyActivity

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SensObsidian)
    ) {
        SensTopBar(
            title = "History",
            subtitle = "Long-Term Health Analytics"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HistoryRange.entries.forEach { r ->
                SensGlassChip(
                    text = r.label,
                    isSelected = uiState.range == r,
                    onClick = { viewModel.setRange(r) }
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SensGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    accentGlow = SensCyan
                ) {
                    Text(
                        text = "Aggregated Step Trends (${uiState.range.label})",
                        style = SensTypography.titleMedium,
                        color = SensTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    val stepPoints = uiState.activities.map { it.steps.toFloat() }.reversed()
                    SensLineChart(
                        dataPoints = if (stepPoints.isNotEmpty()) stepPoints else listOf(8000f, 10000f, 9000f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        lineColor = SensCyan
                    )
                }
            }

            item {
                Text(
                    text = "Daily Logs",
                    style = SensTypography.titleLarge,
                    color = SensTextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(uiState.activities) { act ->
                DailyHistoryRowCard(activity = act)
            }

            item { Spacer(modifier = Modifier.height(84.dp)) }
        }
    }
}

@Composable
private fun DailyHistoryRowCard(activity: DailyActivity) {
    val progress = (activity.steps.toFloat() / activity.stepGoal.toFloat() * 100).toInt()

    SensGlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Day ${activity.epochDay % 30 + 1}",
                    style = SensTypography.titleMedium,
                    color = SensTextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "%.2f km • %d kcal".format(activity.distanceKm, activity.activeCaloriesKcal),
                    style = SensTypography.bodyMedium,
                    color = SensTextSecondary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "%,d steps".format(activity.steps),
                    style = SensTypography.titleMedium,
                    color = SensCyan,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$progress% of goal",
                    style = SensTypography.labelSmall,
                    color = SensTextSecondary
                )
            }
        }
    }
}
