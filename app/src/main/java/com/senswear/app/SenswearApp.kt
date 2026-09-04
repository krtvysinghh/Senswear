package com.senswear.app

import android.app.Application
import com.senswear.app.core.ble.UniversalBleConnector
import com.senswear.app.core.ble.WearableConnector
import com.senswear.app.core.data.local.SenswearDatabase
import com.senswear.app.core.data.repository.AchievementRepository
import com.senswear.app.core.data.repository.ActivityRepository
import com.senswear.app.core.data.repository.GoalRepository
import com.senswear.app.core.data.repository.HealthRepository
import com.senswear.app.core.data.repository.InsightsRepository
import com.senswear.app.core.data.repository.SleepRepository
import com.senswear.app.core.data.repository.WorkoutRepository
import com.senswear.app.core.healthconnect.HealthConnectManager

class SenswearApp : Application() {

    lateinit var database: SenswearDatabase
        private set

    lateinit var wearableConnector: WearableConnector
        private set

    lateinit var healthConnectManager: HealthConnectManager
        private set

    lateinit var activityRepository: ActivityRepository
        private set

    lateinit var healthRepository: HealthRepository
        private set

    lateinit var sleepRepository: SleepRepository
        private set

    lateinit var workoutRepository: WorkoutRepository
        private set

    lateinit var goalRepository: GoalRepository
        private set

    lateinit var achievementRepository: AchievementRepository
        private set

    lateinit var insightsRepository: InsightsRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = SenswearDatabase.getInstance(this)
        healthConnectManager = HealthConnectManager(this)

        // Universal Wearable Engine:
        // Connects to Apple Watch, Samsung Galaxy Watch, Whoop 4.0, Pebble Qore 2, Garmin, Fitbit, etc.
        wearableConnector = UniversalBleConnector(this)

        activityRepository = ActivityRepository(database)
        healthRepository = HealthRepository(database)
        sleepRepository = SleepRepository(database)
        workoutRepository = WorkoutRepository(database)
        goalRepository = GoalRepository(database)
        achievementRepository = AchievementRepository(database)
        insightsRepository = InsightsRepository()
    }

    companion object {
        lateinit var instance: SenswearApp
            private set
    }
}
