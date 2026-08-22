package com.dttrackpro.app.ui.screens.livetracking

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dttrackpro.app.AppContainer
import com.dttrackpro.app.data.model.Device
import com.dttrackpro.app.data.model.Geofence
import com.dttrackpro.app.data.model.TripPoint
import com.dttrackpro.app.ui.components.MapTileMode
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class LiveTrackingUiState(
    val deviceId: Long = 0,
    val device: Device? = null,
    val tileMode: MapTileMode = MapTileMode.STANDARD,
    val trafficOn: Boolean = false,
    val trailOn: Boolean = false,
    val trailPoints: List<TripPoint> = emptyList(),
    val isLoadingTrail: Boolean = false,
    val geofencesOn: Boolean = false,
    val geofences: List<Geofence> = emptyList(),
    val commandInFlight: Boolean = false,
    val lastCommandResult: String? = null,
)

class LiveTrackingViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val deviceId: Long = checkNotNull(savedStateHandle["deviceId"])

    private val _uiState = MutableStateFlow(LiveTrackingUiState(deviceId = deviceId))
    val uiState: StateFlow<LiveTrackingUiState> = _uiState

    init {
        viewModelScope.launch {
            val hash = AppContainer.authRepository.sessionHash.firstOrNull() ?: "demo"
            AppContainer.deviceRepository.observeDevices(hash).collect { devices ->
                devices.find { it.id == deviceId }?.let { d ->
                    _uiState.update { it.copy(device = d) }
                }
            }
        }
    }

    fun toggleTileMode() = _uiState.update {
        it.copy(tileMode = if (it.tileMode == MapTileMode.STANDARD) MapTileMode.TOPOGRAPHIC else MapTileMode.STANDARD)
    }

    fun toggleTraffic() = _uiState.update { it.copy(trafficOn = !it.trafficOn) }

    fun toggleTrail() {
        val turningOn = !_uiState.value.trailOn
        _uiState.update { it.copy(trailOn = turningOn) }
        if (turningOn && _uiState.value.trailPoints.isEmpty()) {
            loadTrail()
        }
    }

    private fun loadTrail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingTrail = true) }
            val hash = AppContainer.authRepository.sessionHash.firstOrNull() ?: "demo"
            val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            val end = Date()
            val start = Date(end.time - 24L * 60 * 60 * 1000)
            val points = runCatching {
                AppContainer.deviceRepository.getHistory(hash, deviceId, fmt.format(start), fmt.format(end))
            }.getOrDefault(emptyList())
            _uiState.update { it.copy(trailPoints = points, isLoadingTrail = false) }
        }
    }

    fun toggleGeofences() {
        val turningOn = !_uiState.value.geofencesOn
        _uiState.update { it.copy(geofencesOn = turningOn) }
        if (turningOn && _uiState.value.geofences.isEmpty()) {
            viewModelScope.launch {
                val hash = AppContainer.authRepository.sessionHash.firstOrNull() ?: "demo"
                val fences = runCatching { AppContainer.deviceRepository.getGeofences(hash) }.getOrDefault(emptyList())
                _uiState.update { it.copy(geofences = fences) }
            }
        }
    }

    fun sendCommand(command: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(commandInFlight = true, lastCommandResult = null) }
            val hash = AppContainer.authRepository.sessionHash.firstOrNull() ?: "demo"
            val result = runCatching { AppContainer.deviceRepository.sendCommand(hash, deviceId, command) }
            _uiState.update {
                it.copy(
                    commandInFlight = false,
                    lastCommandResult = if (result.isSuccess) "\"$command\" sent" else "Failed to send command"
                )
            }
        }
    }

    fun clearCommandResult() = _uiState.update { it.copy(lastCommandResult = null) }
}
