package com.dttrackpro.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

data class DonutSlice(val value: Float, val color: Color)

@Composable
fun StatDonut(
    slices: List<DonutSlice>,
    modifier: Modifier = Modifier,
    strokeWidthDp: Float = 16f,
    trackColor: Color = Color(0xFF1A2B3F),
    centerContent: (@Composable () -> Unit)? = null,
) {
    val total = slices.sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(0.0001f)

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(140.dp)) {
            val stroke = Stroke(width = strokeWidthDp, cap = StrokeCap.Round)
            val diameter = size.minDimension - strokeWidthDp
            val topLeft = androidx.compose.ui.geometry.Offset(
                (size.width - diameter) / 2f,
                (size.height - diameter) / 2f
            )
            val arcSize = Size(diameter, diameter)

            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke
            )

            var startAngle = -90f
            slices.forEach { slice ->
                val sweep = (slice.value / total) * 360f
                if (sweep > 0f) {
                    drawArc(
                        color = slice.color,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = stroke
                    )
                    startAngle += sweep
                }
            }
        }
        centerContent?.invoke()
    }
}
