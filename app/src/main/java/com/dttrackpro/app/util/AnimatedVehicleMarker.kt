package com.dttrackpro.app.util

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay
import kotlin.math.*

/**
 * Draws one vehicle as a rotated chevron + status-colored halo, and smoothly
 * tweens both position and heading whenever a new GPS fix arrives — instead
 * of the marker "teleporting" every poll cycle like a naive implementation.
 *
 * This is the piece that gives DTTrack Pro its signature smooth-tracking
 * feel: call updateTarget() whenever fresh telemetry comes in, and the
 * overlay animates itself on the map's own draw loop.
 */
class AnimatedVehicleMarker(
    val deviceId: Long,
    startPosition: GeoPoint,
    startHeading: Float,
    private val scope: CoroutineScope,
    private val colorProvider: () -> Int,
    private val glowColorProvider: () -> Int,
) : Overlay() {

    // Animated (rendered) state
    var renderedPosition: GeoPoint = startPosition
        private set
    var renderedHeading: Float = startHeading
        private set

    private var animJob: Job? = null
    var selected: Boolean = false

    private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    /** Kick off a smooth tween from the current rendered state to a new fix. */
    fun updateTarget(target: GeoPoint, targetHeading: Float, durationMs: Long = 2500L) {
        animJob?.cancel()
        val startPos = renderedPosition
        val startHead = renderedHeading
        val headingDelta = shortestAngleDelta(startHead, targetHeading)

        animJob = scope.launch {
            val startTime = System.currentTimeMillis()
            while (true) {
                val elapsed = System.currentTimeMillis() - startTime
                val rawT = (elapsed.toFloat() / durationMs).coerceIn(0f, 1f)
                val t = easeInOutCubic(rawT)

                renderedPosition = GeoPoint(
                    lerp(startPos.latitude, target.latitude, t.toDouble()),
                    lerp(startPos.longitude, target.longitude, t.toDouble())
                )
                renderedHeading = (startHead + headingDelta * t + 360f) % 360f

                if (rawT >= 1f) break
                awaitFrame()
            }
        }
    }

    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        if (shadow) return
        val point = mapView.projection.toPixels(renderedPosition, null)

        val haloRadius = if (selected) 34f else 24f
        haloPaint.color = glowColorProvider()
        haloPaint.alpha = if (selected) 70 else 45
        canvas.drawCircle(point.x.toFloat(), point.y.toFloat(), haloRadius, haloPaint)

        if (selected) {
            ringPaint.color = glowColorProvider()
            canvas.drawCircle(point.x.toFloat(), point.y.toFloat(), haloRadius, ringPaint)
        }

        canvas.save()
        canvas.rotate(renderedHeading, point.x.toFloat(), point.y.toFloat())

        bodyPaint.color = colorProvider()
        val size = 16f
        val chevron = Path().apply {
            moveTo(point.x.toFloat(), point.y - size)
            lineTo(point.x + size * 0.62f, point.y + size * 0.72f)
            lineTo(point.x.toFloat(), point.y + size * 0.28f)
            lineTo(point.x - size * 0.62f, point.y + size * 0.72f)
            close()
        }
        canvas.drawPath(chevron, bodyPaint)
        canvas.restore()
    }

    fun hitTest(mapView: MapView, screenX: Float, screenY: Float, tolerancePx: Float = 40f): Boolean {
        val point = mapView.projection.toPixels(renderedPosition, null)
        val dx = point.x - screenX
        val dy = point.y - screenY
        return sqrt(dx * dx + dy * dy) <= tolerancePx
    }

    private fun lerp(a: Double, b: Double, t: Double) = a + (b - a) * t

    private fun easeInOutCubic(t: Float): Float =
        if (t < 0.5f) 4 * t * t * t else 1 - (-2 * t + 2).pow(3) / 2

    private fun shortestAngleDelta(from: Float, to: Float): Float {
        var delta = (to - from) % 360f
        if (delta > 180f) delta -= 360f
        if (delta < -180f) delta += 360f
        return delta
    }
}
