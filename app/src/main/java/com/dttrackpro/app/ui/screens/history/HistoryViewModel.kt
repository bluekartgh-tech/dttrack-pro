package com.dttrackpro.app.ui.screens.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dttrackpro.app.AppContainer
import com.dttrackpro.app.data.model.TripPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HistoryUiState(
    val deviceId: Long = 0,
    val points: List<TripPoint> = emptyList(),
    val playheadIndex: Int = 0,
    val isPlaying: Boolean = false,
    val playbackSpeed: Float = 1f,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
) {
    val currentPoint: TripPoint? get() = points.getOrNull(playheadIndex)
}

class HistoryViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val deviceId: Long = checkNotNull(savedStateHandle["deviceId"])

    private val _uiState = MutableStateFlow(HistoryUiState(deviceId = deviceId))
    val uiState: StateFlow<HistoryUiState> = _uiState

    private var playbackJob: Job? = null

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val hash = AppContainer.authRepository.sessionHash.firstOrNull() ?: "demo"
            val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            val end = Date()
            val start = Date(end.time - 24L * 60 * 60 * 1000)
            val result = runCatching {
                AppContainer.deviceRepository.getHistory(hash, deviceId, fmt.format(start), fmt.format(end))
            }
            result.onSuccess { points ->
                _uiState.update { it.copy(points = points, isLoading = false) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Could not load trip history") }
            }
        }
    }

    fun seekTo(index: Int) = _uiState.update { it.copy(playheadIndex = index.coerceIn(0, (it.points.size - 1).coerceAtLeast(0))) }

    fun setSpeed(speed: Float) = _uiState.update { it.copy(playbackSpeed = speed) }

    fun togglePlay() {
        if (_uiState.value.isPlaying) {
            pause()
        } else {
            play()
        }
    }

    private fun play() {
        _uiState.update { it.copy(isPlaying = true) }
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            while (true) {
                val state = _uiState.value
                if (state.playheadIndex >= state.points.size - 1) {
                    _uiState.update { it.copy(isPlaying = false) }
                    break
                }
                delay((500 / state.playbackSpeed).toLong())
                _uiState.update { it.copy(playheadIndex = it.playheadIndex + 1) }
            }
        }
    }

    private fun pause() {
        playbackJob?.cancel()
        _uiState.update { it.copy(isPlaying = false) }
    }

    override fun onCleared() {
        playbackJob?.cancel()
    }
}
