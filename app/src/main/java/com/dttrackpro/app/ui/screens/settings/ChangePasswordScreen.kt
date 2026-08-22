package com.dttrackpro.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.dttrackpro.app.AppContainer
import com.dttrackpro.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(onBack: () -> Unit) {
    var current by remember { mutableStateOf("") }
    var new by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var success by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = Graphite900,
        topBar = {
            TopAppBar(
                title = { Text("Change password", color = Cloud100) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Cloud100)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Graphite900)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp)) {
            PasswordField("Current password", current) { current = it }
            Spacer(Modifier.height(12.dp))
            PasswordField("New password", new) { new = it }
            Spacer(Modifier.height(12.dp))
            PasswordField("Confirm new password", confirm) { confirm = it }

            error?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, color = DangerCoral, style = MaterialTheme.typography.bodyMedium)
            }
            if (success) {
                Spacer(Modifier.height(10.dp))
                Text("Password updated.", color = OnlineGreen, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    if (new.length < 6) {
                        error = "New password must be at least 6 characters"
                        return@Button
                    }
                    if (new != confirm) {
                        error = "Passwords don't match"
                        return@Button
                    }
                    error = null
                    isSubmitting = true
                    scope.launch {
                        AppContainer.authRepository.changePassword(current, new)
                            .onSuccess { isSubmitting = false; success = true }
                            .onFailure { e -> isSubmitting = false; error = e.message ?: "Could not update password" }
                    }
                },
                enabled = !isSubmitting,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SignalCyan, contentColor = Graphite900)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Graphite900, strokeWidth = 2.dp)
                } else {
                    Text("Update password")
                }
            }
        }
    }
}

@Composable
private fun PasswordField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SignalCyan,
            unfocusedBorderColor = Graphite600,
            focusedLabelColor = SignalCyan,
            unfocusedLabelColor = Slate500,
            focusedTextColor = Cloud100,
            unfocusedTextColor = Cloud100,
            cursorColor = SignalCyan,
        )
    )
}
