package com.senswear.app.core.domain.model

data class Goal(
    val id: String,
    val title: String,
    val targetValue: Double,
    val unit: String,
    val currentValue: Double = 0.0,
    val type: GoalType = GoalType.DAILY_STEPS
) {
    val progressFraction: Float
        get() = if (targetValue > 0) (currentValue / targetValue).toFloat().coerceIn(0f, 1.5f) else 0f

    val isCompleted: Boolean
        get() = currentValue >= targetValue
}

enum class GoalType {
    DAILY_STEPS,
    DAILY_ACTIVE_CALORIES,
    DAILY_DISTANCE_KM,
    DAILY_ACTIVE_MINUTES,
    SLEEP_DURATION_HOURS
}

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val isUnlocked: Boolean = false,
    val unlockedEpochMs: Long? = null,
    val progressPercent: Int = 0,
    val iconName: String = "military_tech"
)

enum class InsightSeverity {
    POSITIVE,
    NEUTRAL,
    ATTENTION
}

data class HealthInsight(
    val id: String,
    val title: String,
    val message: String,
    val category: String,
    val severity: InsightSeverity = InsightSeverity.POSITIVE,
    val timestampEpochMs: Long = System.currentTimeMillis()
)

data class SyncEvent(
    val id: String,
    val timestampEpochMs: Long = System.currentTimeMillis(),
    val source: DataSource,
    val recordsSynced: Int,
    val status: String,
    val durationMs: Long,
    val errorMessage: String? = null
)
