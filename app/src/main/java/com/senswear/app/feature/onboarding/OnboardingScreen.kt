package com.senswear.app.feature.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senswear.app.core.designsystem.components.SensGlassButton
import com.senswear.app.core.designsystem.components.SensGlassCard
import com.senswear.app.core.designsystem.components.SensGlassSurface
import com.senswear.app.core.designsystem.theme.SensCyan
import com.senswear.app.core.designsystem.theme.SensCyanGlow
import com.senswear.app.core.designsystem.theme.SensEmerald
import com.senswear.app.core.designsystem.theme.SensIndigo
import com.senswear.app.core.designsystem.theme.SensObsidian
import com.senswear.app.core.designsystem.theme.SensTextPrimary
import com.senswear.app.core.designsystem.theme.SensTextSecondary
import com.senswear.app.core.designsystem.theme.SensTextTertiary
import com.senswear.app.core.designsystem.theme.SensTypography
import com.senswear.app.core.domain.model.WearableDevice

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SensObsidian)
            .statusBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = uiState.currentStep,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "onboarding_step"
        ) { step ->
            when (step) {
                OnboardingStep.WELCOME -> WelcomeStep(onNext = { viewModel.nextStep() })
                OnboardingStep.PERMISSIONS -> PermissionsStep(onNext = { viewModel.nextStep() })
                OnboardingStep.SCANNING -> ScanningStep()
                OnboardingStep.DEVICE_FOUND -> DeviceFoundStep(
                    device = uiState.selectedDevice,
                    onConnect = { viewModel.nextStep() }
                )
                OnboardingStep.CONNECTING -> ConnectingStep()
                OnboardingStep.HEALTH_CONNECT_SETUP -> HealthConnectStep(onNext = { viewModel.nextStep() })
                OnboardingStep.COMPLETE -> CompleteStep(onFinish = { viewModel.completeOnboarding(onFinish) })
            }
        }
    }
}

@Composable
private fun WelcomeStep(onNext: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(SensCyan, SensIndigo))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Watch,
                contentDescription = null,
                tint = SensObsidian,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Welcome to Senswear",
            style = SensTypography.displayMedium,
            color = SensTextPrimary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Your premium companion dashboard for the screen-free Pebble Qore 2 wellness band.",
            style = SensTypography.bodyLarge,
            color = SensTextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        SensGlassButton(
            text = "Get Started",
            onClick = onNext,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PermissionsStep(onNext: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Hardware Access",
            style = SensTypography.headlineMedium,
            color = SensTextPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Senswear requires Bluetooth LE to connect directly to your Pebble Qore 2 for real-time telemetry.",
            style = SensTypography.bodyMedium,
            color = SensTextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        SensGlassCard(modifier = Modifier.fillMaxWidth()) {
            PermissionRow("Bluetooth Low Energy", "Discover and maintain continuous link with Qore 2", true)
            Spacer(modifier = Modifier.height(12.dp))
            PermissionRow("Google Health Connect", "Reconcile daily historical steps, sleep & calories", true)
            Spacer(modifier = Modifier.height(12.dp))
            PermissionRow("Background Sync", "Keep your stats updated seamlessly without battery drain", true)
        }

        Spacer(modifier = Modifier.height(36.dp))

        SensGlassButton(
            text = "Grant & Search Device",
            onClick = onNext,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PermissionRow(title: String, desc: String, isGranted: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (isGranted) SensEmerald.copy(alpha = 0.2f) else SensTextTertiary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = if (isGranted) SensEmerald else SensTextTertiary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, style = SensTypography.titleMedium, color = SensTextPrimary, fontWeight = FontWeight.SemiBold)
            Text(text = desc, style = SensTypography.bodyMedium, color = SensTextSecondary, fontSize = 11.sp)
        }
    }
}

@Composable
private fun ScanningStep() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        CircularProgressIndicator(
            color = SensCyan,
            modifier = Modifier.size(64.dp),
            strokeWidth = 4.dp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Searching for Pebble Qore 2...",
            style = SensTypography.headlineMedium,
            color = SensTextPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Make sure your Qore 2 band is nearby and charged.",
            style = SensTypography.bodyMedium,
            color = SensTextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DeviceFoundStep(device: WearableDevice?, onConnect: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Device Found",
            style = SensTypography.headlineMedium,
            color = SensTextPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "We discovered your Pebble Qore 2 nearby.",
            style = SensTypography.bodyMedium,
            color = SensTextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        SensGlassCard(
            modifier = Modifier.fillMaxWidth(),
            accentGlow = SensCyan
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(SensCyan.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Watch, contentDescription = null, tint = SensCyan, modifier = Modifier.size(26.dp))
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(text = device?.name ?: "Pebble Qore 2", style = SensTypography.titleLarge, color = SensTextPrimary, fontWeight = FontWeight.Bold)
                    Text(text = "Signal: ${device?.rssi ?: -54} dBm • Ready to pair", style = SensTypography.bodyMedium, color = SensEmerald)
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        SensGlassButton(
            text = "Pair & Connect",
            onClick = onConnect,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ConnectingStep() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        CircularProgressIndicator(
            color = SensEmerald,
            modifier = Modifier.size(64.dp),
            strokeWidth = 4.dp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Pairing with Qore 2...",
            style = SensTypography.headlineMedium,
            color = SensTextPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Configuring GATT notification channels and telemetry sync.",
            style = SensTypography.bodyMedium,
            color = SensTextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun HealthConnectStep(onNext: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Google Health Connect",
            style = SensTypography.headlineMedium,
            color = SensTextPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Senswear synchronizes bi-directionally with Android's secure Health Connect ecosystem.",
            style = SensTypography.bodyMedium,
            color = SensTextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        SensGlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Favorite, contentDescription = null, tint = SensCyan, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = "Unified Health Data", style = SensTypography.titleMedium, color = SensTextPrimary, fontWeight = FontWeight.SemiBold)
                    Text(text = "Automatic deduplication and authoritative source reconciliation", style = SensTypography.bodyMedium, color = SensTextSecondary, fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        SensGlassButton(
            text = "Complete Integration",
            onClick = onNext,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CompleteStep(onFinish: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(SensEmeraldGlow),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = SensObsidian,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "All Systems Ready",
            style = SensTypography.displayMedium,
            color = SensTextPrimary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your Pebble Qore 2 is connected and syncing live telemetry.",
            style = SensTypography.bodyLarge,
            color = SensTextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(44.dp))

        SensGlassButton(
            text = "Enter Senswear",
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private val SensEmeraldGlow = Brush.horizontalGradient(
    colors = listOf(Color(0xFF10B981), Color(0xFF059669))
)
