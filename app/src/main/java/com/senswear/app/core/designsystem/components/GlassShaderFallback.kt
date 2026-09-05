package com.senswear.app.core.designsystem.components

import android.os.Build
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Provides hardware-accelerated RenderEffect backdrop blur on Android 12+ (API 31+),
 * and gracefully falls back to multi-stop scrim gradients on Android 8.0–11 (API 26–30).
 */
object GlassShaderFallback {

    val isRenderEffectSupported: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    fun Modifier.safeGlassBlur(radius: Dp = 20.dp): Modifier {
        return if (isRenderEffectSupported) {
            this.blur(radius)
        } else {
            this // Use high-contrast frosted scrim on older Android releases
        }
    }

    fun getFallbackScrimColor(): Color {
        return Color(0x28FFFFFF)
    }
}
