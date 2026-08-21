package com.dttrackpro.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dttrackpro.app.AppContainer
import com.dttrackpro.app.data.model.Device
import com.dttrackpro.app.data.model.DeviceStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class FleetFilter { ALL, MOVING, STOPPED, OFFLINE }

data class DashboardUiState(
    val devices: List<Device> = emptyList(),
    val query: String = "",
    val filter: FleetFilter = FleetFilter.ALL,
    val selectedDeviceId: Long? = null,
    val followSelected: Boolean = false,
    val isListExpanded: Boolean = true,
) {
    val filteredDevices: List<Device>
        get() = devices
            .filter { filter == FleetFilter.ALL || it.data.status.matches(filter) }
            .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }

    val selectedDevice: Device?
        get() = devices.find { it.id == selectedDeviceId }

    val counts: Map<FleetFilter, Int>
        get() = mapOf(
            FleetFilter.ALL to devices.size,
            FleetFilter.MOVING to devices.count { it.data.status == DeviceStatus.MOVING },
            FleetFilter.STOPPED to devices.count { it.data.status == DeviceStatus.STOPPED },
            FleetFilter.OFFLINE to devices.count { it.data.status == DeviceStatus.OFFLINE },
        )
}

private fun DeviceStatus.matches(filter: FleetFilter) = when (filter) {
    FleetFilter.ALL -> true
    FleetFilter.MOVING -> this == DeviceStatus.MOVING
    FleetFilter.STOPPED -> this == DeviceStatus.STOPPED
    FleetFilter.OFFLINE -> this == DeviceStatus.OFFLINE
}

class DashboardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    init {
        viewModelScope.launch {
            val hash = AppContainer.authRepository.sessionHash.firstOrNull() ?: "demo"
            AppContainer.deviceRepository.observeDevices(hash).collect { devices ->
                _uiState.update { it.copy(devices = devices) }
            }
        }
    }

    fun onQueryChange(v: String) = _uiState.update { it.copy(query = v) }
    fun onFilterChange(f: FleetFilter) = _uiState.update { it.copy(filter = f) }
    fun onListExpandedChange(expanded: Boolean) = _uiState.update { it.copy(isListExpanded = expanded) }

    fun selectDevice(id: Long?) = _uiState.update {
        it.copy(selectedDeviceId = id, followSelected = id != null)
    }

    fun toggleFollow() = _uiState.update { it.copy(followSelected = !it.followSelected) }
}
