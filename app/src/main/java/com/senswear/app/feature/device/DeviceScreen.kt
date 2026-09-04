package com.senswear.app.feature.device

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senswear.app.core.designsystem.components.SensConnectionBadge
import com.senswear.app.core.designsystem.components.SensGlassButton
import com.senswear.app.core.designsystem.components.SensLiquidCapsule
import com.senswear.app.core.designsystem.components.SensLiquidGlassCard
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
import com.senswear.app.core.domain.model.WearableBrand

@Composable
fun DeviceScreen(
    viewModel: DeviceViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val rawPacketLogs by viewModel.rawPacketLogs.collectAsState()
    var showDiagnostics by remember { mutableStateOf(false) }

    val isConnected = connectionState == ConnectionState.CONNECTED
    val listState = rememberLazyListState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SensObsidian)
    ) {
        SensTopBar(
            title = "Wearables Hub",
            subtitle = if (isConnected) "Connected: ${uiState.device?.name ?: "Watch"}" else "Universal Biometric Wearables",
            connectionState = connectionState
        )

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Multi-Brand Wearable Selector Carousel
            item(key = "brand_carousel") {
                Column {
                    Text(
                        text = "SUPPORTED WEARABLES",
                        style = SensTypography.labelSmall,
                        color = Color(0xFFD4A373),
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WearableBrand.entries.forEach { brand ->
                            val isSelected = uiState.selectedBrand == brand
                            SensLiquidCapsule(
                                text = brand.displayName,
                                isSelected = isSelected,
                                onClick = { viewModel.selectBrand(brand) }
                            )
                        }
                    }
                }
            }

            // Hero Connected Device Card / Universal Pairing Card
            item(key = "hero_device_card") {
                SensLiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    accentGlowColor = if (isConnected) SensEmerald else SensCyan
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(if (isConnected) SensEmerald.copy(alpha = 0.2f) else SensCyan.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isConnected) Icons.Default.Watch else Icons.Default.BluetoothSearching,
                                    contentDescription = null,
                                    tint = if (isConnected) SensEmerald else SensCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = if (isConnected) (uiState.device?.name ?: "Wearable Device") else uiState.selectedBrand.displayName,
                                    style = SensTypography.titleMedium,
                                    color = SensTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isConnected) "Active Bluetooth LE Telemetry Stream" else uiState.selectedBrand.brandCategory,
                                    style = SensTypography.bodyMedium,
                                    color = SensTextSecondary
                                )
                            }
                        }

                        SensConnectionBadge(connectionState = connectionState)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isConnected) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            DeviceStatMini(
                                label = "Battery",
                                value = "${uiState.batteryState.percentage}%",
                                icon = Icons.Default.BatteryChargingFull,
                                tint = SensEmerald
                            )
                            DeviceStatMini(
                                label = "Protocol",
                                value = "GATT BLE 5.4",
                                icon = Icons.Default.Bluetooth,
                                tint = SensCyan
                            )
                            DeviceStatMini(
                                label = "Status",
                                value = "Synced",
                                icon = Icons.Default.CheckCircle,
                                tint = SensIndigo
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SensGlassButton(
                                text = if (uiState.isSyncing) "Syncing..." else "Sync Now",
                                onClick = { viewModel.syncNow() },
                                modifier = Modifier.weight(1f),
                                isPrimary = true
                            )
                            SensGlassButton(
                                text = "Disconnect",
                                onClick = { viewModel.disconnectDevice() },
                                modifier = Modifier.weight(1f),
                                isPrimary = false
                            )
                        }
                    } else {
                        Column {
                            Text(
                                text = "Compatible with Samsung Galaxy Watch 4/5/6/7, Apple Watch (BLE Heart Rate Broadcast), Whoop 3.0/4.0, Garmin, Fitbit, Oura Ring, and Pebble Qore 2.",
                                style = SensTypography.bodySmall,
                                color = SensTextTertiary,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                SensGlassButton(
                                    text = if (uiState.isScanning) "Stop Scanning" else "Scan Nearby Wearables",
                                    onClick = {
                                        if (uiState.isScanning) viewModel.stopScanning() else viewModel.startScanning()
                                    },
                                    modifier = Modifier.weight(1f),
                                    isPrimary = true
                                )
                            }
                        }
                    }
                }
            }

            // Scanned Nearby Devices List
            if (uiState.isScanning || uiState.scannedDevices.isNotEmpty()) {
                item(key = "scanned_header") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DISCOVERED WEARABLES (${uiState.scannedDevices.size})",
                            style = SensTypography.labelSmall,
                            color = SensCyan,
                            letterSpacing = 1.2.sp
                        )
                        if (uiState.isScanning) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 2.dp,
                                    color = SensCyan
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Scanning...",
                                    style = SensTypography.labelSmall,
                                    color = SensTextTertiary
                                )
                            }
                        }
                    }
                }

                items(uiState.scannedDevices, key = { it.device.macAddress }) { scanned ->
                    SensLiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { viewModel.connectDevice(scanned.device.macAddress) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = scanned.device.name,
                                        style = SensTypography.titleMedium,
                                        color = SensTextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0x2200F0FF))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = scanned.brand.displayName,
                                            style = SensTypography.labelSmall,
                                            color = SensCyan,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                                Text(
                                    text = "${scanned.device.macAddress} • RSSI: ${scanned.rssi} dBm",
                                    style = SensTypography.labelSmall,
                                    color = SensTextTertiary
                                )
                            }

                            SensGlassButton(
                                text = "Pair",
                                onClick = { viewModel.connectDevice(scanned.device.macAddress) },
                                isPrimary = true
                            )
                        }
                    }
                }
            }

            // Developer Packet Trace Console
            item(key = "diagnostics_toggle") {
                SensLiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showDiagnostics = !showDiagnostics }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.BugReport,
                                contentDescription = null,
                                tint = SensAmber,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Live GATT Telemetry Terminal",
                                style = SensTypography.titleMedium,
                                color = SensTextPrimary
                            )
                        }
                        Text(
                            text = if (showDiagnostics) "Hide" else "Expand (${rawPacketLogs.size})",
                            style = SensTypography.labelSmall,
                            color = SensCyan
                        )
                    }

                    AnimatedVisibility(visible = showDiagnostics) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF030712))
                                    .padding(10.dp)
                            ) {
                                if (rawPacketLogs.isEmpty()) {
                                    Text(
                                        text = "Awaiting GATT telemetry packets...",
                                        style = SensTypography.bodySmall,
                                        color = SensTextTertiary,
                                        fontFamily = FontFamily.Monospace
                                    )
                                } else {
                                    LazyColumn {
                                        items(rawPacketLogs.reversed()) { log ->
                                            Text(
                                                text = log,
                                                style = SensTypography.bodySmall,
                                                color = Color(0xFF38BDF8),
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }
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
private fun DeviceStatMini(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(text = label, style = SensTypography.labelSmall, color = SensTextTertiary, fontSize = 10.sp)
            Text(text = value, style = SensTypography.titleSmall, color = SensTextPrimary, fontWeight = FontWeight.Bold)
        }
    }
}
