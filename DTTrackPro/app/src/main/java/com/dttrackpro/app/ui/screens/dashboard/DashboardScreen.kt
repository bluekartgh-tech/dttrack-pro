package com.dttrackpro.app.ui.screens.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dttrackpro.app.ui.components.DeviceListItem
import com.dttrackpro.app.ui.components.OsmMapView
import com.dttrackpro.app.ui.components.VehicleDetailSheet
import com.dttrackpro.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel(),
    onOpenHistory: (Long) -> Unit,
    onOpenGeofences: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val sheetState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(initialValue = SheetValue.PartiallyExpanded)
    )

    BottomSheetScaffold(
        scaffoldState = sheetState,
        sheetPeekHeight = 320.dp,
        sheetContainerColor = Graphite800,
        sheetDragHandle = { BottomSheetDefaults.DragHandle(color = Graphite600) },
        topBar = {
            DashboardTopBar(
                query = state.query,
                onQueryChange = viewModel::onQueryChange,
                onGeofences = onOpenGeofences,
                onSettings = onOpenSettings,
            )
        },
        sheetContent = {
            AnimatedContent(targetState = state.selectedDevice, label = "sheetContent") { selected ->
                if (selected != null) {
                    VehicleDetailSheet(
                        device = selected,
                        onViewHistory = { onOpenHistory(selected.id) },
                        onClose = { viewModel.selectDevice(null) },
                    )
                } else {
                    FleetListContent(state = state, viewModel = viewModel)
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding())) {
            OsmMapView(
                devices = state.devices,
                selectedDeviceId = state.selectedDeviceId,
                onVehicleTapped = { viewModel.selectDevice(it) },
                followSelected = state.followSelected,
                modifier = Modifier.fillMaxSize(),
            )

            if (state.selectedDeviceId != null) {
                FollowToggleButton(
                    following = state.followSelected,
                    onClick = viewModel::toggleFollow,
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onGeofences: () -> Unit,
    onSettings: () -> Unit,
) {
    Surface(color = Graphite900, tonalElevation = 0.dp) {
        Column(modifier = Modifier.statusBarsPadding().padding(horizontal = 16.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("DTTrack Pro", style = MaterialTheme.typography.headlineSmall, color = Cloud100, modifier = Modifier.weight(1f))
                IconButton(onClick = onGeofences) {
                    Icon(Icons.Filled.Map, contentDescription = "Geofences", tint = Slate300)
                }
                IconButton(onClick = onSettings) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = Slate300)
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Search vehicles") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Slate500) },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SignalCyan,
                    unfocusedBorderColor = Graphite700,
                    focusedTextColor = Cloud100,
                    unfocusedTextColor = Cloud100,
                    cursorColor = SignalCyan,
                )
            )
        }
    }
}

@Composable
private fun FleetListContent(state: DashboardUiState, viewModel: DashboardViewModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FleetFilter.entries.forEach { filter ->
                FilterChip(
                    selected = state.filter == filter,
                    onClick = { viewModel.onFilterChange(filter) },
                    label = { Text("${filter.label()} (${state.counts[filter] ?: 0})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SignalCyan.copy(alpha = 0.18f),
                        selectedLabelColor = SignalCyan,
                        containerColor = Graphite700,
                        labelColor = Slate300,
                    )
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth().heightIn(max = 520.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.filteredDevices, key = { it.id }) { device ->
                DeviceListItem(
                    device = device,
                    isSelected = device.id == state.selectedDeviceId,
                    onClick = { viewModel.selectDevice(device.id) }
                )
            }
            if (state.filteredDevices.isEmpty()) {
                item {
                    Text(
                        "No vehicles match your search.",
                        color = Slate500,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FollowToggleButton(following: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.clip(MaterialTheme.shapes.large),
        color = if (following) SignalCyan else Graphite800,
        shadowElevation = 6.dp,
    ) {
        IconButton(onClick = onClick) {
            Icon(
                Icons.Filled.MyLocation,
                contentDescription = "Follow vehicle",
                tint = if (following) Graphite900 else Slate300
            )
        }
    }
}

private fun FleetFilter.label() = when (this) {
    FleetFilter.ALL -> "All"
    FleetFilter.MOVING -> "Moving"
    FleetFilter.STOPPED -> "Stopped"
    FleetFilter.OFFLINE -> "Offline"
}
