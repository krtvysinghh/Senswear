package com.senswear.app.core.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senswear.app.core.designsystem.theme.SensCyan
import com.senswear.app.core.designsystem.theme.SensCyanGlow
import com.senswear.app.core.designsystem.theme.SensEmerald
import com.senswear.app.core.designsystem.theme.SensGlassBg
import com.senswear.app.core.designsystem.theme.SensGlassBgHeavy
import com.senswear.app.core.designsystem.theme.SensGlassBorder
import com.senswear.app.core.designsystem.theme.SensGlassBorderHighlight
import com.senswear.app.core.designsystem.theme.SensObsidian
import com.senswear.app.core.designsystem.theme.SensRose
import com.senswear.app.core.designsystem.theme.SensSurfaceDark
import com.senswear.app.core.designsystem.theme.SensTextPrimary
import com.senswear.app.core.designsystem.theme.SensTextSecondary
import com.senswear.app.core.designsystem.theme.SensTextTertiary
import com.senswear.app.core.designsystem.theme.SensTypography
import com.senswear.app.core.domain.model.ConnectionState

enum class SensNavTab(val route: String, val title: String, val icon: ImageVector) {
    HOME("home", "Home", Icons.Default.Home),
    ACTIVITY("activity", "Activity", Icons.Default.DirectionsRun),
    HEALTH("health", "Health", Icons.Default.Favorite),
    SLEEP("sleep", "Sleep", Icons.Default.Bedtime),
    WORKOUTS("workouts", "Workouts", Icons.Default.FitnessCenter),
    DEVICE("device", "Device", Icons.Default.Watch)
}

@Composable
fun SensBottomBar(
    currentTab: SensNavTab,
    onTabSelected: (SensNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x381E293B),
                            Color(0x550F172A)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(SensGlassBorderHighlight, SensGlassBorder.copy(alpha = 0.1f))
                    ),
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SensNavTab.entries.forEach { tab ->
                    val isSelected = currentTab == tab
                    val tint = if (isSelected) SensCyan else SensTextTertiary

                    Column(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { onTabSelected(tab) }
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title,
                            tint = tint,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = tab.title,
                            style = SensTypography.labelSmall,
                            color = tint,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SensTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    connectionState: ConnectionState? = null,
    onActionClick: (() -> Unit)? = null,
    actionIcon: ImageVector? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                style = SensTypography.headlineMedium,
                color = SensTextPrimary,
                fontWeight = FontWeight.Bold
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = SensTypography.bodyMedium,
                    color = SensTextSecondary
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (connectionState != null) {
                SensConnectionBadge(connectionState = connectionState)
            }

            if (onActionClick != null && actionIcon != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SensGlassBg)
                        .border(1.dp, SensGlassBorder, CircleShape)
                        .clickable(onClick = onActionClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = actionIcon,
                        contentDescription = "Action",
                        tint = SensTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SensConnectionBadge(
    connectionState: ConnectionState,
    modifier: Modifier = Modifier
) {
    val (dotColor, text) = when (connectionState) {
        ConnectionState.CONNECTED -> SensEmerald to "Qore 2 Connected"
        ConnectionState.SYNCING -> SensCyan to "Syncing..."
        ConnectionState.CONNECTING -> SensCyan to "Connecting..."
        ConnectionState.SCANNING -> SensCyan to "Scanning..."
        ConnectionState.DISCONNECTED -> SensTextTertiary to "Disconnected"
        ConnectionState.ERROR -> SensRose to "Error"
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(SensGlassBg)
            .border(1.dp, dotColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = SensTypography.labelSmall,
                color = SensTextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun SensEmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    actionButtonText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    SensGlassSurface(
        modifier = modifier.fillMaxWidth().padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Sensors,
                contentDescription = null,
                tint = SensTextTertiary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = SensTypography.titleMedium,
                color = SensTextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                style = SensTypography.bodyMedium,
                color = SensTextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            if (actionButtonText != null && onActionClick != null) {
                Spacer(modifier = Modifier.height(16.dp))
                SensGlassButton(text = actionButtonText, onClick = onActionClick, isPrimary = false)
            }
        }
    }
}
