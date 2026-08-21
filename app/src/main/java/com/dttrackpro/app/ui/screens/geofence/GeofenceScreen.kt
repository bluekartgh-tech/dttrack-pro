package com.dttrackpro.app.ui.screens.geofence

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dttrackpro.app.data.model.Geofence
import com.dttrackpro.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeofenceScreen(
    viewModel: GeofenceViewModel = viewModel(),
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Graphite900,
        topBar = {
            TopAppBar(
                title = { Text("Geofences", color = Cloud100) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Cloud100)
                    }
                },
                actions = {
                    IconButton(onClick = { /* hook up create-geofence flow */ }) {
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

@Composable
private fun GeofenceRow(fence: Geofence) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Graphite800, MaterialTheme.shapes.medium)
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
