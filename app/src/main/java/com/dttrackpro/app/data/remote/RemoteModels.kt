package com.dttrackpro.app.data.remote

import com.dttrackpro.app.data.model.Device
import com.dttrackpro.app.data.model.DeviceData
import com.dttrackpro.app.data.model.DeviceParams
import com.google.gson.annotations.SerializedName

data class DeviceGroup(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("items") val items: List<RemoteDevice>? = null
)

data class RemoteDevice(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String? = null,
    @SerializedName("online") val online: String? = null,
    @SerializedName("time") val time: String? = null,
    @SerializedName("lat") val lat: Double = 0.0,
    @SerializedName("lng") val lng: Double = 0.0,
    @SerializedName("course") val course: Float = 0f,
    @SerializedName("speed") val speed: Double = 0.0,
    @SerializedName("altitude") val altitude: Double = 0.0,
    @SerializedName("icon_color") val iconColor: String? = null,
    @SerializedName("address") val address: String? = null,
    @SerializedName("protocol") val protocol: String? = null,
    @SerializedName("total_distance") val totalDistance: Double? = null,
    @SerializedName("sensors") val sensors: List<RemoteSensor>? = null,
    @SerializedName("device_data") val deviceInfo: RemoteDeviceInfo? = null,
)

data class RemoteSensor(
    @SerializedName("type") val type: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("value") val value: String? = null,
)

data class RemoteDeviceInfo(
    @SerializedName("imei") val imei: String? = null,
    @SerializedName("sim_number") val simNumber: String? = null,
)

fun RemoteDevice.toDevice(): Device {
    val sensorList = sensors.orEmpty()
    val ignitionOn = sensorList.firstOrNull { it.type == "ignition" }
        ?.value?.equals("On", ignoreCase = true) ?: false
    val batteryPct = sensorList.firstOrNull { it.type == "battery" }
        ?.value?.trim()?.toIntOrNull()
    val signal = sensorList.firstOrNull { it.name == "SIGNAL" }?.value?.trim()
    val power = sensorList.firstOrNull { it.name == "Power" }?.value?.trim()
        ?.takeIf { it.isNotBlank() && it != "-" }
    val satellites = sensorList.firstOrNull { it.name == "GPS SAT" }?.value?.trim()

    val locationValid = online != "offline" && iconColor != "red"
    val cleanAddress = address?.takeIf { it.isNotBlank() && it != "-" }

    return Device(
        id = id,
        name = name ?: "Unnamed device",
        imei = deviceInfo?.imei ?: "",
        icon = null,
        protocol = protocol,
        simNumber = deviceInfo?.simNumber,
        data = DeviceData(
            lat = lat,
            lng = lng,
            course = course,
            speed = speed,
            altitude = altitude,
            params = DeviceParams(
                ignition = ignitionOn,
                fuelLevel = null,
                batteryLevel = batteryPct,
                odometerKm = totalDistance,
                engineHours = null,
                signalStrength = signal,
                powerVoltage = power,
                satelliteCount = satellites,
            ),
            locationValid = locationValid,
            lastUpdate = time ?: "",
            address = cleanAddress,
        )
    )
}
