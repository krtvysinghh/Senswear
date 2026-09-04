package com.senswear.app

import android.app.Application
import com.senswear.app.core.ble.WearableConnector
import com.senswear.app.core.data.local.SenswearDatabase
import com.senswear.app.core.data.repository.AchievementRepository
import com.senswear.app.core.data.repository.ActivityRepository
import com.senswear.app.core.data.repository.DataProvenanceRepository
import com.senswear.app.core.data.repository.GoalRepository
import com.senswear.app.core.data.repository.HealthRepository
import com.senswear.app.core.data.repository.InsightsRepository
import com.senswear.app.core.data.repository.SleepRepository
import com.senswear.app.core.data.repository.WorkoutRepository
import com.senswear.app.core.healthconnect.HealthConnectManager
import com.senswear.app.core.wearable.WearableManager
import com.senswear.app.core.wearable.cloud.CloudSyncManager

class SenswearApp : Application() {

    lateinit var database: SenswearDatabase
        private set

    lateinit var wearableConnector: WearableConnector
        private set

    lateinit var wearableManager: WearableManager
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

    lateinit var dataProvenanceRepository: DataProvenanceRepository
        private set

    lateinit var cloudSyncManager: CloudSyncManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = SenswearDatabase.getInstance(this)
        healthConnectManager = HealthConnectManager(this)

        // Production Truth: Universal Wearable Architecture
        wearableManager = WearableManager(this, healthConnectManager)
        wearableConnector = wearableManager

        activityRepository = ActivityRepository(database)
        healthRepository = HealthRepository(database)
        sleepRepository = SleepRepository(database)
        workoutRepository = WorkoutRepository(database)
        goalRepository = GoalRepository(database)
        achievementRepository = AchievementRepository(database)
        insightsRepository = InsightsRepository()
        dataProvenanceRepository = DataProvenanceRepository(database)
        cloudSyncManager = CloudSyncManager(healthRepository, activityRepository, dataProvenanceRepository)
    }

    companion object {
        lateinit var instance: SenswearApp
            private set
    }
}
