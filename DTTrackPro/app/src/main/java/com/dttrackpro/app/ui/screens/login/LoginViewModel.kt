package com.dttrackpro.app.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dttrackpro.app.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun onEmailChange(v: String) = _uiState.update { it.copy(email = v, error = null) }
    fun onPasswordChange(v: String) = _uiState.update { it.copy(password = v, error = null) }

    fun login(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(error = "Enter your email and password") }
            return
        }

        // Demo mode: skip the network round trip so the UI is explorable
        // immediately. Set AppContainer.USE_DEMO_DATA = false to go through
        // the real GpsWox-style /api/login call below.
        if (AppContainer.USE_DEMO_DATA) {
            onSuccess()
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            AppContainer.authRepository.login(state.email, state.password)
                .onSuccess { onSuccess() }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message ?: "Login failed") } }
        }
    }
}
