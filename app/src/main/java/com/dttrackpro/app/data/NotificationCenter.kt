package com.dttrackpro.app.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

object NotificationCenter {

    enum class Category { OFFLINE, BACK_ONLINE, IGNITION, BATTERY, GEOFENCE }

    data class NotificationEvent(
        val id: String,
        val deviceId: Long,
        val deviceName: String,
        val message: String,
        val category: Category,
        val timestampMs: Long,
    )

    private val _events = MutableStateFlow<List<NotificationEvent>>(emptyList())
    val events: StateFlow<List<NotificationEvent>> = _events

    fun push(deviceId: Long, deviceName: String, message: String, category: Category) {
        val event = NotificationEvent(
            id = "${deviceId}_${System.currentTimeMillis()}",
            deviceId = deviceId,
            deviceName = deviceName,
            message = message,
            category = category,
            timestampMs = System.currentTimeMillis(),
        )
        _events.update { current -> (listOf(event) + current).take(100) }
    }
}
