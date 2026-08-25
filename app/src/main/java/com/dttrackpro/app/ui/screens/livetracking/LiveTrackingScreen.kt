package com.dttrackpro.app.ui.screens.livetracking

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dttrackpro.app.data.model.Device
import com.dttrackpro.app.ui.components.MapTileMode
import com.dttrackpro.app.ui.components.SingleVehicleMapView
import com.dttrackpro.app.ui.components.StatusChip
import com.dttrackpro.app.ui.components.dtCard
import com.dttrackpro.app.ui.theme.*

private data class QuickAction(val icon: ImageVector, val label: String, val active: Boolean, val onClick: () -> Unit)

@Composable
fun LiveTrackingScreen(
    viewModel: LiveTrackingViewModel = viewModel(),
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showCommandSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.lastCommandResult) {
        state.lastCommandResult?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearCommandResult()
        }
    }

    Scaffold(
        containerColor = Graphite900,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { _ ->
        Box(modifier = Modifier.fillMaxSize()) {
            SingleVehicleMapView(
                device = state.device,
                tileMode = state.tileMode,
                trailPoints = state.trailPoints,
                showTrail = state.trailOn,
                geofences = state.geofences,
                showGeofences = state.geofencesOn,
                modifier = Modifier.fillMaxSize(),
            )

            Surface(
                modifier = Modifier.align(Alignment.TopStart).statusBarsPadding().padding(12.dp),
                color = Graphite800.copy(alpha = 0.92f),
                shape = MaterialTheme.shapes.large,
                shadowElevation = 6.dp,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Cloud100)
                }
            }

            if (state.trafficOn) {
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(12.dp),
                    color = AlertAmber.copy(alpha = 0.92f),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Text(
                        "Traffic needs a live provider",
                        color = Graphite900,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val actions = listOf(
                    QuickAction(Icons.Filled.Terminal, "Commands", false) { showCommandSheet = true },
                    QuickAction(Icons.Filled.Layers, "Map mode", state.tileMode == MapTileMode.TOPOGRAPHIC, viewModel::toggleTileMode),
                    QuickAction(Icons.Filled.Traffic, "Traffic", state.trafficOn, viewModel::toggleTraffic),
                    QuickAction(Icons.Filled.Route, "Trail", state.trailOn, viewModel::toggleTrail),
                    QuickAction(Icons.Filled.Share, "Share", false) {
                        state.device?.let { d ->
                            val url = "https://maps.google.com/?q=${d.data.lat},${d.data.lng}"
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "${d.name} — live location: $url")
                            }
                            context.startActivity(Intent.createChooser(intent, "Share location"))
                        }
                    },
                    QuickAction(Icons.Filled.Map, "Geofences", state.geofencesOn, viewModel::toggleGeofences),
                )
                actions.forEach { action -> FloatingActionChip(action) }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(12.dp)
            ) {
                VehicleInfoCard(device = state.device, isLoadingTrail = state.isLoadingTrail)
            }
        }
    }

    if (showCommandSheet) {
        CommandDialog(
            onDismiss = { showCommandSheet = false },
            onSend = { command ->
                viewModel.sendCommand(command)
                showCommandSheet = false
            },
            sending = state.commandInFlight,
        )
    }
}

@Composable
private fun FloatingActionChip(action: QuickAction) {
    Surface(
        modifier = Modifier
            .width(66.dp)
            .clickable(onClick = action.onClick),
        color = if (action.active) SignalCyan else Graphite800.copy(alpha = 0.92f),
        shape = MaterialTheme.shapes.medium,
        shadowElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                action.icon,
                contentDescription = action.label,
                tint = if (action.active) Graphite900 else SignalCyan,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                action.label,
                style = MaterialTheme.typography.labelSmall,
                color = if (action.active) Graphite900 else Slate300,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun VehicleInfoCard(device: Device?, isLoadingTrail: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .dtCard(fill = Graphite800.copy(alpha = 0.96f))
            .padding(16.dp)
    ) {
        if (device == null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = SignalCyan, strokeWidth = 2.dp)
                Spacer(Modifier.width(10.dp))
                Text("Waiting for vehicle data…", color = Slate300)
            }
            return@Column
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(device.name, style = MaterialTheme.typography.titleMedium, color = Cloud100, maxLines = 1)
                Spacer(Modifier.height(4.dp))
                StatusChip(device.data.status)
            }
            Text("${device.data.speed.toInt()} km/h", style = MaterialTheme.typography.titleMedium, color = Cloud100)
        }

        Spacer(Modifier.height(6.dp))
        Text(
            device.data.address ?: "Resolving address…",
            style = MaterialTheme.typography.bodyMedium,
            color = Slate500,
            maxLines = 2,
        )

        Spacer(Modifier.height(12.dp))

        val params = device.data.params
        val chips = buildList {
            add(Triple(Icons.Filled.PowerSettingsNew, "Ignition", if (params.ignition) "On" else "Off"))
            params.batteryLevel?.let { add(Triple(Icons.Filled.BatteryFull, "Battery", "$it%")) }
            params.signalStrength?.let { add(Triple(Icons.Filled.SignalCellular4Bar, "Signal", it)) }
            params.satelliteCount?.let { add(Triple(Icons.Filled.Satellite, "GPS", "$it sats")) }
            params.powerVoltage?.let { add(Triple(Icons.Filled.Bolt, "Power", "$it V")) }
            params.odometerKm?.let { add(Triple(Icons.Filled.Route, "Odometer", "${it.toInt()} km")) }
        }

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            chips.forEach { (icon, label, value) -> SensorChip(icon, label, value) }
        }

        if (isLoadingTrail) {
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = SignalCyan)
        }
    }
}

@Composable
private fun SensorChip(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.background(Graphite700, MaterialTheme.shapes.small).padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = SignalCyan, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(6.dp))
        Text("$label: $value", style = MaterialTheme.typography.labelSmall, color = Cloud100)
    }
}

@Composable
private fun CommandDialog(onDismiss: () -> Unit, onSend: (String) -> Unit, sending: Boolean) {
    var customCommand by remember { mutableStateOf("") }
    val presets = listOf("Engine cutoff", "Engine restore", "Request location")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Graphite800,
        title = { Text("Send command", color = Cloud100) },
        text = {
            Column {
                presets.forEach { preset ->
                    TextButton(onClick = { onSend(preset) }, enabled = !sending) {
                        Text(preset, color = Cloud100)
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = customCommand,
                    onValueChange = { customCommand = it },
                    label = { Text("Custom command") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SignalCyan,
                        unfocusedBorderColor = Graphite600,
                        focusedTextColor = Cloud100,
                        unfocusedTextColor = Cloud100,
                        cursorColor = SignalCyan,
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { if (customCommand.isNotBlank()) onSend(customCommand) }, enabled = !sending && customCommand.isNotBlank()) {
                Text("Send custom", color = SignalCyan)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = Slate500) }
        }
    )
}
