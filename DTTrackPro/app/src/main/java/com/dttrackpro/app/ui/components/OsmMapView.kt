package com.dttrackpro.app.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.dttrackpro.app.data.model.Device
import com.dttrackpro.app.data.model.DeviceStatus
import com.dttrackpro.app.ui.theme.OfflineGrey
import com.dttrackpro.app.ui.theme.OnlineGreen
import com.dttrackpro.app.ui.theme.SignalCyan
import com.dttrackpro.app.util.AnimatedVehicleMarker
import kotlinx.coroutines.CoroutineScope
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

/**
 * Live map surface: one AnimatedVehicleMarker per device, smoothly tweened
 * on every telemetry update. Also draws simple circular geofences.
 */
@Composable
fun OsmMapView(
    devices: List<Device>,
    selectedDeviceId: Long?,
    onVehicleTapped: (Long) -> Unit,
    followSelected: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val markers = remember { mutableMapOf<Long, AnimatedVehicleMarker>() }
    val mapViewRef = remember { mutableStateOf<MapView?>(null) }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(14.0)
                controller.setCenter(GeoPoint(19.076, 72.877))
                minZoomLevel = 4.0
                maxZoomLevel = 20.0

                setOnTouchListener { v, event ->
                    if (event.actionMasked == android.view.MotionEvent.ACTION_UP) {
                        val hit = markers.values.firstOrNull { it.hitTest(this, event.x, event.y) }
                        if (hit != null) onVehicleTapped(hit.deviceId)
                    }
                    false // let the map still handle pan/zoom
                }

                mapViewRef.value = this
            }
        },
        update = { mapView ->
            devices.forEach { device ->
                val point = GeoPoint(device.data.lat, device.data.lng)
                val existing = markers[device.id]
                if (existing == null) {
                    val marker = AnimatedVehicleMarker(
                        deviceId = device.id,
                        startPosition = point,
                        startHeading = device.data.course,
                        scope = scope,
                        colorProvider = { statusColor(device.data.status).toArgb() },
                        glowColorProvider = { statusColor(device.data.status).toArgb() },
                    )
                    markers[device.id] = marker
                    mapView.overlays.add(marker)
                } else {
                    existing.updateTarget(point, device.data.course)
                }
            }
            markers.values.forEach { it.selected = it.deviceId == selectedDeviceId }

            if (followSelected && selectedDeviceId != null) {
                markers[selectedDeviceId]?.let {
                    mapView.controller.animateTo(it.renderedPosition)
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

private fun statusColor(status: DeviceStatus) = when (status) {
    DeviceStatus.MOVING -> SignalCyan
    DeviceStatus.STOPPED -> OnlineGreen
    DeviceStatus.OFFLINE -> OfflineGrey
}
