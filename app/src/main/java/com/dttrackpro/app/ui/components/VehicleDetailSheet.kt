package com.dttrackpro.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dttrackpro.app.data.model.Device
import com.dttrackpro.app.ui.theme.*

@Composable
fun VehicleDetailSheet(
    device: Device,
    onViewHistory: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(device.name, style = MaterialTheme.typography.headlineSmall, color = Cloud100)
                Spacer(Modifier.height(4.dp))
                StatusChip(device.data.status)
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.Close, contentDescription = "Close", tint = Slate300)
            }
        }

        Spacer(Modifier.height(4.dp))
        Text(device.data.address ?: "Address unavailable", style = MaterialTheme.typography.bodyMedium, color = Slate300)

        Spacer(Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricTile(Icons.Filled.Speed, "Speed", "${device.data.speed.toInt()} km/h", Modifier.weight(1f))
            MetricTile(
                Icons.Filled.PowerSettingsNew, "Ignition",
                if (device.data.params.ignition) "On" else "Off",
                Modifier.weight(1f)
            )
            MetricTile(
                Icons.Filled.LocalGasStation, "Fuel",
                device.data.params.fuelLevel?.let { "$it%" } ?: "—",
                Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricTile(
                Icons.Filled.BatteryFull, "Battery",
                device.data.params.batteryLevel?.let { "$it%" } ?: "—",
                Modifier.weight(1f)
            )
            MetricTile(
                Icons.Filled.Route, "Odometer",
                device.data.params.odometerKm?.let { "${it.toInt()} km" } ?: "—",
                Modifier.weight(1f)
            )
            MetricTile(Icons.Filled.Height, "Altitude", "${device.data.altitude.toInt()} m", Modifier.weight(1f))
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = onViewHistory,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SignalCyan, contentColor = Graphite900)
        ) {
            Icon(Icons.Filled.History, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("View trip history", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun MetricTile(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Graphite700, MaterialTheme.shapes.medium)
            .padding(vertical = 12.dp, horizontal = 10.dp)
    ) {
        Icon(icon, contentDescription = null, tint = SignalCyan, modifier = Modifier.size(18.dp))
        Spacer(Modifier.height(8.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, color = Cloud100)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Slate500)
    }
}
