package com.dttrackpro.app.data.repository

import com.dttrackpro.app.data.model.Device
import com.dttrackpro.app.data.model.Geofence
import com.dttrackpro.app.data.model.TripPoint
import com.dttrackpro.app.data.remote.GpsWoxApiService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface DeviceRepository {
    fun observeDevices(apiHash: String): Flow<List<Device>>
    suspend fun getHistory(apiHash: String, deviceId: Long, dateStart: String, dateEnd: String): List<TripPoint>
    suspend fun getGeofences(apiHash: String): List<Geofence>
    suspend fun updateDevice(apiHash: String, deviceId: Long, name: String, icon: String)
    suspend fun sendCommand(apiHash: String, deviceId: Long, command: String)
}

class RemoteDeviceRepository(
    private val api: GpsWoxApiService,
    private val pollIntervalMs: Long = 5_000L
) : DeviceRepository {

    override fun observeDevices(apiHash: String): Flow<List<Device>> = flow {
        while (true) {
            runCatching {
                val groups = api.getDevices(apiHash)
                groups.flatMap { it.items }
            }.onSuccess { emit(it) }
            delay(pollIntervalMs)
        }
    }

    override suspend fun getHistory(
        apiHash: String,
        deviceId: Long,
        dateStart: String,
        dateEnd: String
    ): List<TripPoint> =
        api.getHistory(apiHash, deviceId, dateStart, dateEnd).data.orEmpty()

    override suspend fun getGeofences(apiHash: String): List<Geofence> =
        api.getGeofences(apiHash).data.orEmpty()

    override suspend fun updateDevice(apiHash: String, deviceId: Long, name: String, icon: String) {
        api.updateDevice(apiHash, deviceId, name, icon)
    }

    override suspend fun sendCommand(apiHash: String, deviceId: Long, command: String) {
        api.sendCommand(apiHash, deviceId, command)
    }
}
