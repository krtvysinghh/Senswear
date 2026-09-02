package com.senswear.app.core.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "senswear_user_prefs")

data class UserSettings(
    val dailyStepGoal: Int = 10000,
    val unitSystem: String = "METRIC",
    val isHapticsEnabled: Boolean = true,
    val isAutoSyncEnabled: Boolean = true
)

class UserPreferencesRepository(private val context: Context) {
    private object Keys {
        val STEP_GOAL = intPreferencesKey("step_goal")
        val UNIT_SYSTEM = stringPreferencesKey("unit_system")
        val HAPTICS = booleanPreferencesKey("haptics_enabled")
        val AUTO_SYNC = booleanPreferencesKey("auto_sync_enabled")
    }

    val userSettings: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        UserSettings(
            dailyStepGoal = prefs[Keys.STEP_GOAL] ?: 10000,
            unitSystem = prefs[Keys.UNIT_SYSTEM] ?: "METRIC",
            isHapticsEnabled = prefs[Keys.HAPTICS] ?: true,
            isAutoSyncEnabled = prefs[Keys.AUTO_SYNC] ?: true
        )
    }

    suspend fun updateStepGoal(goal: Int) {
        context.dataStore.edit { it[Keys.STEP_GOAL] = goal }
    }

    suspend fun updateUnitSystem(system: String) {
        context.dataStore.edit { it[Keys.UNIT_SYSTEM] = system }
    }
}
