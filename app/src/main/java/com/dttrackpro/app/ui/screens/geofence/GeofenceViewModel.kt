package com.dttrackpro.app.ui.screens.geofence

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dttrackpro.app.AppContainer
import com.dttrackpro.app.data.model.Geofence
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GeofenceUiState(
    val geofences: List<Geofence> = emptyList(),
    val isLoading: Boolean = true,
)

class GeofenceViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(GeofenceUiState())
    val uiState: StateFlow<GeofenceUiState> = _uiState

    init {
        viewModelScope.launch {
            val hash = AppContainer.authRepository.sessionHash.firstOrNull() ?: "demo"
            val fences = AppContainer.deviceRepository.getGeofences(hash)
            _uiState.update { it.copy(geofences = fences, isLoading = false) }
        }
    }
}
