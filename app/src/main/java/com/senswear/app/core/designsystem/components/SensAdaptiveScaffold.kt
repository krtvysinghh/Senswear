package com.senswear.app.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senswear.app.core.designsystem.theme.SensGlassBorderHighlight
import com.senswear.app.core.designsystem.theme.SensObsidian
import com.senswear.app.core.designsystem.theme.SensTextMuted
import com.senswear.app.core.designsystem.theme.SensTextPrimary
import com.senswear.app.core.designsystem.theme.SensTypography

enum class WindowSizeClass {
    COMPACT,
    MEDIUM,
    EXPANDED
}

@Composable
fun rememberWindowSizeClass(): WindowSizeClass {
    val config = LocalConfiguration.current
    val widthDp = config.screenWidthDp
    return when {
        widthDp < 600 -> WindowSizeClass.COMPACT
        widthDp < 840 -> WindowSizeClass.MEDIUM
        else -> WindowSizeClass.EXPANDED
    }
}

/**
 * Adaptive Liquid Glass Scaffold:
 * - Mobile (< 600dp): Floating bottom liquid glass capsule
 * - Tablet / Foldable (>= 600dp): Lateral frosted glass Navigation Rail
 */
@Composable
fun SensAdaptiveScaffold(
    currentTab: SensNavTab,
    onTabSelected: (SensNavTab) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (isWideScreen: Boolean) -> Unit
) {
    val sizeClass = rememberWindowSizeClass()
    val isWideScreen = sizeClass != WindowSizeClass.COMPACT

    if (isWideScreen) {
        // Tablet / Foldable layout: Lateral Liquid Glass Rail + Content
        Row(
            modifier = modifier
                .fillMaxSize()
                .background(SensObsidian)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(88.dp)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF0D1222).copy(alpha = 0.9f))
                    .border(
                        1.dp,
                        Brush.verticalGradient(
                            listOf(SensGlassBorderHighlight, Color(0x14FFFFFF))
                        ),
                        RoundedCornerShape(24.dp)
                    )
            ) {
                NavigationRail(
                    containerColor = Color.Transparent,
                    contentColor = SensTextPrimary,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    SensNavTab.entries.forEach { tab ->
                        val isSelected = currentTab == tab
                        NavigationRailItem(
                            selected = isSelected,
                            onClick = { onTabSelected(tab) },
                            icon = { Icon(tab.icon, contentDescription = tab.title) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = Color.White,
                                unselectedIconColor = SensTextMuted,
                                indicatorColor = Color(0x3300F0FF)
                            )
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                content(true)
            }
        }
    } else {
        // Mobile layout: Bottom Liquid Glass Bar
        Scaffold(
            bottomBar = {
                SensBottomBar(
                    currentTab = currentTab,
                    onTabSelected = onTabSelected
                )
            },
            containerColor = SensObsidian,
            modifier = modifier
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                content(false)
            }
        }
    }
}
