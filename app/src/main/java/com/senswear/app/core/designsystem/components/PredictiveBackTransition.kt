package com.senswear.app.core.designsystem.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Provides smooth scale and alpha transitions during Android 13+ predictive back gestures.
 */
@Composable
fun Modifier.predictiveBackScale(progress: Float): Modifier {
    val scale by animateFloatAsState(
        targetValue = 1.0f - (progress * 0.10f).coerceIn(0.0f, 0.10f),
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "predictive_back_scale"
    )

    val alpha by animateFloatAsState(
        targetValue = 1.0f - (progress * 0.20f).coerceIn(0.0f, 0.20f),
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "predictive_back_alpha"
    )

    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
        this.alpha = alpha
    }
}
