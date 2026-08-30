package com.dttrackpro.app.ui.screens.livetracking

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
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
private val iconOptions = listOf("car" to Icons.Filled.DirectionsCar, "truck" to Icons.Filled.LocalShipping, "van" to Icons.Filled.AirportShuttle, "bike" to Icons.Filled.DirectionsBike)

@Composable
fun LiveTrackingScreen(
    viewModel: LiveTrackingViewModel = viewModel(),
    onBack: () -> Unit,
    onBellClick: () -> Unit,
    onViewHistory: (Long) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showCommandSheet by remember { mutableStateOf(false) }
    var showCreateGeofence by remember { mutableStateOf(false) }
    var showChangeIcon by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.toast) {
        state.toast?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearToast()
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
                onVehicleTapped = { viewModel.openVehicleMenu() },
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

            Surface(
                modifier = Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(12.dp),
                color = Graphite800.copy(alpha = 0.92f),
                shape = CircleShape,
                shadowElevation = 6.dp,
            ) {
                IconButton(onClick = onBellClick, modifier = Modifier.size(44.dp)) {
                    Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = SignalCyan, modifier = Modifier.size(20.dp))
                }
            }

            if (state.trafficOn) {
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(top = 68.dp, end = 12.dp),
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

            if (state.showVehicleMenu && state.device != null) {
                VehicleContextMenu(
                    modifier = Modifier.align(Alignment.Center).offset(y = (-70).dp),
                    onDismiss = viewModel::dismissVehicleMenu,
                    onHistory = { viewModel.dismissVehicleMenu(); onViewHistory(state.deviceId) },
                    onCreateGeofence = { showCreateGeofence = true },
                    onChangeIcon = { showChangeIcon = true },
                    onParkingOn = { viewModel.toggleParkingMode(true) },
                    onParkingOff = { viewModel.toggleParkingMode(false) },
                )
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

    if (showCreateGeofence) {
        CreateGeofenceHereDialog(
            onDismiss = { showCreateGeofence = false },
            onCreate = { name, radius ->
                viewModel.createGeofenceHere(name, radius)
                showCreateGeofence = false
            }
        )
    }

    if (showChangeIcon) {
        ChangeIconDialog(
            currentIcon = state.device?.icon,
            onDismiss = { showChangeIcon = false },
            onSave = { icon ->
                viewModel.changeIcon(icon)
                showChangeIcon = false
            }
        )
    }
}

@Composable
private fun VehicleContextMenu(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onHistory: () -> Unit,
    onCreateGeofence: () -> Unit,
    onChangeIcon: () -> Unit,
    onParkingOn: () -> Unit,
    onParkingOff: () -> Unit,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            color = Graphite800,
            shape = MaterialTheme.shapes.medium,
            shadowElevation = 10.dp,
        ) {
            Column(modifier = Modifier.width(220.dp).padding(vertical = 6.dp)) {
                MenuRow(Icons.Filled.History, "View history") { onHistory() }
                MenuRow(Icons.Filled.Map, "Create geofence here") { onCreateGeofence() }
                MenuRow(Icons.Filled.Edit, "Change icon") { onChangeIcon() }
                MenuRow(Icons.Filled.LocalParking, "Turn on parking mode") { onParkingOn() }
                MenuRow(Icons.Filled.LocalParking, "Turn off parking mode") { onParkingOff() }
                MenuRow(Icons.Filled.Close, "Close") { onDismiss() }
            }
        }
        Canvas(modifier = Modifier.size(18.dp, 9.dp)) {
            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width / 2f, size.height)
                close()
            }
            drawPath(path, color = Graphite800)
        }
    }
}

@Composable
private fun MenuRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = SignalCyan, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Cloud100)
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
            add(Triple(Icons.Filled.PowerSettingsNew, "Ignition", if (params.ignition) "On" else "Off") to (if (params.ignition) OnlineGreen else Slate500))
            params.batteryLevel?.let { level ->
                val color = when { level >= 50 -> OnlineGreen; level >= 20 -> AlertAmber; else -> DangerCoral }
                add(Triple(Icons.Filled.BatteryFull, "Battery", "$level%") to color)
            }
            params.signalStrength?.let { add(Triple(Icons.Filled.SignalCellular4Bar, "Signal", it) to AccentBlue) }
            params.satelliteCount?.let { add(Triple(Icons.Filled.Satellite, "GPS", "$it sats") to AccentViolet) }
            params.powerVoltage?.let { add(Triple(Icons.Filled.Bolt, "Power", "$it V") to AlertAmber) }
            params.odometerKm?.let { add(Triple(Icons.Filled.Route, "Odometer", "${it.toInt()} km") to Slate300) }
        }

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            chips.forEach { (triple, color) -> SensorChip(triple.first, triple.second, triple.third, color) }
        }

        if (isLoadingTrail) {
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = SignalCyan)
        }
    }
}

@Composable
private fun SensorChip(icon: ImageVector, label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier.background(Graphite700, MaterialTheme.shapes.small).padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
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

@Composable
private fun CreateGeofenceHereDialog(onDismiss: () -> Unit, onCreate: (name: String, radius: Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var radius by remember { mutableStateOf("300") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Graphite800,
        title = { Text("Geofence at current location", color = Cloud100) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SignalCyan, unfocusedBorderColor = Graphite600,
                        focusedTextColor = Cloud100, unfocusedTextColor = Cloud100, cursorColor = SignalCyan,
                    )
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = radius,
                    onValueChange = { radius = it },
                    label = { Text("Radius (meters)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SignalCyan, unfocusedBorderColor = Graphite600,
                        focusedTextColor = Cloud100, unfocusedTextColor = Cloud100, cursorColor = SignalCyan,
                    )
                )
                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = DangerCoral, style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val radiusD = radius.toDoubleOrNull()
                when {
                    name.isBlank() -> error = "Name is required"
                    radiusD == null || radiusD <= 0 -> error = "Enter a valid radius"
                    else -> onCreate(name, radiusD)
                }
            }) { Text("Create", color = SignalCyan) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Slate500) }
        }
    )
}

@Composable
private fun ChangeIconDialog(currentIcon: String?, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var selected by remember { mutableStateOf(currentIcon ?: "car") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Graphite800,
        title = { Text("Change icon", color = Cloud100) },
        text = {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                iconOptions.forEach { (key, icon) ->
                    val isSelected = key == selected
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(if (isSelected) SignalCyan.copy(alpha = 0.2f) else Graphite700, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = { selected = key }) {
                            Icon(icon, contentDescription = key, tint = if (isSelected) SignalCyan else Slate300)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(selected) }) { Text("Save", color = SignalCyan) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Slate500) }
        }
    )
}
