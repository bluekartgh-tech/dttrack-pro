package com.dttrackpro.app.data.model

import com.google.gson.annotations.SerializedName

/**
 * Mirrors the "device" object returned by GpsWox-style `get_devices` /
 * `get_device_data` endpoints. Field names follow the common GpsWox
 * convention (snake_case, lat/lng as strings-or-numbers depending on
 * backend). Adjust the @SerializedName values to match your backend's
 * exact schema if it differs.
 */
data class Device(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("imei") val imei: String,
    @SerializedName("icon") val icon: String? = null,
    @SerializedName("protocol") val protocol: String? = null,
    @SerializedName("sim_number") val simNumber: String? = null,
    @SerializedName("device_data") val data: DeviceData
)

data class DeviceData(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double,
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
