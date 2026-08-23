package com.dttrackpro.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dttrackpro.app.data.model.Device
import com.dttrackpro.app.ui.components.dtCard
import com.dttrackpro.app.ui.main.FleetViewModel
import com.dttrackpro.app.ui.theme.*
import kotlinx.coroutines.launch

private val iconOptions = listOf("car" to Icons.Filled.DirectionsCar, "truck" to Icons.Filled.LocalShipping, "van" to Icons.Filled.AirportShuttle, "bike" to Icons.Filled.DirectionsBike)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleManageScreen(
    onBack: () -> Unit,
    fleetViewModel: FleetViewModel = viewModel(),
) {
    val state by fleetViewModel.uiState.collectAsState()
    var editingDevice by remember { mutableStateOf<Device?>(null) }

    Scaffold(
        containerColor = Graphite900,
        topBar = {
            TopAppBar(
                title = { Text("Manage vehicles", color = Cloud100) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Cloud100)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Graphite900)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.devices, key = { it.id }) { device ->
                Row(
                    modifier = Modifier.fillMaxWidth().dtCard().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(iconFor(device.icon), contentDescription = null, tint = SignalCyan)
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(device.name, style = MaterialTheme.typography.titleSmall, color = Cloud100)
                        Text(device.imei, style = MaterialTheme.typography.bodyMedium, color = Slate500)
                    }
                    IconButton(onClick = { editingDevice = device }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = Slate300)
                    }
                }
            }
        }
    }

    editingDevice?.let { device ->
        EditVehicleDialog(
            device = device,
            onDismiss = { editingDevice = null },
            onSave = { name, icon ->
                fleetViewModel.updateVehicle(device.id, name, icon)
                editingDevice = null
            }
        )
    }
}

@Composable
private fun EditVehicleDialog(device: Device, onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var name by remember { mutableStateOf(device.name) }
    var selectedIcon by remember { mutableStateOf(device.icon ?: "car") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Graphite800,
        title = { Text("Edit vehicle", color = Cloud100) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Vehicle name") },
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
                Spacer(Modifier.height(16.dp))
                Text("Icon", style = MaterialTheme.typography.labelSmall, color = Slate500)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    iconOptions.forEach { (key, icon) ->
                        val selected = key == selectedIcon
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    if (selected) SignalCyan.copy(alpha = 0.2f) else Graphite700,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(onClick = { selectedIcon = key }) {
                                Icon(icon, contentDescription = key, tint = if (selected) SignalCyan else Slate300)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name, selectedIcon) }) {
                Text("Save", color = SignalCyan)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Slate500)
            }
        }
    )
}

private fun iconFor(icon: String?): ImageVector = when (icon) {
    "truck" -> Icons.Filled.LocalShipping
    "van" -> Icons.Filled.AirportShuttle
    "bike" -> Icons.Filled.DirectionsBike
    else -> Icons.Filled.DirectionsCar
}
