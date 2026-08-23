package com.dttrackpro.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.dttrackpro.app.ui.components.dtCard
import com.dttrackpro.app.ui.components.dtSwitchColors
import com.dttrackpro.app.ui.theme.*

private data class NotificationToggle(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val key: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(onBack: () -> Unit) {
    val toggles = remember {
        mutableStateMapOf(
            "geofence" to true,
            "ignition" to true,
            "battery" to true,
            "offline" to false,
            "overspeed" to false,
        )
    }

    val items = listOf(
        NotificationToggle(Icons.Filled.Map, "Geofence entry/exit", "Alert when a vehicle crosses a zone boundary", "geofence"),
        NotificationToggle(Icons.Filled.PowerSettingsNew, "Ignition changes", "Alert on ignition on/off", "ignition"),
        NotificationToggle(Icons.Filled.BatteryAlert, "Low battery", "Alert when device battery is low", "battery"),
        NotificationToggle(Icons.Filled.SignalCellularOff, "Vehicle offline", "Alert when a vehicle stops reporting", "offline"),
        NotificationToggle(Icons.Filled.Speed, "Overspeed", "Alert when a vehicle exceeds a speed limit", "overspeed"),
    )

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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Graphite900)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .dtCard()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(item.icon, contentDescription = null, tint = SignalCyan)
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.title, style = MaterialTheme.typography.titleSmall, color = Cloud100)
                        Text(item.subtitle, style = MaterialTheme.typography.bodyMedium, color = Slate500)
                    }
                    Switch(
                        checked = toggles[item.key] == true,
                        onCheckedChange = { toggles[item.key] = it },
                        colors = dtSwitchColors()
                    )
                }
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}
