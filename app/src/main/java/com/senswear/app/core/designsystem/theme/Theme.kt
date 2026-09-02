package com.senswear.app.core.designsystem.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = SensCyan,
    onPrimary = SensObsidian,
    primaryContainer = SensDeepNavy,
    onPrimaryContainer = SensCyan,
    secondary = SensIndigo,
    onSecondary = SensTextPrimary,
    background = SensObsidian,
    onBackground = SensTextPrimary,
    surface = SensSurfaceDark,
    onSurface = SensTextPrimary,
    surfaceVariant = SensSurfaceCard,
    onSurfaceVariant = SensTextSecondary,
    error = SensDanger,
    onError = SensTextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = SensCyan,
    onPrimary = SensObsidian,
    primaryContainer = SensDeepNavy,
    onPrimaryContainer = SensCyan,
    secondary = SensIndigo,
    onSecondary = SensTextPrimary,
    background = SensObsidian, // Senswear is dark-first by design
    onBackground = SensTextPrimary,
    surface = SensSurfaceDark,
    onSurface = SensTextPrimary,
    surfaceVariant = SensSurfaceCard,
    onSurfaceVariant = SensTextSecondary,
    error = SensDanger,
    onError = SensTextPrimary
)

@Composable
fun SenswearTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = SensObsidian.toArgb()
                window.navigationBarColor = SensObsidian.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SensTypography,
        content = content
    )
}
