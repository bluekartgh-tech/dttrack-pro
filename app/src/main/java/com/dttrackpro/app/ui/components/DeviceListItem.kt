package com.dttrackpro.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.AirportShuttle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dttrackpro.app.data.model.Device
import com.dttrackpro.app.data.model.DeviceStatus
import com.dttrackpro.app.ui.theme.*

@Composable
fun DeviceListItem(
    device: Device,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg by animateColorAsState(if (isSelected) Graphite700 else Graphite800, label = "rowBg")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .dtCard(fill = bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(iconTint(device.data.status).copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconFor(device.icon),
                contentDescription = null,
                tint = iconTint(device.data.status),
                modifier = Modifier.size(20.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(device.name, style = MaterialTheme.typography.titleSmall, color = Cloud100, maxLines = 1)
            Spacer(Modifier.height(2.dp))
            Text(
                device.data.address ?: "—",
                style = MaterialTheme.typography.bodyMedium,
                color = Slate500,
                maxLines = 1
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            StatusChip(device.data.status)
            Spacer(Modifier.height(4.dp))
            Text(
                if (device.data.status == DeviceStatus.OFFLINE) "no signal" else "${device.data.speed.toInt()} km/h",
                style = MaterialTheme.typography.bodyMedium,
                color = Slate500
            )
        }
    }
}

private fun iconFor(icon: String?) = when (icon) {
    "truck" -> Icons.Filled.LocalShipping
    "van" -> Icons.Filled.AirportShuttle
    "bike" -> Icons.Filled.DirectionsBike
    else -> Icons.Filled.DirectionsCar
}

private fun iconTint(status: DeviceStatus): Color = when (status) {
    DeviceStatus.MOVING -> SignalCyan
    DeviceStatus.STOPPED -> OnlineGreen
    DeviceStatus.OFFLINE -> OfflineGrey
}
