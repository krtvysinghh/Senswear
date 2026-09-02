package com.senswear.app.feature.workouts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senswear.app.core.designsystem.components.SensGlassButton
import com.senswear.app.core.designsystem.components.SensGlassCard
import com.senswear.app.core.designsystem.components.SensGlassChip
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
import com.senswear.app.core.domain.model.HeartRateZone
import com.senswear.app.core.domain.model.WorkoutSession
import com.senswear.app.core.domain.model.WorkoutType

@Composable
fun WorkoutsScreen(
    viewModel: WorkoutsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val liveMetrics by viewModel.liveMetrics.collectAsState()
    val activeSession = uiState.activeSession

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SensObsidian)
    ) {
        SensTopBar(
            title = "Workouts",
            subtitle = "Live Recording & History"
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Active Workout Session Card (if active)
            item {
                AnimatedVisibility(
                    visible = activeSession != null,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    activeSession?.let { session ->
                        LiveWorkoutRecordingCard(
                            session = session,
                            liveBpm = liveMetrics.liveHeartRateBpm ?: 136,
                            onStopClick = { viewModel.stopWorkout() }
                        )
                    }
                }
            }

            // Start New Workout Card
            item {
                if (activeSession == null) {
                    SensGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        accentGlow = SensRose
                    ) {
                        Text(
                            text = "START NEW WORKOUT",
                            style = SensTypography.labelSmall,
                            color = SensTextSecondary,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Workout Type Selector Chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(listOf(WorkoutType.OUTDOOR_WALK, WorkoutType.OUTDOOR_RUN, WorkoutType.CYCLING, WorkoutType.HIIT, WorkoutType.STRENGTH_TRAINING)) { type ->
                                SensGlassChip(
                                    text = type.displayName,
                                    isSelected = uiState.selectedWorkoutType == type,
                                    onClick = { viewModel.selectWorkoutType(type) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        SensGlassButton(
                            text = "Start ${uiState.selectedWorkoutType.displayName}",
                            icon = Icons.Default.PlayArrow,
                            onClick = { viewModel.startWorkout() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Recent Completed Workouts List
            item {
                Text(
                    text = "Recent Workouts",
                    style = SensTypography.titleLarge,
                    color = SensTextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(uiState.recentWorkouts) { workout ->
                WorkoutHistoryCard(workout = workout)
            }

            item { Spacer(modifier = Modifier.height(84.dp)) }
        }
    }
}

@Composable
private fun LiveWorkoutRecordingCard(
    session: WorkoutSession,
    liveBpm: Int,
    onStopClick: () -> Unit
) {
    val durationSec = session.durationSeconds
    val mins = durationSec / 60
    val secs = durationSec % 60
    val timeFormatted = "%02d:%02d".format(mins, secs)
    val zone = HeartRateZone.fromBpm(liveBpm)

    SensGlassCard(
        modifier = Modifier.fillMaxWidth(),
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
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(SensRose)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "RECORDING: ${session.type.displayName.uppercase()}",
                    style = SensTypography.labelLarge,
                    color = SensRose,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = timeFormatted,
                style = SensTypography.headlineMedium,
                color = SensTextPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "LIVE HEART RATE", style = SensTypography.labelSmall, color = SensTextSecondary)
                Text(text = "$liveBpm BPM", style = SensTypography.displayMedium, color = SensRose, fontWeight = FontWeight.Bold)
                Text(text = zone.title, style = SensTypography.bodyMedium, color = SensAmber, fontSize = 11.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(text = "DISTANCE & PACE", style = SensTypography.labelSmall, color = SensTextSecondary)
                Text(text = "%.2f km".format(session.distanceKm), style = SensTypography.displayMedium, color = SensCyan, fontWeight = FontWeight.Bold)
                Text(text = "${session.totalCaloriesKcal} kcal burned", style = SensTypography.bodyMedium, color = SensEmerald, fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SensGlassButton(
            text = "Finish & Save Workout",
            icon = Icons.Default.Stop,
            onClick = onStopClick,
            modifier = Modifier.fillMaxWidth(),
            isPrimary = false
        )
    }
}

@Composable
private fun WorkoutHistoryCard(workout: WorkoutSession) {
    val mins = workout.durationSeconds / 60
    val secs = workout.durationSeconds % 60
    val timeFormatted = "%d min %02d sec".format(mins, secs)

    val icon = when (workout.type) {
        WorkoutType.OUTDOOR_WALK -> Icons.Default.DirectionsWalk
        WorkoutType.OUTDOOR_RUN -> Icons.Default.DirectionsRun
        WorkoutType.CYCLING -> Icons.Default.DirectionsBike
        else -> Icons.Default.FitnessCenter
    }

    SensGlassCard(
        modifier = Modifier.fillMaxWidth(),
        accentGlow = SensCyan
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
                        .background(SensCyan.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = SensCyan, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = workout.type.displayName, style = SensTypography.titleMedium, color = SensTextPrimary, fontWeight = FontWeight.SemiBold)
                    Text(text = timeFormatted, style = SensTypography.bodyMedium, color = SensTextSecondary)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(text = "${workout.totalCaloriesKcal} kcal", style = SensTypography.titleMedium, color = SensAmber, fontWeight = FontWeight.Bold)
                Text(text = "Avg ${workout.avgHeartRateBpm} BPM", style = SensTypography.labelSmall, color = SensTextSecondary)
            }
        }
    }
}
