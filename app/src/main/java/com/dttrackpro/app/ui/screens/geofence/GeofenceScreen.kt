package com.dttrackpro.app.ui.screens.geofence

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dttrackpro.app.data.model.Geofence
import com.dttrackpro.app.ui.components.dtCard
import com.dttrackpro.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeofenceScreen(
    viewModel: GeofenceViewModel = viewModel(),
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = Graphite900,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Geofences", color = Cloud100) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Cloud100)
                    }
                },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add geofence", tint = SignalCyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Graphite900)
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SignalCyan)
            }
            return@Scaffold
        }

        if (state.geofences.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding).padding(32.dp), contentAlignment = Alignment.Center) {
                Text(
                    "No geofences yet. Tap + to create one.",
                    color = Slate500,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.geofences, key = { it.id }) { fence ->
                    GeofenceRow(fence)
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateGeofenceDialog(
            isSaving = state.isSaving,
            onDismiss = { showCreateDialog = false },
            onCreate = { name, lat, lng, radius ->
                viewModel.createGeofence(name, lat, lng, radius) { success ->
                    if (success) showCreateDialog = false
                }
            }
        )
    }
}

@Composable
private fun GeofenceRow(fence: Geofence) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .dtCard()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .background(runCatching { Color(android.graphics.Color.parseColor(fence.color)) }.getOrDefault(SignalCyan), CircleShape)
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(fence.name, style = MaterialTheme.typography.titleSmall, color = Cloud100)
            Spacer(Modifier.height(2.dp))
            Text(
                "${fence.type} • radius ${fence.radiusMeters?.toInt() ?: 0} m",
                style = MaterialTheme.typography.bodyMedium,
                color = Slate500
            )
        }
    }
}

@Composable
private fun CreateGeofenceDialog(
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onCreate: (name: String, lat: Double, lng: Double, radius: Double) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var lat by remember { mutableStateOf("") }
    var lng by remember { mutableStateOf("") }
    var radius by remember { mutableStateOf("300") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Graphite800,
        title = { Text("New geofence", color = Cloud100) },
        text = {
            Column {
                DialogField("Name", name) { name = it }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DialogField("Latitude", lat, Modifier.weight(1f), KeyboardType.Decimal) { lat = it }
                    DialogField("Longitude", lng, Modifier.weight(1f), KeyboardType.Decimal) { lng = it }
                }
                Spacer(Modifier.height(10.dp))
                DialogField("Radius (meters)", radius, keyboardType = KeyboardType.Number) { radius = it }
                Text(
                    "Tip: open a vehicle's Live Tracking screen to read off its exact coordinates.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate500,
                    modifier = Modifier.padding(top = 10.dp)
                )
                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = DangerCoral, style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val latD = lat.toDoubleOrNull()
                    val lngD = lng.toDoubleOrNull()
                    val radiusD = radius.toDoubleOrNull()
                    when {
                        name.isBlank() -> error = "Name is required"
                        latD == null || lngD == null -> error = "Enter valid coordinates"
                        radiusD == null || radiusD <= 0 -> error = "Enter a valid radius"
                        else -> {
                            error = null
                            onCreate(name, latD, lngD, radiusD)
                        }
                    }
                },
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = SignalCyan, strokeWidth = 2.dp)
                } else {
                    Text("Create", color = SignalCyan)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) { Text("Cancel", color = Slate500) }
        }
    )
}

@Composable
private fun DialogField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SignalCyan,
            unfocusedBorderColor = Graphite600,
            focusedTextColor = Cloud100,
            unfocusedTextColor = Cloud100,
            cursorColor = SignalCyan,
        )
    )
}
