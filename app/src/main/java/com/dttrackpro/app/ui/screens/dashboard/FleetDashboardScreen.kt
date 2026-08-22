package com.dttrackpro.app.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.SignalCellularOff
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.dttrackpro.app.data.model.DeviceStatus
import com.dttrackpro.app.ui.components.DonutSlice
import com.dttrackpro.app.ui.components.StatDonut
import com.dttrackpro.app.ui.main.FleetViewModel
import com.dttrackpro.app.ui.theme.*

@Composable
fun FleetDashboardScreen(
    fleetViewModel: FleetViewModel,
    onVehicleTapped: (Long) -> Unit,
) {
    val state by fleetViewModel.uiState.collectAsState()
    val moving = state.counts[com.dttrackpro.app.ui.main.FleetFilter.MOVING] ?: 0
    val stopped = state.counts[com.dttrackpro.app.ui.main.FleetFilter.STOPPED] ?: 0
    val offline = state.counts[com.dttrackpro.app.ui.main.FleetFilter.OFFLINE] ?: 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Graphite900)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Text("Fleet overview", style = MaterialTheme.typography.headlineSmall, color = Cloud100)
        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile(Icons.Filled.DirectionsCar, "Total vehicles", state.devices.size.toString(), Modifier.weight(1f))
            StatTile(Icons.Filled.Speed, "Avg speed (moving)", "${state.averageSpeed.toInt()} km/h", Modifier.weight(1f))
        }

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth().background(Graphite800, MaterialTheme.shapes.large).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatDonut(
                slices = listOf(
                    DonutSlice(moving.toFloat(), SignalCyan),
                    DonutSlice(stopped.toFloat(), OnlineGreen),
                    DonutSlice(offline.toFloat(), OfflineGrey),
                ),
                centerContent = {
                    Text("${state.devices.size}", style = MaterialTheme.typography.headlineSmall, color = Cloud100)
                }
            )
            Spacer(Modifier.width(20.dp))
            Column {
                LegendRow(SignalCyan, "Moving", moving)
                Spacer(Modifier.height(10.dp))
                LegendRow(OnlineGreen, "Stopped", stopped)
                Spacer(Modifier.height(10.dp))
                LegendRow(OfflineGrey, "Offline", offline)
            }
        }

        Spacer(Modifier.height(24.dp))

        Text("Needs attention", style = MaterialTheme.typography.titleMedium, color = Cloud100)
        Spacer(Modifier.height(10.dp))

        val offlineDevices = state.devices.filter { it.data.status == DeviceStatus.OFFLINE }
        if (offlineDevices.isEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().background(Graphite800, MaterialTheme.shapes.medium).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.WifiTethering, contentDescription = null, tint = OnlineGreen)
                Spacer(Modifier.width(12.dp))
                Text("Whole fleet is reporting normally.", style = MaterialTheme.typography.bodyMedium, color = Slate300)
            }
        } else {
            offlineDevices.forEach { device ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Graphite800, MaterialTheme.shapes.medium)
                        .clickable { onVehicleTapped(device.id) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.SignalCellularOff, contentDescription = null, tint = DangerCoral)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(device.name, style = MaterialTheme.typography.titleSmall, color = Cloud100)
                        Text("No signal — last known position on file", style = MaterialTheme.typography.bodyMedium, color = Slate500)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun StatTile(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.background(Graphite800, MaterialTheme.shapes.large).padding(16.dp)
    ) {
        Icon(icon, contentDescription = null, tint = SignalCyan, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(10.dp))
        Text(value, style = MaterialTheme.typography.headlineSmall, color = Cloud100)
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Slate500)
    }
}

@Composable
private fun LegendRow(color: androidx.compose.ui.graphics.Color, label: String, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).background(color, androidx.compose.foundation.shape.CircleShape))
        Spacer(Modifier.width(8.dp))
        Text("$label — $count", style = MaterialTheme.typography.bodyMedium, color = Slate300)
    }
}
