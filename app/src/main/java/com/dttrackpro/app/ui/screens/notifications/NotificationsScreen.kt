package com.dttrackpro.app.ui.screens.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SignalCellularOff
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.dttrackpro.app.data.NotificationCenter
import com.dttrackpro.app.ui.components.dtCard
import com.dttrackpro.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val events by NotificationCenter.events.collectAsState()

    Scaffold(
        containerColor = Graphite900,
        topBar = {
            TopAppBar(
                title = { Text("Notifications", color = Cloud100) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Cloud100)
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Notification settings", tint = SignalCyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Graphite900)
            )
        }
    ) { padding ->
        if (events.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding).padding(32.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.NotificationsNone, contentDescription = null, tint = Slate500, modifier = Modifier.size(40.dp))
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "No notifications yet. You'll see ignition, offline, and low-battery alerts here as they happen.",
                        color = Slate500,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(events, key = { it.id }) { event ->
                    Row(
                        modifier = Modifier.fillMaxWidth().dtCard().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val (icon, color) = iconFor(event.category)
                        Icon(icon, contentDescription = null, tint = color)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(event.message, style = MaterialTheme.typography.bodyLarge, color = Cloud100)
                            Spacer(Modifier.height(2.dp))
                            Text(relativeTime(event.timestampMs), style = MaterialTheme.typography.labelSmall, color = Slate500)
                        }
                    }
                }
            }
        }
    }
}

private fun iconFor(category: NotificationCenter.Category): Pair<ImageVector, Color> = when (category) {
    NotificationCenter.Category.OFFLINE -> Icons.Filled.SignalCellularOff to DangerCoral
    NotificationCenter.Category.BACK_ONLINE -> Icons.Filled.WifiTethering to OnlineGreen
    NotificationCenter.Category.IGNITION -> Icons.Filled.PowerSettingsNew to SignalCyan
    NotificationCenter.Category.BATTERY -> Icons.Filled.BatteryAlert to AlertAmber
    NotificationCenter.Category.GEOFENCE -> Icons.Filled.Map to AccentViolet
}

private fun relativeTime(timestampMs: Long): String {
    val diffMs = System.currentTimeMillis() - timestampMs
    val minutes = diffMs / 60_000
    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        minutes < 24 * 60 -> "${minutes / 60}h ago"
        else -> "${minutes / (24 * 60)}d ago"
    }
}
