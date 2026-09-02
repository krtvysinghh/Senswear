package com.senswear.app.core.designsystem.components

import android.view.HapticFeedbackConstants
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

class SensHapticController(private val view: android.view.View) {
    fun performLightImpact() {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    fun performSuccessFeedback() {
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    }
}

@Composable
fun rememberSensHapticController(): SensHapticController {
    val view = LocalView.current
    return remember(view) { SensHapticController(view) }
}
