package com.dttrackpro.app.data.model

import com.google.gson.annotations.SerializedName

data class DeviceGroup(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String? = null,
    @SerializedName("items") val items: List<Device> = emptyList()
)

data class Device(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("lat") val lat: Double = 0.0,
    @SerializedName("lng") val lng: Double = 0.0,
    @SerializedName("speed") val speed: Double = 0.0,
    @SerializedName("course") val course: Float = 0f,
    @SerializedName("altitude") val altitude: Double = 0.0,
    @SerializedName("online") val onlineStatus: String? = null,
    @SerializedName("time") val time: String? = null
) {
    val status: DeviceStatus
        get() = when {
            onlineStatus == "offline" -> DeviceStatus.OFFLINE
            speed > 3.0 -> DeviceStatus.MOVING
            else -> DeviceStatus.STOPPED
        }
}

enum class DeviceStatus { MOVING, STOPPED, OFFLINE }
