package com.dttrackpro.app.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.SpaceDashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dttrackpro.app.ui.screens.dashboard.FleetDashboardScreen
import com.dttrackpro.app.ui.screens.map.MapScreen
import com.dttrackpro.app.ui.screens.settings.SettingsScreen
import com.dttrackpro.app.ui.screens.vehicles.VehiclesScreen
import com.dttrackpro.app.ui.theme.Graphite800
import com.dttrackpro.app.ui.theme.Graphite900
import com.dttrackpro.app.ui.theme.SignalCyan
import com.dttrackpro.app.ui.theme.Slate500

private enum class MainTab(val label: String, val icon: ImageVector) {
    VEHICLES("Vehicles", Icons.Filled.DirectionsCar),
    MAP("Map", Icons.Filled.Map),
    DASHBOARD("Dashboard", Icons.Filled.SpaceDashboard),
    SETTINGS("Settings", Icons.Filled.Settings),
}

@Composable
fun MainScaffoldScreen(
    onVehicleTapped: (Long) -> Unit,
    onOpenChangePassword: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onOpenGeofences: () -> Unit,
    onOpenVehicleManage: () -> Unit,
    onLoggedOut: () -> Unit,
) {
    val fleetViewModel: FleetViewModel = viewModel()
    var selectedTab by remember { mutableStateOf(MainTab.VEHICLES) }

    Scaffold(
        containerColor = Graphite900,
        bottomBar = {
            NavigationBar(containerColor = Graphite800) {
                MainTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SignalCyan,
                            selectedTextColor = SignalCyan,
                            unselectedIconColor = Slate500,
                            unselectedTextColor = Slate500,
                            indicatorColor = SignalCyan.copy(alpha = 0.14f),
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding())) {
            when (selectedTab) {
                MainTab.VEHICLES -> VehiclesScreen(fleetViewModel = fleetViewModel, onVehicleTapped = onVehicleTapped)
                MainTab.MAP -> MapScreen(fleetViewModel = fleetViewModel, onVehicleTapped = onVehicleTapped)
                MainTab.DASHBOARD -> FleetDashboardScreen(fleetViewModel = fleetViewModel, onVehicleTapped = onVehicleTapped)
                MainTab.SETTINGS -> SettingsScreen(
                    onOpenChangePassword = onOpenChangePassword,
                    onOpenNotificationSettings = onOpenNotificationSettings,
                    onOpenGeofences = onOpenGeofences,
                    onOpenVehicleManage = onOpenVehicleManage,
                    onLoggedOut = onLoggedOut,
                )
            }
        }
    }
}
