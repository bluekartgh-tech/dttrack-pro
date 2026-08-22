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
    @SerializedName("imei") val imei: String = "",
    @SerializedName("icon") val icon: String? = null,
    @SerializedName("protocol") val protocol: String? = null,
    @SerializedName("sim_number") val simNumber: String? = null,
    @SerializedName("lat") val lat: Double = 0.0,
    @SerializedName("lng") val lng: Double = 0.0,
    @SerializedName("speed") val speed: Double = 0.0,
    @SerializedName("course") val course: Float = 0f,
    @SerializedName("altitude") val altitude: Double = 0.0,
    @SerializedName("online") val onlineStatus: String? = null,
    @SerializedName("time") val time: String? = null
) {
    val data: DeviceData
        get() = DeviceData(
            lat = lat,
            lng = lng,
            course = course,
            speed = speed,
            altitude = altitude,
            locationValid = onlineStatus != "offline",
            lastUpdate = time ?: ""
        )

    val status: DeviceStatus
        get() = when {
            onlineStatus == "offline" -> DeviceStatus.OFFLINE
            speed > 3.0 -> DeviceStatus.MOVING
            else -> DeviceStatus.STOPPED
        }
}

data class DeviceData(
    @SerializedName("lat") val lat: Double = 0.0,
    @SerializedName("lng") val lng: Double = 0.0,
    @SerializedName("course") val course: Float = 0f,
    @SerializedName("speed") val speed: Double = 0.0,
    @SerializedName("altitude") val altitude: Double = 0.0,
    @SerializedName("params") val params: DeviceParams = DeviceParams(),
    @SerializedName("loc_valid") val locationValid: Boolean = true,
    @SerializedName("last_update") val lastUpdate: String = "",
    @SerializedName("address") val address: String? = null
) {
    val status: DeviceStatus
        get() = when {
            !locationValid -> DeviceStatus.OFFLINE
            speed > 3.0 -> DeviceStatus.MOVING
            else -> DeviceStatus.STOPPED
        }
}

data class DeviceParams(
    @SerializedName("ignition") val ignition: Boolean = false,
    @SerializedName("fuel_level") val fuelLevel: Int? = null,
    @SerializedName("battery_level") val batteryLevel: Int? = null,
    @SerializedName("odometer") val odometerKm: Double? = null,
    @SerializedName("engine_hours") val engineHours: Double? = null
)

enum class DeviceStatus { MOVING, STOPPED, OFFLINE }
