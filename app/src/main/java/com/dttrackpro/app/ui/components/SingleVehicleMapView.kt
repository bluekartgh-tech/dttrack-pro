package com.dttrackpro.app.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.dttrackpro.app.data.model.Device
import com.dttrackpro.app.data.model.Geofence
import com.dttrackpro.app.data.model.TripPoint
import com.dttrackpro.app.ui.theme.SignalCyan
import com.dttrackpro.app.util.AnimatedVehicleMarker
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline

enum class MapTileMode { STANDARD, TOPOGRAPHIC }

private val topoTileSource = XYTileSource(
    "OpenTopoMap", 0, 17, 256, ".png",
    arrayOf("https://a.tile.opentopomap.org/", "https://b.tile.opentopomap.org/", "https://c.tile.opentopomap.org/")
)

@Composable
fun SingleVehicleMapView(
    device: Device?,
    tileMode: MapTileMode,
    trailPoints: List<TripPoint>,
    showTrail: Boolean,
    geofences: List<Geofence>,
    showGeofences: Boolean,
    modifier: Modifier = Modifier,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val marker = remember { mutableStateOf<AnimatedVehicleMarker?>(null) }
    val mapViewRef = remember { mutableStateOf<MapView?>(null) }
    val trailOverlay = remember { Polyline().apply { outlinePaint.color = SignalCyan.toArgb(); outlinePaint.strokeWidth = 6f } }
    val geofenceOverlays = remember { mutableListOf<Polygon>() }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(16.0)
                overlays.add(trailOverlay)
                mapViewRef.value = this
            }
        },
        update = { mapView ->
            mapView.setTileSource(if (tileMode == MapTileMode.TOPOGRAPHIC) topoTileSource else TileSourceFactory.MAPNIK)

            device?.let { d ->
                val point = GeoPoint(d.data.lat, d.data.lng)
                val existing = marker.value
                if (existing == null) {
                    val m = AnimatedVehicleMarker(
                        deviceId = d.id,
                        startPosition = point,
                        startHeading = d.data.course,
                        scope = scope,
                        colorProvider = { SignalCyan.toArgb() },
                        glowColorProvider = { SignalCyan.toArgb() },
                    ).also { it.selected = true }
                    marker.value = m
                    mapView.overlays.add(m)
                    mapView.controller.setCenter(point)
                } else {
                    existing.updateTarget(point, d.data.course)
                    mapView.controller.animateTo(existing.renderedPosition)
                }
            }

            trailOverlay.setPoints(if (showTrail) trailPoints.map { GeoPoint(it.lat, it.lng) } else emptyList())

            geofenceOverlays.forEach { mapView.overlays.remove(it) }
            geofenceOverlays.clear()
            if (showGeofences) {
                geofences.forEach { fence ->
                    if (fence.centerLat != null && fence.centerLng != null && fence.radiusMeters != null) {
                        val circle = Polygon(mapView).apply {
                            points = Polygon.pointsAsCircle(GeoPoint(fence.centerLat, fence.centerLng), fence.radiusMeters)
                            fillColor = 0x2200D9C0
                            strokeColor = SignalCyan.toArgb()
                            strokeWidth = 3f
                        }
                        geofenceOverlays.add(circle)
                        mapView.overlays.add(circle)
                    }
                }
            }

            mapView.invalidate()
        }
    )

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapViewRef.value?.onResume()
                Lifecycle.Event.ON_PAUSE -> mapViewRef.value?.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}
