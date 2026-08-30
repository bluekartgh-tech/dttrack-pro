package com.dttrackpro.app.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dttrackpro.app.AppContainer
import com.dttrackpro.app.data.NotificationCenter
import com.dttrackpro.app.data.model.Device
import com.dttrackpro.app.data.model.DeviceStatus
import com.dttrackpro.app.data.remote.GeocodingRepository
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

    private var previousDevices: Map<Long, Device> = emptyMap()
    private var hasSeenFirstLoad = false

    init {
        viewModelScope.launch {
            val hash = AppContainer.authRepository.sessionHash.firstOrNull() ?: "demo"
            AppContainer.deviceRepository.observeDevices(hash).collect { result: Result<List<Device>> ->
                if (result.isSuccess) {
                    val devices: List<Device> = result.getOrDefault(emptyList())
                    detectNotableChanges(devices)
                    _uiState.update { state -> state.copy(devices = devices, isLoading = false, errorMessage = null) }
                    resolveMissingAddresses(devices)
                } else {
                    val message: String = result.exceptionOrNull()?.message ?: "Could not load vehicles"
                    _uiState.update { state -> state.copy(isLoading = false, errorMessage = message) }
                }
            }
        }
    }

    private fun detectNotableChanges(devices: List<Device>) {
        if (!hasSeenFirstLoad) {
            hasSeenFirstLoad = true
            previousDevices = devices.associateBy { it.id }
            return
        }

        devices.forEach { device ->
            val prev = previousDevices[device.id] ?: return@forEach

            val prevStatus = prev.data.status
            val newStatus = device.data.status
            if (prevStatus != DeviceStatus.OFFLINE && newStatus == DeviceStatus.OFFLINE) {
                NotificationCenter.push(device.id, device.name, "${device.name} went offline", NotificationCenter.Category.OFFLINE)
            } else if (prevStatus == DeviceStatus.OFFLINE && newStatus != DeviceStatus.OFFLINE) {
                NotificationCenter.push(device.id, device.name, "${device.name} is back online", NotificationCenter.Category.BACK_ONLINE)
            }

            val prevIgnition = prev.data.params.ignition
            val newIgnition = device.data.params.ignition
            if (prevIgnition != newIgnition) {
                val state = if (newIgnition) "on" else "off"
                NotificationCenter.push(device.id, device.name, "${device.name} ignition turned $state", NotificationCenter.Category.IGNITION)
            }

            val prevBattery = prev.data.params.batteryLevel
            val newBattery = device.data.params.batteryLevel
            if (prevBattery != null && newBattery != null && prevBattery >= 20 && newBattery < 20) {
                NotificationCenter.push(device.id, device.name, "${device.name} battery low ($newBattery%)", NotificationCenter.Category.BATTERY)
            }
        }

        previousDevices = devices.associateBy { it.id }
    }

    fun deviceById(id: Long): Device? = _uiState.value.devices.find { it.id == id }

    private fun resolveMissingAddresses(devices: List<Device>) {
        devices.filter { it.data.address == null }.forEach { device ->
            viewModelScope.launch {
                val resolved = GeocodingRepository.reverseGeocode(device.data.lat, device.data.lng)
                if (resolved != null) {
                    _uiState.update { state ->
                        state.copy(devices = state.devices.map { d ->
                            if (d.id == device.id) d.copy(data = d.data.copy(address = resolved)) else d
                        })
                    }
                }
            }
        }
    }

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
