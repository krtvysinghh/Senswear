package com.senswear.app.feature.device

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senswear.app.core.designsystem.components.SensConnectionBadge
import com.senswear.app.core.designsystem.components.SensGlassButton
import com.senswear.app.core.designsystem.components.SensGlassCard
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
import com.senswear.app.core.domain.model.ConnectionState

@Composable
fun DeviceScreen(
    viewModel: DeviceViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val rawPacketLogs by viewModel.rawPacketLogs.collectAsState()
    var showDiagnostics by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SensObsidian)
    ) {
        SensTopBar(
            title = "Device",
            subtitle = "Pebble Qore 2 Hardware",
            connectionState = connectionState
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Qore 2 Hero Status Card
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(SensCyan.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Watch,
                                    contentDescription = null,
                                    tint = SensCyan,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "PEBBLE QORE 2",
                                    style = SensTypography.titleLarge,
                                    color = SensTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Screen-Free Titanium Chassis",
                                    style = SensTypography.bodyMedium,
                                    color = SensTextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SensMetricLarge(
                            value = "${uiState.batteryState.percentage}%",
                            unit = "(${uiState.batteryState.estimatedDaysRemaining} days remaining)",
                            label = "Battery Health",
                            accentColor = SensEmerald,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SensGlassButton(
                            text = if (uiState.isSyncing) "Syncing..." else "Sync Now",
                            icon = Icons.Default.Refresh,
                            onClick = { viewModel.syncNow() },
                            modifier = Modifier.weight(1f)
                        )

                        SensGlassButton(
                            text = if (connectionState == ConnectionState.CONNECTED) "Disconnect" else "Connect",
                            onClick = {
                                if (connectionState == ConnectionState.CONNECTED) viewModel.disconnectDevice()
                                else viewModel.connectDevice()
                            },
                            modifier = Modifier.weight(1f),
                            isPrimary = false
                        )
                    }
                }
            }

            // Hardware & Firmware Details Card
            item {
                SensGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Hardware Specifications",
                        style = SensTypography.titleMedium,
                        color = SensTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    DetailRow("Model Number", "PB-Q2-BLACK")
                    DetailRow("Firmware Version", "v2.4.1-rc3 (Latest)")
                    DetailRow("Hardware Revision", "Rev. C (Optical PPG 3.0)")
                    DetailRow("MAC Address", "E4:5F:01:A8:2B:99")
                    DetailRow("Bluetooth Profile", "BLE 5.4 Low Energy")
                    DetailRow("Signal RSSI", "-58 dBm (Excellent)")
                    DetailRow("Last Synchronization", uiState.lastSyncMessage)
                }
            }

            // Device Actions & Haptic Testing
            item {
                SensGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Device Tools & Testing",
                        style = SensTypography.titleMedium,
                        color = SensTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SensGlassButton(
                            text = "Test Haptics",
                            icon = Icons.Default.Vibration,
                            onClick = { viewModel.testHapticFeedback(1) },
                            modifier = Modifier.weight(1f),
                            isPrimary = false
                        )
                        SensGlassButton(
                            text = "Diagnostics",
                            icon = Icons.Default.BugReport,
                            onClick = { showDiagnostics = !showDiagnostics },
                            modifier = Modifier.weight(1f),
                            isPrimary = false
                        )
                    }
                }
            }

            // Real-time Diagnostics Terminal (if expanded)
            item {
                AnimatedVisibility(visible = showDiagnostics) {
                    SensGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        accentGlow = SensEmerald
                    ) {
                        Text(
                            text = "Developer BLE Packet Stream",
                            style = SensTypography.titleMedium,
                            color = SensTextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF070A10))
                                .padding(10.dp)
                        ) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                reverseLayout = true
                            ) {
                                items(rawPacketLogs.reversed()) { log ->
                                    Text(
                                        text = log,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = if (log.contains("RX")) SensEmerald else if (log.contains("TX")) SensCyan else SensTextSecondary,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(84.dp)) }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = SensTypography.bodyMedium, color = SensTextSecondary)
        Text(text = value, style = SensTypography.bodyMedium, color = SensTextPrimary, fontWeight = FontWeight.Medium)
    }
}
