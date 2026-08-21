package com.dttrackpro.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DTColorScheme = darkColorScheme(
    primary = SignalCyan,
    onPrimary = Graphite900,
    secondary = AlertAmber,
    onSecondary = Graphite900,
    background = Graphite900,
    onBackground = Cloud100,
    surface = Graphite800,
    onSurface = Cloud100,
    surfaceVariant = Graphite700,
    onSurfaceVariant = Slate300,
    outline = Graphite600,
    error = DangerCoral,
    onError = Color.White,
)

private val DTShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun DTTrackProTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DTColorScheme,
        typography = DTTypography,
        shapes = DTShapes,
        content = content
    )
}
