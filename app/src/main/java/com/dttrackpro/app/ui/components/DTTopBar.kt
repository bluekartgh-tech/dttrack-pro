package com.dttrackpro.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dttrackpro.app.ui.theme.Cloud100
import com.dttrackpro.app.ui.theme.Graphite900
import com.dttrackpro.app.ui.theme.SignalCyan

@Composable
fun DTTopBar(
    title: String,
    onBellClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall, color = Cloud100, modifier = Modifier.weight(1f))
        IconButton(onClick = onBellClick) {
            Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = SignalCyan)
        }
    }
}

@Composable
fun DTFloatingTopBar(
    title: String,
    onBellClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.statusBarsPadding(),
        color = Graphite900.copy(alpha = 0.92f),
        shape = MaterialTheme.shapes.large,
        shadowElevation = 6.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = Cloud100)
            Spacer(Modifier.width(20.dp))
            IconButton(onClick = onBellClick, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = SignalCyan, modifier = Modifier.size(20.dp))
            }
        }
    }
}
