package com.dttrackpro.app.ui.screens.livetracking

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.dttrackpro.app.ui.components.MapTileMode
import com.dttrackpro.app.ui.components.SingleVehicleMapView
import com.dttrackpro.app.ui.components.StatusChip
import com.dttrackpro.app.ui.theme.*

private data class QuickAction(val icon: ImageVector, val label: String, val active: Boolean, val onClick: () -> Unit)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveTrackingScreen(
    viewModel: LiveTrackingViewModel = viewModel(),
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val sheetState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(initialValue = SheetValue.PartiallyExpanded)
    )
    var showCommandSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.lastCommandResult) {
        state.lastCommandResult?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearCommandResult()
        }
    }

    BottomSheetScaffold(
        scaffoldState = sheetState,
        sheetPeekHeight = 220.dp,
        sheetContainerColor = Graphite800,
        sheetDragHandle = { BottomSheetDefaults.DragHandle(color = Graphite600) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        sheetContent = {
            LiveTrackingSheetContent(
                state = state,
                onCommandsClick = { showCommandSheet = true },
                onMapModeClick = viewModel::toggleTileMode,
                onTrafficClick = { viewModel.toggleTraffic() },
                onTrailClick = viewModel::toggleTrail,
                onShareClick = {
                    state.device?.let { d ->
                        val url = "https://maps.google.com/?q=${d.data.lat},${d.data.lng}"
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "${d.name} — live location: $url")
                        }
                        context.startActivity(Intent.createChooser(intent, "Share location"))
                    }
                },
                onGeofencesClick = viewModel::toggleGeofences,
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(top = padding.calculateTopPadding())) {
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
private fun LiveTrackingSheetContent(
    state: LiveTrackingUiState,
    onCommandsClick: () -> Unit,
    onMapModeClick: () -> Unit,
    onTrafficClick: () -> Unit,
    onTrailClick: () -> Unit,
    onShareClick: () -> Unit,
    onGeofencesClick: () -> Unit,
) {
    val device = state.device

    Column(modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
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
                Text(device.name, style = MaterialTheme.typography.headlineSmall, color = Cloud100)
                Spacer(Modifier.height(4.dp))
                StatusChip(device.data.status)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${device.data.speed.toInt()} km/h", style = MaterialTheme.typography.titleMedium, color = Cloud100)
                Text(device.data.address ?: "—", style = MaterialTheme.typography.bodyMedium, color = Slate500)
            }
        }

        Spacer(Modifier.height(20.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.heightIn(max = 260.dp)
        ) {
            val actions = listOf(
                QuickAction(Icons.Filled.Terminal, "Commands", false, onCommandsClick),
                QuickAction(Icons.Filled.Layers, if (state.tileMode == MapTileMode.STANDARD) "Standard" else "Topographic", state.tileMode == MapTileMode.TOPOGRAPHIC, onMapModeClick),
                QuickAction(Icons.Filled.Traffic, "Traffic", state.trafficOn, onTrafficClick),
                QuickAction(Icons.Filled.Route, "24h trail", state.trailOn, onTrailClick),
                QuickAction(Icons.Filled.Share, "Share location", false, onShareClick),
                QuickAction(Icons.Filled.Map, "Geofences", state.geofencesOn, onGeofencesClick),
            )
            items(actions) { action -> QuickActionButton(action) }
        }

        if (state.isLoadingTrail) {
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = SignalCyan)
        }
    }
}

@Composable
private fun QuickActionButton(action: QuickAction) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (action.active) SignalCyan.copy(alpha = 0.16f) else Graphite700,
                MaterialTheme.shapes.medium
            )
            .clickable(onClick = action.onClick)
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(action.icon, contentDescription = action.label, tint = if (action.active) SignalCyan else Slate300, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(6.dp))
        Text(action.label, style = MaterialTheme.typography.labelSmall, color = if (action.active) SignalCyan else Slate300, textAlign = TextAlign.Center)
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
