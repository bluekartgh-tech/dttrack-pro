package com.dttrackpro.app.ui.screens.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dttrackpro.app.ui.theme.*

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoggedIn: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Graphite900, Graphite800)))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(SignalCyan.copy(alpha = 0.15f), MaterialTheme.shapes.large),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.GpsFixed, contentDescription = null, tint = SignalCyan, modifier = Modifier.size(36.dp))
            }

            Spacer(Modifier.height(20.dp))
            Text("DTTrack Pro", style = MaterialTheme.typography.displaySmall, color = Cloud100)
            Spacer(Modifier.height(4.dp))
            Text("Fleet tracking, in real time", style = MaterialTheme.typography.bodyLarge, color = Slate500)

            Spacer(Modifier.height(40.dp))

            OutlinedTextField(
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Filled.Mail, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                colors = loginFieldColors()
            )
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                colors = loginFieldColors()
            )

            AnimatedVisibility(visible = state.error != null) {
                Column {
                    Spacer(Modifier.height(10.dp))
                    Text(state.error.orEmpty(), color = DangerCoral, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { viewModel.login(onLoggedIn) },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SignalCyan, contentColor = Graphite900)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Graphite900, strokeWidth = 2.dp)
                } else {
                    Text("Sign in", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "Connects to your own GpsWox-compatible server",
                style = MaterialTheme.typography.bodyMedium,
                color = Slate500
            )
        }
    }
}

@Composable
private fun loginFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = SignalCyan,
    unfocusedBorderColor = Graphite600,
    focusedLabelColor = SignalCyan,
    unfocusedLabelColor = Slate500,
    focusedTextColor = Cloud100,
    unfocusedTextColor = Cloud100,
    cursorColor = SignalCyan,
)
