package com.dttrackpro.app.ui.screens.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dttrackpro.app.ui.components.OsmMapView
import com.dttrackpro.app.ui.main.FleetViewModel
import com.dttrackpro.app.ui.theme.*

@Composable
fun MapScreen(
    fleetViewModel: FleetViewModel,
    onVehicleTapped: (Long) -> Unit,
) {
    val state by fleetViewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        OsmMapView(
            devices = state.devices,
            selectedDeviceId = null,
            onVehicleTapped = onVehicleTapped,
            followSelected = false,
            modifier = Modifier.fillMaxSize(),
        )

        Surface(
            modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = 12.dp),
            color = Graphite800.copy(alpha = 0.92f),
            shape = MaterialTheme.shapes.large,
            shadowElevation = 6.dp,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LegendDot(SignalCyan, "${state.counts[com.dttrackpro.app.ui.main.FleetFilter.MOVING] ?: 0} moving")
                LegendDot(OnlineGreen, "${state.counts[com.dttrackpro.app.ui.main.FleetFilter.STOPPED] ?: 0} stopped")
                LegendDot(OfflineGrey, "${state.counts[com.dttrackpro.app.ui.main.FleetFilter.OFFLINE] ?: 0} offline")
            }
        }
    }
}

@Composable
private fun LegendDot(color: androidx.compose.ui.graphics.Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = Slate300)
    }
}
