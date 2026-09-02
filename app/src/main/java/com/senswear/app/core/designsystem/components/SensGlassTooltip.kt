package com.senswear.app.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senswear.app.core.designsystem.theme.SensGlassBgHeavy
import com.senswear.app.core.designsystem.theme.SensGlassBorderHighlight
import com.senswear.app.core.designsystem.theme.SensTextPrimary
import com.senswear.app.core.designsystem.theme.SensTypography

@Composable
fun SensGlassTooltip(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SensGlassBgHeavy)
            .border(1.dp, SensGlassBorderHighlight, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = message,
            style = SensTypography.labelSmall,
            color = SensTextPrimary,
            fontSize = 11.sp
        )
    }
}
