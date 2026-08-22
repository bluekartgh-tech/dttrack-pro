package com.dttrackpro.app.ui.screens.vehicles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dttrackpro.app.ui.components.DeviceListItem
import com.dttrackpro.app.ui.main.FleetFilter
import com.dttrackpro.app.ui.main.FleetViewModel
import com.dttrackpro.app.ui.theme.*

@Composable
fun VehiclesScreen(
    fleetViewModel: FleetViewModel,
    onVehicleTapped: (Long) -> Unit,
) {
    val state by fleetViewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(Graphite900)) {
        Column(modifier = Modifier.padding(16.dp).statusBarsPadding()) {
            Text("Vehicles", style = MaterialTheme.typography.headlineSmall, color = Cloud100)
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.query,
                onValueChange = fleetViewModel::onQueryChange,
                placeholder = { Text("Search vehicles") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Slate500) },
                singleLine = true,
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

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FleetFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = state.filter == filter,
                        onClick = { fleetViewModel.onFilterChange(filter) },
                        label = { Text("${filter.label()} (${state.counts[filter] ?: 0})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SignalCyan.copy(alpha = 0.18f),
                            selectedLabelColor = SignalCyan,
                            containerColor = Graphite800,
                            labelColor = Slate300,
                        )
                    )
                }
            }
        }

        if (state.isLoading) {
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator(color = SignalCyan)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.filteredDevices, key = { it.id }) { device ->
                DeviceListItem(device = device, isSelected = false, onClick = { onVehicleTapped(device.id) })
            }
            if (!state.isLoading && state.filteredDevices.isEmpty()) {
                item {
                    Text(
                        "No vehicles found. Check that your fleet has GPS units reporting to the server.",
                        color = Slate500,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

private fun FleetFilter.label() = when (this) {
    FleetFilter.ALL -> "All"
    FleetFilter.MOVING -> "Moving"
    FleetFilter.STOPPED -> "Stopped"
    FleetFilter.OFFLINE -> "Offline"
}
