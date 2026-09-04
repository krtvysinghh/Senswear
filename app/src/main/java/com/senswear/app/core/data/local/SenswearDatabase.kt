package com.senswear.app.core.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.flow.MutableStateFlow

class SenswearDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "senswear.db"
        const val DATABASE_VERSION = 2

        @Volatile
        private var INSTANCE: SenswearDatabase? = null

        fun getInstance(context: Context): SenswearDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SenswearDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }

        fun createProvenanceTable(db: SQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS data_provenance_records (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    metric_name TEXT NOT NULL,
                    canonical_value REAL NOT NULL,
                    canonical_unit TEXT NOT NULL,
                    timestamp INTEGER NOT NULL,
                    start_time INTEGER,
                    end_time INTEGER,
                    source_device_name TEXT NOT NULL,
                    source_device_id TEXT NOT NULL,
                    source_vendor TEXT NOT NULL,
                    source_protocol TEXT NOT NULL,
                    data_quality TEXT NOT NULL,
                    confidence_score REAL NOT NULL,
                    is_estimated INTEGER NOT NULL,
                    sync_timestamp INTEGER NOT NULL,
                    raw_payload_fingerprint TEXT
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_provenance_metric_time ON data_provenance_records(metric_name, timestamp)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_provenance_device_time ON data_provenance_records(source_device_id, timestamp)")
        }
    }

    private val changeNotifier = MutableStateFlow(System.currentTimeMillis())

    fun notifyChanged() {
        changeNotifier.value = System.currentTimeMillis()
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE daily_activity (
                epoch_day INTEGER PRIMARY KEY,
                steps INTEGER NOT NULL,
                step_goal INTEGER NOT NULL,
                distance_meters REAL NOT NULL,
                active_calories INTEGER NOT NULL,
                total_calories INTEGER NOT NULL,
                active_minutes INTEGER NOT NULL,
                hourly_steps TEXT NOT NULL,
                source TEXT NOT NULL,
                last_updated INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX idx_activity_epoch_source ON daily_activity(epoch_day, source)")

        db.execSQL(
            """
            CREATE TABLE heart_rate_readings (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                timestamp INTEGER NOT NULL,
                bpm INTEGER NOT NULL,
                resting_bpm INTEGER,
                source TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX idx_hr_timestamp ON heart_rate_readings(timestamp)")

        db.execSQL(
            """
            CREATE TABLE spo2_readings (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                timestamp INTEGER NOT NULL,
                percentage INTEGER NOT NULL,
                source TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX idx_spo2_timestamp ON spo2_readings(timestamp)")

        db.execSQL(
            """
            CREATE TABLE hrv_readings (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                timestamp INTEGER NOT NULL,
                rmssd INTEGER NOT NULL,
                sdnn INTEGER,
                source TEXT NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE stress_readings (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                timestamp INTEGER NOT NULL,
                score INTEGER NOT NULL,
                source TEXT NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE temperature_readings (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                timestamp INTEGER NOT NULL,
                temp_celsius REAL NOT NULL,
                baseline_delta REAL NOT NULL,
                source TEXT NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE sleep_sessions (
                id TEXT PRIMARY KEY,
                start_time INTEGER NOT NULL,
                end_time INTEGER NOT NULL,
                duration_minutes INTEGER NOT NULL,
                deep_minutes INTEGER NOT NULL,
                light_minutes INTEGER NOT NULL,
                rem_minutes INTEGER NOT NULL,
                awake_minutes INTEGER NOT NULL,
                sleep_score INTEGER NOT NULL,
                source TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX idx_sleep_start ON sleep_sessions(start_time)")

        db.execSQL(
            """
            CREATE TABLE workouts (
                id TEXT PRIMARY KEY,
                type TEXT NOT NULL,
                start_time INTEGER NOT NULL,
                end_time INTEGER,
                duration_seconds INTEGER NOT NULL,
                distance_meters REAL NOT NULL,
                calories INTEGER NOT NULL,
                avg_hr INTEGER NOT NULL,
                max_hr INTEGER NOT NULL,
                source TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX idx_workout_start ON workouts(start_time)")

        db.execSQL(
            """
            CREATE TABLE goals (
                id TEXT PRIMARY KEY,
                title TEXT NOT NULL,
                target_value REAL NOT NULL,
                unit TEXT NOT NULL,
                current_value REAL NOT NULL,
                type TEXT NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE achievements (
                id TEXT PRIMARY KEY,
                title TEXT NOT NULL,
                description TEXT NOT NULL,
                category TEXT NOT NULL,
                is_unlocked INTEGER NOT NULL,
                unlocked_time INTEGER,
                progress_percent INTEGER NOT NULL,
                icon TEXT NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE sync_events (
                id TEXT PRIMARY KEY,
                timestamp INTEGER NOT NULL,
                source TEXT NOT NULL,
                records_synced INTEGER NOT NULL,
                status TEXT NOT NULL,
                duration_ms INTEGER NOT NULL,
                error_message TEXT
            )
            """.trimIndent()
        )

        // Version 2 Table: Historical Data Provenance
        createProvenanceTable(db)

        seedInitialGoalsAndAchievements(db)
    }

    private fun seedInitialGoalsAndAchievements(db: SQLiteDatabase) {
        val goals = listOf(
            ContentValues().apply {
                put("id", "goal_steps")
                put("title", "Daily Steps")
                put("target_value", 10000.0)
                put("unit", "steps")
                put("current_value", 0.0)
                put("type", "DAILY_STEPS")
            },
            ContentValues().apply {
                put("id", "goal_calories")
                put("title", "Active Calories")
                put("target_value", 450.0)
                put("unit", "kcal")
                put("current_value", 0.0)
                put("type", "DAILY_ACTIVE_CALORIES")
            },
            ContentValues().apply {
                put("id", "goal_sleep")
                put("title", "Nightly Sleep")
                put("target_value", 8.0)
                put("unit", "hours")
                put("current_value", 0.0)
                put("type", "SLEEP_DURATION_HOURS")
            }
        )
        for (g in goals) {
            db.insertWithOnConflict("goals", null, g, SQLiteDatabase.CONFLICT_REPLACE)
        }

        val achievements = listOf(
            ContentValues().apply {
                put("id", "ach_first_steps")
                put("title", "First Steps")
                put("description", "Record your first steps with your wearable companion")
                put("category", "Activity")
                put("is_unlocked", 0)
                putNull("unlocked_time")
                put("progress_percent", 0)
                put("icon", "directions_walk")
            },
            ContentValues().apply {
                put("id", "ach_10k_day")
                put("title", "10,000 Step Milestone")
                put("description", "Reach 10,000 steps in a single day")
                put("category", "Activity")
                put("is_unlocked", 0)
                putNull("unlocked_time")
                put("progress_percent", 0)
                put("icon", "military_tech")
            },
            ContentValues().apply {
                put("id", "ach_7day_streak")
                put("title", "7-Day Consistency")
                put("description", "Hit your step goal 7 days in a row")
                put("category", "Streak")
                put("is_unlocked", 0)
                putNull("unlocked_time")
                put("progress_percent", 0)
                put("icon", "local_fire_department")
            },
            ContentValues().apply {
                put("id", "ach_sleep_master")
                put("title", "Deep Recovery")
                put("description", "Record > 2 hours of Deep Sleep")
                put("category", "Sleep")
                put("is_unlocked", 0)
                putNull("unlocked_time")
                put("progress_percent", 0)
                put("icon", "bedtime")
            }
        )
        for (a in achievements) {
            db.insertWithOnConflict("achievements", null, a, SQLiteDatabase.CONFLICT_REPLACE)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            createProvenanceTable(db)
        }
    }
}
