package com.senswear.app.feature.widget

/**
 * Data models for Android Home Screen Glance widgets:
 * - Liquid Recovery Ring Widget
 * - Live Heart Rate & Step Tracker
 * - Sleep Debt & Bedtime Reminder
 */
data class RecoveryWidgetState(
    val recoveryScore: Int,
    val rhrBpm: Int,
    val hrvMs: Int,
    val sleepHours: Double,
    val isDeviceConnected: Boolean,
    val lastSyncTimestampMs: Long
)

data class StepProgressWidgetState(
    val currentSteps: Int,
    val stepGoal: Int,
    val activeCaloriesKcal: Int,
    val progressFraction: Float
)
