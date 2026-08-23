package com.dttrackpro.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dttrackpro.app.ui.theme.CardBorder
import com.dttrackpro.app.ui.theme.Graphite800
import com.dttrackpro.app.ui.theme.Graphite900
import com.dttrackpro.app.ui.theme.SignalCyan
import com.dttrackpro.app.ui.theme.SwitchThumbOff
import com.dttrackpro.app.ui.theme.SwitchTrackOff

fun Modifier.dtCard(
    fill: androidx.compose.ui.graphics.Color = Graphite800,
    radius: androidx.compose.ui.unit.Dp = 16.dp,
): Modifier {
    val shape = RoundedCornerShape(radius)
    return this
        .background(fill, shape)
        .border(1.dp, CardBorder, shape)
}

@Composable
fun dtSwitchColors() = SwitchDefaults.colors(
    checkedTrackColor = SignalCyan,
    checkedThumbColor = Graphite900,
    checkedBorderColor = SignalCyan,
    uncheckedTrackColor = SwitchTrackOff,
    uncheckedThumbColor = SwitchThumbOff,
    uncheckedBorderColor = SwitchTrackOff,
)
