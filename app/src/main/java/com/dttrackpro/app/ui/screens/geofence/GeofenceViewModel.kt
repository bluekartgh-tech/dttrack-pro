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
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)

class GeofenceViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(GeofenceUiState())
    val uiState: StateFlow<GeofenceUiState> = _uiState

    init {
        refresh()
    }

    private fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val hash = AppContainer.authRepository.sessionHash.firstOrNull() ?: "demo"
            val fences = runCatching { AppContainer.deviceRepository.getGeofences(hash) }.getOrDefault(emptyList())
            _uiState.update { it.copy(geofences = fences, isLoading = false) }
        }
    }

    fun createGeofence(name: String, lat: Double, lng: Double, radiusMeters: Double, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val hash = AppContainer.authRepository.sessionHash.firstOrNull() ?: "demo"
            val result = runCatching { AppContainer.deviceRepository.createGeofence(hash, name, lat, lng, radiusMeters) }
            _uiState.update { it.copy(isSaving = false) }
            if (result.isSuccess) {
                refresh()
                onDone(true)
            } else {
                _uiState.update { it.copy(errorMessage = result.exceptionOrNull()?.message ?: "Could not create geofence") }
                onDone(false)
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
}
