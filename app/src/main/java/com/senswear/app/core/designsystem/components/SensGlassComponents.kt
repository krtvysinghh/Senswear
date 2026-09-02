package com.senswear.app.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senswear.app.core.designsystem.theme.SensCyan
import com.senswear.app.core.designsystem.theme.SensCyanGlow
import com.senswear.app.core.designsystem.theme.SensGlassBg
import com.senswear.app.core.designsystem.theme.SensGlassBgHeavy
import com.senswear.app.core.designsystem.theme.SensGlassBgSubtle
import com.senswear.app.core.designsystem.theme.SensGlassBorder
import com.senswear.app.core.designsystem.theme.SensGlassBorderHighlight
import com.senswear.app.core.designsystem.theme.SensObsidian
import com.senswear.app.core.designsystem.theme.SensSurfaceCard
import com.senswear.app.core.designsystem.theme.SensTextPrimary
import com.senswear.app.core.designsystem.theme.SensTextSecondary
import com.senswear.app.core.designsystem.theme.SensTextTertiary
import com.senswear.app.core.designsystem.theme.SensTypography

@Composable
fun SensGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    backgroundColor: Color = SensGlassBg,
    borderColor: Color = SensGlassBorder,
    borderWidth: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor,
                        backgroundColor.copy(alpha = backgroundColor.alpha * 0.6f)
                    )
                )
            )
            .border(
                width = borderWidth,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        borderColor,
                        borderColor.copy(alpha = 0.08f)
                    )
                ),
                shape = shape
            ),
        content = content
    )
}

@Composable
fun SensGlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(24.dp),
    accentGlow: Color? = null,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    val clickModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else Modifier

    Box(
        modifier = modifier
            .then(clickModifier)
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x1F243B55),
                        Color(0x14141E30)
                    )
                )
            )
            .drawBehind {
                if (accentGlow != null) {
                    drawCircle(
                        color = accentGlow.copy(alpha = 0.12f),
                        radius = size.width * 0.4f,
                        center = Offset(size.width * 0.85f, size.height * 0.15f)
                    )
                }
            }
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        SensGlassBorderHighlight,
                        SensGlassBorder.copy(alpha = 0.1f)
                    )
                ),
                shape = shape
            )
            .padding(18.dp)
    ) {
        Column(content = content)
    }
}

@Composable
fun SensGlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isPrimary: Boolean = true,
    enabled: Boolean = true
) {
    val bgBrush = if (isPrimary) {
        SensCyanGlow
    } else {
        Brush.linearGradient(listOf(SensGlassBgHeavy, SensGlassBg))
    }

    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bgBrush)
            .clickable(enabled = enabled, onClick = onClick)
            .border(
                width = 1.dp,
                color = if (isPrimary) Color.Transparent else SensGlassBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isPrimary) SensObsidian else SensTextPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = SensTypography.labelLarge,
                color = if (isPrimary) SensObsidian else SensTextPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SensGlassChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isSelected) SensCyan.copy(alpha = 0.2f) else SensGlassBgSubtle
    val border = if (isSelected) SensCyan else SensGlassBorder

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = SensTypography.labelSmall,
            color = if (isSelected) SensCyan else SensTextSecondary,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun SensMetricLarge(
    value: String,
    unit: String,
    label: String,
    modifier: Modifier = Modifier,
    accentColor: Color = SensCyan
) {
    Column(modifier = modifier) {
        Text(
            text = label.uppercase(),
            style = SensTypography.labelSmall,
            color = SensTextSecondary,
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = value,
                style = SensTypography.displayMedium,
                color = SensTextPrimary,
                fontWeight = FontWeight.Bold
            )
            if (unit.isNotBlank()) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = unit,
                    style = SensTypography.titleMedium,
                    color = accentColor,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
        }
    }
}

@Composable
fun SensProgressRing(
    progress: Float, // 0f..1f
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 10.dp,
    trackColor: Color = Color(0x18FFFFFF),
    progressBrush: Brush = SensCyanGlow,
    centerContent: @Composable () -> Unit = {}
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1000),
        label = "progress_ring"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { 1f },
            modifier = Modifier.matchParentSize(),
            color = trackColor,
            strokeWidth = strokeWidth,
            strokeCap = StrokeCap.Round,
            trackColor = Color.Transparent
        )
        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.matchParentSize(),
            color = SensCyan,
            strokeWidth = strokeWidth,
            strokeCap = StrokeCap.Round,
            trackColor = Color.Transparent
        )
        centerContent()
    }
}
