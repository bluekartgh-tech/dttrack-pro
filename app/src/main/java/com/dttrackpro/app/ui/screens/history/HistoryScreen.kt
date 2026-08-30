package com.dttrackpro.app.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dttrackpro.app.ui.theme.*
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = viewModel(),
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Graphite900,
        topBar = {
            TopAppBar(
                title = { Text("Trip history", color = Cloud100) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Cloud100)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Graphite900)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Box(modifier = Modifier.weight(1f)) {
                HistoryMap(points = state.points.map { it.lat to it.lng }, playheadIndex = state.playheadIndex)
            }

            if (state.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = SignalCyan)
            }

            state.errorMessage?.let { msg ->
                Row(
                    modifier = Modifier.fillMaxWidth().background(DangerCoral.copy(alpha = 0.12f)).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Couldn't load history: $msg", style = MaterialTheme.typography.bodyMedium, color = Cloud100)
                }
            }

            PlaybackControls(state = state, viewModel = viewModel)
        }
    }
}

@Composable
private fun HistoryMap(points: List<Pair<Double, Double>>, playheadIndex: Int) {
    val context = LocalContext.current
    val mapView = remember { MapView(context).apply { setTileSource(TileSourceFactory.MAPNIK); setMultiTouchControls(true) } }
    val marker = remember { Marker(mapView) }
    val polyline = remember { Polyline().apply { outlinePaint.color = SignalCyan.toArgb(); outlinePaint.strokeWidth = 8f } }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            mapView.overlays.add(polyline)
            mapView.overlays.add(marker)
            mapView
        },
        update = { mv ->
            if (points.isNotEmpty()) {
                val geoPoints = points.map { GeoPoint(it.first, it.second) }
                polyline.setPoints(geoPoints)
                val current = geoPoints.getOrNull(playheadIndex) ?: geoPoints.first()
                marker.position = current
                if (mv.zoomLevelDouble < 5.0) {
                    mv.controller.setZoom(15.0)
                    mv.controller.setCenter(current)
                } else {
                    mv.controller.animateTo(current)
                }
            }
            mv.invalidate()
        }
    )
}

@Composable
private fun PlaybackControls(state: HistoryUiState, viewModel: HistoryViewModel) {
    Column(
        modifier = Modifier.fillMaxWidth().background(Graphite800).padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = viewModel::togglePlay,
                modifier = Modifier
                    .background(SignalCyan, MaterialTheme.shapes.large)
            ) {
                Icon(
                    if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = Graphite900
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    state.currentPoint?.let { "${it.speed.toInt()} km/h  •  ${it.timestamp}" } ?: "No data",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate300
                )
                Slider(
                    value = state.playheadIndex.toFloat(),
                    onValueChange = { viewModel.seekTo(it.toInt()) },
                    valueRange = 0f..(state.points.size - 1).coerceAtLeast(0).toFloat(),
                    colors = SliderDefaults.colors(thumbColor = SignalCyan, activeTrackColor = SignalCyan)
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(0.5f, 1f, 2f, 4f).forEach { speed ->
                FilterChip(
                    selected = state.playbackSpeed == speed,
                    onClick = { viewModel.setSpeed(speed) },
                    label = { Text("${speed}x") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SignalCyan.copy(alpha = 0.18f),
                        selectedLabelColor = SignalCyan,
                        containerColor = Graphite700,
                        labelColor = Slate300,
                    )
                )
            }
        }
    }
}
