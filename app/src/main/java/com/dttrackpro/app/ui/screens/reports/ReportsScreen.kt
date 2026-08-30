package com.dttrackpro.app.ui.screens.reports

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dttrackpro.app.ui.components.dtCard
import com.dttrackpro.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel = viewModel(),
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Graphite900,
        topBar = {
            TopAppBar(
                title = { Text("Reports", color = Cloud100) },
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
            Text("Vehicle", style = MaterialTheme.typography.labelSmall, color = Slate500)
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.devices.forEach { device ->
                    FilterChip(
                        selected = state.selectedDeviceId == device.id,
                        onClick = { viewModel.selectDevice(device.id) },
                        label = { Text(device.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SignalCyan,
                            selectedLabelColor = Graphite900,
                            containerColor = Graphite800,
                            labelColor = Slate300,
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = state.selectedDeviceId == device.id,
                            borderColor = Graphite600,
                            selectedBorderColor = SignalCyan,
                        )
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            when {
                state.selectedDeviceId == null -> {
                    Text(
                        "Pick a vehicle above to see its last 24 hours: distance covered, average and top speed.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate500,
                    )
                }
                state.isLoading -> {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = SignalCyan)
                    }
                }
                state.errorMessage != null -> {
                    Row(
                        modifier = Modifier.fillMaxWidth().dtCard(fill = DangerCoral.copy(alpha = 0.12f)).padding(14.dp),
                    ) {
                        Text("Couldn't load report: ${state.errorMessage}", color = Cloud100, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                else -> {
                    Text("Last 24 hours", style = MaterialTheme.typography.titleMedium, color = Cloud100)
                    Spacer(Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatTile(Icons.Filled.Route, "Distance", "${(state.totalDistanceKm ?: 0.0).let { "%.1f".format(it) }} km", Modifier.weight(1f))
                        StatTile(Icons.Filled.Speed, "Avg speed", "${(state.avgSpeed ?: 0.0).toInt()} km/h", Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatTile(Icons.Filled.TrendingUp, "Top speed", "${(state.maxSpeed ?: 0.0).toInt()} km/h", Modifier.weight(1f))
                        StatTile(Icons.Filled.Timeline, "GPS fixes", "${state.pointCount}", Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun StatTile(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.dtCard().padding(16.dp)) {
        Icon(icon, contentDescription = null, tint = SignalCyan, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(10.dp))
        Text(value, style = MaterialTheme.typography.headlineSmall, color = Cloud100)
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Slate500)
    }
}
