package com.senswear.app.navigation

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senswear.app.core.designsystem.components.SensBottomBar
import com.senswear.app.core.designsystem.components.SensNavTab
import com.senswear.app.core.designsystem.theme.SensGlassBorderHighlight
import com.senswear.app.core.designsystem.theme.SensObsidian
import com.senswear.app.core.designsystem.theme.SensTextMuted
import com.senswear.app.core.designsystem.theme.SensTextPrimary
import com.senswear.app.core.designsystem.theme.SensTypography
import com.senswear.app.feature.activity.ActivityScreen
import com.senswear.app.feature.activity.ActivityViewModel
import com.senswear.app.feature.device.DeviceScreen
import com.senswear.app.feature.device.DeviceViewModel
import com.senswear.app.feature.health.HealthHubScreen
import com.senswear.app.feature.health.HealthViewModel
import com.senswear.app.feature.home.HomeScreen
import com.senswear.app.feature.home.HomeViewModel
import com.senswear.app.feature.onboarding.OnboardingScreen
import com.senswear.app.feature.onboarding.OnboardingViewModel
import com.senswear.app.feature.settings.SettingsScreen
import com.senswear.app.feature.settings.SettingsViewModel
import com.senswear.app.feature.sleep.SleepScreen
import com.senswear.app.feature.sleep.SleepViewModel
import com.senswear.app.feature.workouts.WorkoutsScreen
import com.senswear.app.feature.workouts.WorkoutsViewModel

@Composable
fun SenswearNavHost(
    homeViewModel: HomeViewModel,
    activityViewModel: ActivityViewModel,
    healthViewModel: HealthViewModel,
    sleepViewModel: SleepViewModel,
    workoutsViewModel: WorkoutsViewModel,
    deviceViewModel: DeviceViewModel,
    settingsViewModel: SettingsViewModel,
    onboardingViewModel: OnboardingViewModel,
    modifier: Modifier = Modifier
) {
    var isOnboardingCompleted by remember { mutableStateOf(true) }
    var currentTab by remember { mutableStateOf(SensNavTab.HOME) }

    val config = LocalConfiguration.current
    val isTabletOrFoldable = config.screenWidthDp >= 600

    if (!isOnboardingCompleted) {
        OnboardingScreen(
            viewModel = onboardingViewModel,
            onFinish = { isOnboardingCompleted = true }
        )
    } else {
        if (isTabletOrFoldable) {
            // Material 3 Adaptive Layout: Liquid Glass Navigation Rail
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
                                onClick = { currentTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.title
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.title,
                                        style = SensTypography.labelSmall,
                                        fontSize = 10.sp
                                    )
                                },
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
                    when (currentTab) {
                        SensNavTab.HOME -> HomeScreen(
                            viewModel = homeViewModel,
                            onNavigateToActivity = { currentTab = SensNavTab.ACTIVITY },
                            onNavigateToHealth = { currentTab = SensNavTab.HEALTH },
                            onNavigateToSleep = { currentTab = SensNavTab.SLEEP },
                            onNavigateToWorkouts = { currentTab = SensNavTab.WORKOUTS },
                            onNavigateToSettings = { /* open settings */ }
                        )
                        SensNavTab.ACTIVITY -> ActivityScreen(viewModel = activityViewModel)
                        SensNavTab.HEALTH -> HealthHubScreen(viewModel = healthViewModel)
                        SensNavTab.SLEEP -> SleepScreen(viewModel = sleepViewModel)
                        SensNavTab.WORKOUTS -> WorkoutsScreen(viewModel = workoutsViewModel)
                        SensNavTab.DEVICE -> DeviceScreen(viewModel = deviceViewModel)
                    }
                }
            }
        } else {
            // Mobile Layout: Floating Bottom Liquid Glass Capsule
            Box(modifier = modifier.fillMaxSize()) {
                when (currentTab) {
                    SensNavTab.HOME -> HomeScreen(
                        viewModel = homeViewModel,
                        onNavigateToActivity = { currentTab = SensNavTab.ACTIVITY },
                        onNavigateToHealth = { currentTab = SensNavTab.HEALTH },
                        onNavigateToSleep = { currentTab = SensNavTab.SLEEP },
                        onNavigateToWorkouts = { currentTab = SensNavTab.WORKOUTS },
                        onNavigateToSettings = { /* open settings */ }
                    )
                    SensNavTab.ACTIVITY -> ActivityScreen(viewModel = activityViewModel)
                    SensNavTab.HEALTH -> HealthHubScreen(viewModel = healthViewModel)
                    SensNavTab.SLEEP -> SleepScreen(viewModel = sleepViewModel)
                    SensNavTab.WORKOUTS -> WorkoutsScreen(viewModel = workoutsViewModel)
                    SensNavTab.DEVICE -> DeviceScreen(viewModel = deviceViewModel)
                }

                SensBottomBar(
                    currentTab = currentTab,
                    onTabSelected = { currentTab = it },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}
