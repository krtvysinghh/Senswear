package com.senswear.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.senswear.app.core.designsystem.theme.SensObsidian
import com.senswear.app.core.designsystem.theme.SenswearTheme
import com.senswear.app.feature.activity.ActivityViewModel
import com.senswear.app.feature.device.DeviceViewModel
import com.senswear.app.feature.health.HealthViewModel
import com.senswear.app.feature.home.HomeViewModel
import com.senswear.app.feature.onboarding.OnboardingViewModel
import com.senswear.app.feature.settings.SettingsViewModel
import com.senswear.app.feature.sleep.SleepViewModel
import com.senswear.app.feature.workouts.WorkoutsViewModel
import com.senswear.app.navigation.SenswearNavHost

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as SenswearApp

        val homeViewModel = HomeViewModel(
            wearableConnector = app.wearableConnector,
            activityRepository = app.activityRepository,
            healthRepository = app.healthRepository,
            sleepRepository = app.sleepRepository,
            insightsRepository = app.insightsRepository
        )

        val activityViewModel = ActivityViewModel(
            activityRepository = app.activityRepository,
            goalRepository = app.goalRepository
        )

        val healthViewModel = HealthViewModel(
            wearableConnector = app.wearableConnector,
            healthRepository = app.healthRepository
        )

        val sleepViewModel = SleepViewModel(
            sleepRepository = app.sleepRepository
        )

        val workoutsViewModel = WorkoutsViewModel(
            wearableConnector = app.wearableConnector,
            workoutRepository = app.workoutRepository
        )

        val deviceViewModel = DeviceViewModel(
            context = app,
            wearableConnector = app.wearableConnector
        )

        val settingsViewModel = SettingsViewModel(
            goalRepository = app.goalRepository
        )

        val onboardingViewModel = OnboardingViewModel(
            wearableConnector = app.wearableConnector
        )

        setContent {
            SenswearTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = SensObsidian
                ) {
                    SenswearNavHost(
                        homeViewModel = homeViewModel,
                        activityViewModel = activityViewModel,
                        healthViewModel = healthViewModel,
                        sleepViewModel = sleepViewModel,
                        workoutsViewModel = workoutsViewModel,
                        deviceViewModel = deviceViewModel,
                        settingsViewModel = settingsViewModel,
                        onboardingViewModel = onboardingViewModel
                    )
                }
            }
        }
    }
}
