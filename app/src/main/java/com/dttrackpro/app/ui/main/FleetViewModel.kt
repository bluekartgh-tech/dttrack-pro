package com.dttrackpro.app.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dttrackpro.app.AppContainer
import com.dttrackpro.app.data.model.Device
import com.dttrackpro.app.data.model.DeviceStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class FleetFilter { ALL, MOVING, STOPPED, OFFLINE }

data class FleetUiState(
    val devices: List<Device> = emptyList(),
    val query: String = "",
    val filter: FleetFilter = FleetFilter.ALL,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
) {
    val filteredDevices: List<Device>
        get() = devices
            .filter { filter == FleetFilter.ALL || it.data.status.matches(filter) }
            .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }

    val counts: Map<FleetFilter, Int>
        get() = mapOf(
            FleetFilter.ALL to devices.size,
            FleetFilter.MOVING to devices.count { it.data.status == DeviceStatus.MOVING },
            FleetFilter.STOPPED to devices.count { it.data.status == DeviceStatus.STOPPED },
            FleetFilter.OFFLINE to devices.count { it.data.status == DeviceStatus.OFFLINE },
        )

    val averageSpeed: Double
        get() = devices.filter { it.data.status == DeviceStatus.MOVING }
            .map { it.data.speed }.average().takeIf { !it.isNaN() } ?: 0.0
}

private fun DeviceStatus.matches(filter: FleetFilter) = when (filter) {
    FleetFilter.ALL -> true
    FleetFilter.MOVING -> this == DeviceStatus.MOVING
    FleetFilter.STOPPED -> this == DeviceStatus.STOPPED
    FleetFilter.OFFLINE -> this == DeviceStatus.OFFLINE
}

class FleetViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FleetUiState())
    val uiState: StateFlow<FleetUiState> = _uiState

    init {
        viewModelScope.launch {
            val hash = AppContainer.authRepository.sessionHash.firstOrNull() ?: "demo"
            AppContainer.deviceRepository.observeDevices(hash).collect { result: Result<List<Device>> ->
                if (result.isSuccess) {
                    val devices: List<Device> = result.getOrDefault(emptyList())
                    _uiState.update { state -> state.copy(devices = devices, isLoading = false, errorMessage = null) }
                } else {
                    val message: String = result.exceptionOrNull()?.message ?: "Could not load vehicles"
                    _uiState.update { state -> state.copy(isLoading = false, errorMessage = message) }
                }
            }
        }
    }

    fun deviceById(id: Long): Device? = _uiState.value.devices.find { it.id == id }

    fun updateVehicle(deviceId: Long, name: String, icon: String) {
        viewModelScope.launch {
            val hash = AppContainer.authRepository.sessionHash.firstOrNull() ?: "demo"
            runCatching { AppContainer.deviceRepository.updateDevice(hash, deviceId, name, icon) }
            _uiState.update { state ->
                state.copy(devices = state.devices.map {
                    if (it.id == deviceId) it.copy(name = name, icon = icon) else it
                })
            }
        }
    }

    fun onQueryChange(v: String) = _uiState.update { it.copy(query = v) }
    fun onFilterChange(f: FleetFilter) = _uiState.update { it.copy(filter = f) }
}
