package com.senswear.app.core.designsystem.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Obsidian & Dark Canvas Foundation
val SensObsidian = Color(0xFF090D16)
val SensDeepNavy = Color(0xFF0F172A)
val SensSurfaceDark = Color(0xFF131C2E)
val SensSurfaceCard = Color(0xFF1E293B)

// Translucent Glass Tints
val SensGlassBg = Color(0x1CFFFFFF)
val SensGlassBgSubtle = Color(0x0EFFFFFF)
val SensGlassBgHeavy = Color(0x33FFFFFF)
val SensGlassBorder = Color(0x28FFFFFF)
val SensGlassBorderHighlight = Color(0x4DFFFFFF)

// Vivid Glowing Accents
val SensCyan = Color(0xFF06B6D4)
val SensEmerald = Color(0xFF10B981)
val SensIndigo = Color(0xFF6366F1)
val SensViolet = Color(0xFFA855F7)
val SensRose = Color(0xFFF43F5E)
val SensAmber = Color(0xFFF59E0B)
val SensBlue = Color(0xFF3B82F6)

// Text Colors
val SensTextPrimary = Color(0xFFF8FAFC)
val SensTextSecondary = Color(0xFF94A3B8)
val SensTextTertiary = Color(0xFF64748B)

// Status
val SensSuccess = Color(0xFF10B981)
val SensWarning = Color(0xFFF59E0B)
val SensDanger = Color(0xFFEF4444)
val SensInfo = Color(0xFF38BDF8)

// Gradients
val SensGlassGradient = Brush.verticalGradient(
    colors = listOf(Color(0x24FFFFFF), Color(0x0AFFFFFF))
)

val SensCyanGlow = Brush.horizontalGradient(
    colors = listOf(Color(0xFF06B6D4), Color(0xFF3B82F6))
)

val SensEmeraldGlow = Brush.horizontalGradient(
    colors = listOf(Color(0xFF10B981), Color(0xFF059669))
)

val SensRoseGlow = Brush.horizontalGradient(
    colors = listOf(Color(0xFFF43F5E), Color(0xFFE11D48))
)

val SensIndigoVioletGlow = Brush.horizontalGradient(
    colors = listOf(Color(0xFF6366F1), Color(0xFFA855F7))
)
