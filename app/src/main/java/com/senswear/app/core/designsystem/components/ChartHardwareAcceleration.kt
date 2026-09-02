package com.senswear.app.core.designsystem.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

fun Modifier.sensChartRenderLayer(): Modifier = this.graphicsLayer {
    clip = true
    renderEffect = null
}
