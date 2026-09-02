package com.senswear.app.core.domain.model

object StreakCalculator {
    fun calculateCurrentStreak(activities: List<DailyActivity>): Int {
        var streak = 0
        val sorted = activities.sortedByDescending { it.epochDay }

        for (activity in sorted) {
            if (activity.steps >= activity.stepGoal) {
                streak++
            } else {
                break
            }
        }
        return streak
    }
}
