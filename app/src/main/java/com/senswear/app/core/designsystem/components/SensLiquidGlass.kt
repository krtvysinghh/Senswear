package com.senswear.app.core.designsystem.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senswear.app.core.designsystem.theme.SensCyan
import com.senswear.app.core.designsystem.theme.SensEmerald
import com.senswear.app.core.designsystem.theme.SensGlassBorder
import com.senswear.app.core.designsystem.theme.SensGlassBorderHighlight
import com.senswear.app.core.designsystem.theme.SensObsidian
import com.senswear.app.core.designsystem.theme.SensRose
import com.senswear.app.core.designsystem.theme.SensTextMuted
import com.senswear.app.core.designsystem.theme.SensTextPrimary
import com.senswear.app.core.designsystem.theme.SensTypography
import com.senswear.app.core.domain.model.ConnectionState

/**
 * Ultra-premium Apple Native Liquid Glass card container.
 * Features multi-layered frosted refraction, specular top border highlight,
 * and subtle ambient chromatic dispersion.
 */
@Composable
fun SensLiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    accentGlowColor: Color = Color.Transparent,
    hasSpecularHighlight: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    } else Modifier

    val baseBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0x28FFFFFF), // 16% frosted top sheen
            Color(0x0EFFFFFF), // 5.5% subtle body
            Color(0x05FFFFFF)  // 2% deep bottom fade
        )
    )

    val borderBrush = Brush.verticalGradient(
        colors = if (hasSpecularHighlight) {
            listOf(
                Color(0x52FFFFFF), // Crisp liquid glass specular edge
                Color(0x1AFFFFFF),
                Color(0x0AFFFFFF)
            )
        } else {
            listOf(SensGlassBorder, SensGlassBorder.copy(alpha = 0.05f))
        }
    )

    Box(
        modifier = modifier
            .shadow(
                elevation = 16.dp,
                shape = shape,
                ambientColor = accentGlowColor.copy(alpha = 0.15f),
                spotColor = accentGlowColor.copy(alpha = 0.25f)
            )
            .clip(shape)
            .background(Color(0xFF090D18).copy(alpha = 0.85f)) // Obsidian base layer
            .background(baseBrush)                             // Liquid glass refraction layer
            .border(width = 1.dp, brush = borderBrush, shape = shape)
            .then(clickableModifier)
            .padding(20.dp)
    ) {
        Column(content = content)
    }
}

/**
 * Floating Liquid Capsule for metrics, quick filters, and status bars.
 */
@Composable
fun SensLiquidCapsule(
    text: String,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
    isSelected: Boolean = false,
    accentColor: Color = Color(0xFFD4A373),
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(32.dp)
    val backgroundBrush = if (isSelected) {
        Brush.horizontalGradient(
            listOf(accentColor.copy(alpha = 0.35f), accentColor.copy(alpha = 0.15f))
        )
    } else {
        Brush.horizontalGradient(
            listOf(Color(0x1FFFFFFF), Color(0x0AFFFFFF))
        )
    }

    val borderBrush = if (isSelected) {
        Brush.horizontalGradient(
            listOf(accentColor.copy(alpha = 0.8f), accentColor.copy(alpha = 0.3f))
        )
    } else {
        Brush.verticalGradient(
            listOf(Color(0x3DFFFFFF), Color(0x14FFFFFF))
        )
    }

    val clickMod = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    } else Modifier

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundBrush)
            .border(1.dp, borderBrush, shape)
            .then(clickMod)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                style = SensTypography.labelMedium,
                color = if (isSelected) Color.White else SensTextPrimary,
                fontSize = 13.sp
            )
        }
    }
}

/**
 * Dynamic Island / Connection Capsule for instant watch status.
 */
@Composable
fun SensLiquidDynamicIsland(
    state: ConnectionState,
    batteryPercent: Int? = null,
    rssi: Int? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_anim"
    )

    val (statusColor, statusText) = when (state) {
        ConnectionState.CONNECTED -> SensEmerald to "Qore 2 Connected"
        ConnectionState.CONNECTING, ConnectionState.SCANNING -> SensCyan to "Connecting..."
        ConnectionState.SYNCING -> Color(0xFFD4A373) to "Syncing Data..."
        ConnectionState.DISCONNECTED -> Color(0xFF94A3B8) to "Watch Disconnected"
        ConnectionState.ERROR -> SensRose to "Connection Error"
    }

    Box(
        modifier = modifier
            .shadow(12.dp, CircleShape, spotColor = statusColor.copy(alpha = 0.3f))
            .clip(CircleShape)
            .background(Color(0xFF0D1222).copy(alpha = 0.92f))
            .border(
                1.dp,
                Brush.horizontalGradient(
                    listOf(
                        Color(0x4DFFFFFF),
                        statusColor.copy(alpha = 0.4f),
                        Color(0x1AFFFFFF)
                    )
                ),
                CircleShape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Live pulsing dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = if (state == ConnectionState.CONNECTED) 1.0f else pulseAlpha))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = statusText,
                style = SensTypography.labelSmall,
                color = Color.White,
                fontSize = 12.sp
            )
            if (batteryPercent != null && state == ConnectionState.CONNECTED) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "•  ${batteryPercent}%",
                    style = SensTypography.labelSmall,
                    color = Color(0xFFD4A373),
                    fontSize = 12.sp
                )
            }
        }
    }
}
