package com.senswear.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.senswear.app.core.designsystem.components.SensBottomBar
import com.senswear.app.core.designsystem.components.SensNavTab
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

    if (!isOnboardingCompleted) {
        OnboardingScreen(
            viewModel = onboardingViewModel,
            onFinish = { isOnboardingCompleted = true }
        )
    } else {
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
