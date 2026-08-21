package com.dttrackpro.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dttrackpro.app.data.model.DeviceStatus
import com.dttrackpro.app.ui.theme.OfflineGrey
import com.dttrackpro.app.ui.theme.OnlineGreen
import com.dttrackpro.app.ui.theme.SignalCyan

@Composable
fun StatusChip(status: DeviceStatus, modifier: Modifier = Modifier) {
    val (color, label) = when (status) {
        DeviceStatus.MOVING -> SignalCyan to "Moving"
        DeviceStatus.STOPPED -> OnlineGreen to "Stopped"
        DeviceStatus.OFFLINE -> OfflineGrey to "Offline"
    }
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.16f), shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable
fun StatusDot(status: DeviceStatus, modifier: Modifier = Modifier) {
    val color = when (status) {
        DeviceStatus.MOVING -> SignalCyan
        DeviceStatus.STOPPED -> OnlineGreen
        DeviceStatus.OFFLINE -> OfflineGrey
    }
    Box(modifier = modifier.background(color, CircleShape))
}
