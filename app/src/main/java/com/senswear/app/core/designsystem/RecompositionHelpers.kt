package com.senswear.app.core.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Immutable
data class ImmutableMetricHolder(
    val value: String,
    val unit: String,
    val label: String
)
