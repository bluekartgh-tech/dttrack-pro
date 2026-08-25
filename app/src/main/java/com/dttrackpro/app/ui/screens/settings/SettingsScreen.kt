package com.dttrackpro.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.dttrackpro.app.AppContainer
import com.dttrackpro.app.BuildConfig
import com.dttrackpro.app.ui.components.DTTopBar
import com.dttrackpro.app.ui.components.dtCard
import com.dttrackpro.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onOpenChangePassword: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onOpenGeofences: () -> Unit,
    onOpenVehicleManage: () -> Unit,
    onLoggedOut: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Graphite900)
            .verticalScroll(rememberScrollState())
    ) {
        DTTopBar(title = "Settings", onBellClick = onOpenNotificationSettings)

        Column(modifier = Modifier.padding(16.dp)) {
        Spacer(Modifier.height(4.dp))

        SectionLabel("Account")
        SettingsNavRow(Icons.Filled.Lock, "Change password", "Update your login credentials", onOpenChangePassword)

        Spacer(Modifier.height(20.dp))
        SectionLabel("Fleet")
        SettingsNavRow(Icons.Filled.Notifications, "Notifications", "Geofence, ignition, and offline alerts", onOpenNotificationSettings)
        Spacer(Modifier.height(8.dp))
        SettingsNavRow(Icons.Filled.Map, "Geofences", "Create and manage zones", onOpenGeofences)
        Spacer(Modifier.height(8.dp))
        SettingsNavRow(Icons.Filled.DirectionsCar, "Manage vehicles", "Rename vehicles and change icons", onOpenVehicleManage)

        Spacer(Modifier.height(20.dp))
        SectionLabel("System")
        SettingsInfoRow(Icons.Filled.Dns, "Backend server", BuildConfig.API_BASE_URL)

        Spacer(Modifier.height(28.dp))

        Button(
            onClick = {
                scope.launch {
                    AppContainer.authRepository.logout()
                    onLoggedOut()
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Graphite700, contentColor = DangerCoral)
        ) {
            Icon(Icons.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Log out")
        }

        Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text.uppercase(), style = MaterialTheme.typography.labelSmall, color = Slate500)
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun SettingsNavRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .dtCard()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = SignalCyan)
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = Cloud100)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Slate500)
        }
        Icon(
            Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = Slate500,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun SettingsInfoRow(icon: ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier.fillMaxWidth().dtCard().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = SignalCyan)
        Spacer(Modifier.width(14.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall, color = Cloud100)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Slate500)
        }
    }
}
