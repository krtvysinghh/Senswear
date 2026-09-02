package com.senswear.app.feature.settings

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senswear.app.core.designsystem.components.SensGlassButton
import com.senswear.app.core.designsystem.components.SensGlassCard
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
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SensObsidian)
    ) {
        SensTopBar(
            title = "Settings",
            subtitle = "Goals, Privacy & Integration"
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Daily Goals Configuration
            item {
                SensGlassCard(modifier = Modifier.fillMaxWidth(), accentGlow = SensCyan) {
                    Text(
                        text = "Daily Goals Target",
                        style = SensTypography.titleMedium,
                        color = SensTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Step Goal: %,d steps".format(uiState.stepGoal),
                        style = SensTypography.bodyMedium,
                        color = SensCyan
                    )
                    Slider(
                        value = uiState.stepGoal.toFloat(),
                        onValueChange = { viewModel.updateStepGoal(it.toInt()) },
                        valueRange = 5000f..25000f,
                        steps = 19,
                        colors = SliderDefaults.colors(
                            thumbColor = SensCyan,
                            activeTrackColor = SensCyan,
                            inactiveTrackColor = Color(0x28FFFFFF)
                        )
                    )
                }
            }

            // Unit Preferences
            item {
                SensGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { viewModel.toggleUnitSystem() }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = SensCyan, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "Measurement Units", style = SensTypography.titleMedium, color = SensTextPrimary, fontWeight = FontWeight.SemiBold)
                                Text(text = uiState.unitSystem.label, style = SensTypography.bodyMedium, color = SensTextSecondary)
                            }
                        }
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = SensTextTertiary)
                    }
                }
            }

            // Health Connect Integration Status
            item {
                SensGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Favorite, contentDescription = null, tint = SensRose, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "Google Health Connect", style = SensTypography.titleMedium, color = SensTextPrimary, fontWeight = FontWeight.SemiBold)
                                Text(text = "Connected & Active (Steps, HR, Sleep)", style = SensTypography.bodyMedium, color = SensEmerald)
                            }
                        }
                    }
                }
            }

            // Privacy & Data Sovereignty
            item {
                SensGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Privacy & Local Data",
                        style = SensTypography.titleMedium,
                        color = SensTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Senswear operates on a strict local-first architecture. No account required, no remote servers, zero telemetry tracking.",
                        style = SensTypography.bodyMedium,
                        color = SensTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SensGlassButton(
                            text = if (uiState.isExportSuccess) "Exported!" else "Export JSON",
                            icon = Icons.Default.Download,
                            onClick = { viewModel.exportDataJson() },
                            modifier = Modifier.weight(1f),
                            isPrimary = false
                        )
                        SensGlassButton(
                            text = if (uiState.isDataDeleted) "Purged" else "Delete Data",
                            icon = Icons.Default.DeleteForever,
                            onClick = { viewModel.deleteAllData() },
                            modifier = Modifier.weight(1f),
                            isPrimary = false
                        )
                    }
                }
            }

            // About Senswear
            item {
                SensGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "About Senswear", style = SensTypography.titleMedium, color = SensTextPrimary, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Version: 1.0.0 (Production Build)", style = SensTypography.bodyMedium, color = SensTextSecondary)
                    Text(text = "Engine: Kotlin • Jetpack Compose • BLE Direct • Health Connect", style = SensTypography.bodyMedium, color = SensTextTertiary, fontSize = 11.sp)
                }
            }

            item { Spacer(modifier = Modifier.height(84.dp)) }
        }
    }
}
