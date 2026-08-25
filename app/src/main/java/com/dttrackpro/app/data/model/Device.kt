package com.dttrackpro.app.data.model

import com.google.gson.annotations.SerializedName

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
    @SerializedName("engine_hours") val engineHours: Double? = null,
    @SerializedName("signal") val signalStrength: String? = null,
    @SerializedName("power") val powerVoltage: String? = null,
    @SerializedName("satellites") val satelliteCount: String? = null,
)

enum class DeviceStatus { MOVING, STOPPED, OFFLINE }
