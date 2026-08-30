package com.dttrackpro.app.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dttrackpro.app.AppContainer
import com.dttrackpro.app.data.model.Device
import com.dttrackpro.app.data.model.TripPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class ReportsUiState(
    val devices: List<Device> = emptyList(),
    val selectedDeviceId: Long? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val totalDistanceKm: Double? = null,
    val avgSpeed: Double? = null,
    val maxSpeed: Double? = null,
    val pointCount: Int = 0,
)

class ReportsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState

    init {
        viewModelScope.launch {
            val hash = AppContainer.authRepository.sessionHash.firstOrNull() ?: "demo"
            AppContainer.deviceRepository.observeDevices(hash).collect { result ->
                result.getOrNull()?.let { devices ->
                    _uiState.update { it.copy(devices = devices) }
                }
            }
        }
    }

    fun selectDevice(deviceId: Long) {
        _uiState.update { it.copy(selectedDeviceId = deviceId, isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val hash = AppContainer.authRepository.sessionHash.firstOrNull() ?: "demo"
            val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            val end = Date()
            val start = Date(end.time - 24L * 60 * 60 * 1000)
            val result = runCatching {
                AppContainer.deviceRepository.getHistory(hash, deviceId, fmt.format(start), fmt.format(end))
            }
            result.onSuccess { points ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        totalDistanceKm = computeDistanceKm(points),
                        avgSpeed = points.map { p -> p.speed }.average().takeIf { avg -> !avg.isNaN() },
                        maxSpeed = points.maxOfOrNull { p -> p.speed },
                        pointCount = points.size,
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Could not load report") }
            }
        }
    }

    private fun computeDistanceKm(points: List<TripPoint>): Double {
        if (points.size < 2) return 0.0
        var total = 0.0
        for (i in 1 until points.size) {
            total += haversineKm(points[i - 1].lat, points[i - 1].lng, points[i].lat, points[i].lng)
        }
        return total
    }

    private fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusKm * c
    }
}
