package com.dttrackpro.app.data.repository

import com.dttrackpro.app.data.model.Device
import com.dttrackpro.app.data.model.Geofence
import com.dttrackpro.app.data.model.TripPoint
import com.dttrackpro.app.data.remote.GpsWoxApiService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface DeviceRepository {
    /** Emits the full fleet list on an interval, matching GpsWox's polling model. */
    fun observeDevices(apiHash: String): Flow<List<Device>>

    suspend fun getHistory(apiHash: String, deviceId: Long, dateStart: String, dateEnd: String): List<TripPoint>

    suspend fun getGeofences(apiHash: String): List<Geofence>
}

/**
 * Talks to the real backend. This is what you plug in once your GpsWox-style
 * server is reachable — just switch the binding in AppContainer from
 * DemoDeviceRepository to this class.
 */
class RemoteDeviceRepository(
    private val api: GpsWoxApiService,
    private val pollIntervalMs: Long = 5_000L
) : DeviceRepository {

    override fun observeDevices(apiHash: String): Flow<List<Device>> = flow {
        while (true) {
            runCatching { api.getDevices(apiHash).data.orEmpty() }
                .onSuccess { emit(it) }
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
}
