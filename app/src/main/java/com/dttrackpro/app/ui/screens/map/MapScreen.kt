package com.dttrackpro.app.ui.screens.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
    onBellClick: () -> Unit,
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
            modifier = Modifier.align(Alignment.TopStart).statusBarsPadding().padding(12.dp),
            color = Graphite900.copy(alpha = 0.9f),
            shape = MaterialTheme.shapes.medium,
            shadowElevation = 4.dp,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LegendDot(SignalCyan, "${state.counts[com.dttrackpro.app.ui.main.FleetFilter.MOVING] ?: 0}")
                LegendDot(OnlineGreen, "${state.counts[com.dttrackpro.app.ui.main.FleetFilter.STOPPED] ?: 0}")
                LegendDot(OfflineGrey, "${state.counts[com.dttrackpro.app.ui.main.FleetFilter.OFFLINE] ?: 0}")
            }
        }

        Surface(
            modifier = Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(12.dp),
            color = Graphite900.copy(alpha = 0.9f),
            shape = CircleShape,
            shadowElevation = 4.dp,
        ) {
            IconButton(onClick = onBellClick, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = SignalCyan, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun LegendDot(color: androidx.compose.ui.graphics.Color, count: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(7.dp).background(color, CircleShape))
        Spacer(Modifier.width(5.dp))
        Text(count, style = MaterialTheme.typography.labelSmall, color = Slate300)
    }
}
