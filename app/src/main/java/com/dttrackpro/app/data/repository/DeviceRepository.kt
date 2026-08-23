package com.dttrackpro.app.data.repository

import com.dttrackpro.app.data.model.Device
import com.dttrackpro.app.data.model.Geofence
import com.dttrackpro.app.data.model.TripPoint
import com.dttrackpro.app.data.remote.GpsWoxApiService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException

interface DeviceRepository {
    fun observeDevices(apiHash: String): Flow<Result<List<Device>>>
    suspend fun getHistory(apiHash: String, deviceId: Long, dateStart: String, dateEnd: String): List<TripPoint>
    suspend fun getGeofences(apiHash: String): List<Geofence>
    suspend fun updateDevice(apiHash: String, deviceId: Long, name: String, icon: String)
    suspend fun sendCommand(apiHash: String, deviceId: Long, command: String)
    suspend fun createGeofence(apiHash: String, name: String, lat: Double, lng: Double, radiusMeters: Double)
}

private suspend fun <T> safeCall(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (e: HttpException) {
    val body = runCatching { e.response()?.errorBody()?.string() }.getOrNull()
    Result.failure(Exception("HTTP ${e.code()}${if (!body.isNullOrBlank()) ": $body" else ": ${e.message()}"}"))
} catch (e: IOException) {
    Result.failure(Exception("Network error — check the server URL and your connection (${e.message})"))
} catch (e: Exception) {
    Result.failure(e)
}

class RemoteDeviceRepository(
    private val api: GpsWoxApiService,
    private val pollIntervalMs: Long = 5_000L
) : DeviceRepository {

    override fun observeDevices(apiHash: String): Flow<Result<List<Device>>> = flow {
        while (true) {
            emit(safeCall { api.getDevices(apiHash).data.orEmpty() })
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

    override suspend fun createGeofence(apiHash: String, name: String, lat: Double, lng: Double, radiusMeters: Double) {
        api.createGeofence(
            apiHash,
            Geofence(id = 0, name = name, type = "circle", centerLat = lat, centerLng = lng, radiusMeters = radiusMeters)
        )
    }
}
