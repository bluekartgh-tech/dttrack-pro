package com.dttrackpro.app.ui.screens.livetracking

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dttrackpro.app.AppContainer
import com.dttrackpro.app.data.model.Device
import com.dttrackpro.app.data.model.Geofence
import com.dttrackpro.app.data.model.TripPoint
import com.dttrackpro.app.data.remote.GeocodingRepository
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
    val showVehicleMenu: Boolean = false,
    val toast: String? = null,
)

class LiveTrackingViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val deviceId: Long = checkNotNull(savedStateHandle["deviceId"])

    private val _uiState = MutableStateFlow(LiveTrackingUiState(deviceId = deviceId))
    val uiState: StateFlow<LiveTrackingUiState> = _uiState

    init {
        viewModelScope.launch {
            val hash = AppContainer.authRepository.sessionHash.firstOrNull() ?: "demo"
            AppContainer.deviceRepository.observeDevices(hash).collect { result: Result<List<Device>> ->
                val devices: List<Device> = result.getOrDefault(emptyList())
                val found: Device? = devices.find { device -> device.id == deviceId }
                if (found != null) {
                    _uiState.update { state -> state.copy(device = found) }
                    if (found.data.address == null) {
                        resolveAddress(found)
                    }
                }
            }
        }
    }

    private fun resolveAddress(device: Device) {
        viewModelScope.launch {
            val resolved = GeocodingRepository.reverseGeocode(device.data.lat, device.data.lng)
            if (resolved != null) {
                _uiState.update { state ->
                    val current = state.device
                    if (current != null && current.id == device.id) {
                        state.copy(device = current.copy(data = current.data.copy(address = resolved)))
                    } else {
                        state
                    }
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
            val result = runCatching {
                AppContainer.deviceRepository.getHistory(hash, deviceId, fmt.format(start), fmt.format(end))
            }
            result.onSuccess { points ->
                _uiState.update { it.copy(trailPoints = points, isLoadingTrail = false) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoadingTrail = false, toast = "Trail: ${e.message ?: "could not load"}") }
            }
        }
    }

    fun toggleGeofences() {
        val turningOn = !_uiState.value.geofencesOn
        _uiState.update { it.copy(geofencesOn = turningOn) }
        if (turningOn && _uiState.value.geofences.isEmpty()) {
            viewModelScope.launch {
                val hash = AppContainer.authRepository.sessionHash.firstOrNull() ?: "demo"
                val result = runCatching { AppContainer.deviceRepository.getGeofences(hash) }
                result.onSuccess { fences ->
                    _uiState.update { it.copy(geofences = fences) }
                }.onFailure { e ->
                    _uiState.update { it.copy(toast = "Geofences: ${e.message ?: "could not load"}") }
                }
            }
        }
    }

    fun sendCommand(command: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(commandInFlight = true, toast = null) }
            val hash = AppContainer.authRepository.sessionHash.firstOrNull() ?: "demo"
            val result = runCatching { AppContainer.deviceRepository.sendCommand(hash, deviceId, command) }
            _uiState.update {
                it.copy(
                    commandInFlight = false,
                    toast = if (result.isSuccess) "\"$command\" sent" else "Command failed: ${result.exceptionOrNull()?.message ?: "unknown error"}"
                )
            }
        }
    }

    fun openVehicleMenu() = _uiState.update { it.copy(showVehicleMenu = true) }
    fun dismissVehicleMenu() = _uiState.update { it.copy(showVehicleMenu = false) }

    fun createGeofenceHere(name: String, radiusMeters: Double) {
        val device = _uiState.value.device ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(showVehicleMenu = false) }
            val hash = AppContainer.authRepository.sessionHash.firstOrNull() ?: "demo"
            val result = runCatching {
                AppContainer.deviceRepository.createGeofence(hash, name, device.data.lat, device.data.lng, radiusMeters)
            }
            _uiState.update {
                it.copy(toast = if (result.isSuccess) "Geofence \"$name\" created" else "Geofence: ${result.exceptionOrNull()?.message ?: "could not create"}")
            }
        }
    }

    fun changeIcon(icon: String) {
        val device = _uiState.value.device ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(showVehicleMenu = false) }
            val hash = AppContainer.authRepository.sessionHash.firstOrNull() ?: "demo"
            val result = runCatching { AppContainer.deviceRepository.updateDevice(hash, device.id, device.name, icon) }
            _uiState.update { state ->
                state.copy(
                    device = if (result.isSuccess) state.device?.copy(icon = icon) else state.device,
                    toast = if (result.isSuccess) "Icon updated" else "Icon: ${result.exceptionOrNull()?.message ?: "could not update"}"
                )
            }
        }
    }

    fun toggleParkingMode(turnOn: Boolean) {
        _uiState.update { it.copy(showVehicleMenu = false) }
        sendCommand(if (turnOn) "Parking mode on" else "Parking mode off")
    }

    fun clearToast() = _uiState.update { it.copy(toast = null) }
}
