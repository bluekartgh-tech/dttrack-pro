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

private suspend fun <T> safeCall(block: suspend () -> T): Result<T> {
    return try {
        val value: T = block()
        Result.success(value)
    } catch (e: HttpException) {
        val body: String? = try { e.response()?.errorBody()?.string() } catch (inner: Exception) { null }
        val message: String = if (!body.isNullOrBlank()) "HTTP ${e.code()}: $body" else "HTTP ${e.code()}: ${e.message()}"
        Result.failure(Exception(message))
    } catch (e: IOException) {
        Result.failure(Exception("Network error — check the server URL and your connection (${e.message})"))
    } catch (e: Exception) {
        Result.failure(e)
    }
}

class RemoteDeviceRepository(
    private val api: GpsWoxApiService,
    private val pollIntervalMs: Long = 5_000L
) : DeviceRepository {

    override fun observeDevices(apiHash: String): Flow<Result<List<Device>>> = flow {
        while (true) {
            val result: Result<List<Device>> = safeCall {
                val envelope = api.getDevices(apiHash)
                val list: List<Device>? = envelope.data
                list ?: emptyList()
            }
            emit(result)
            delay(pollIntervalMs)
        }
    }

    override suspend fun getHistory(
        apiHash: String,
        deviceId: Long,
        dateStart: String,
        dateEnd: String
    ): List<TripPoint> {
        val envelope = api.getHistory(apiHash, deviceId, dateStart, dateEnd)
        return envelope.data ?: emptyList()
    }

    override suspend fun getGeofences(apiHash: String): List<Geofence> {
        val envelope = api.getGeofences(apiHash)
        return envelope.data ?: emptyList()
    }

    override suspend fun updateDevice(apiHash: String, deviceId: Long, name: String, icon: String) {
        api.updateDevice(apiHash, deviceId, name, icon)
    }

    override suspend fun sendCommand(apiHash: String, deviceId: Long, command: String) {
        api.sendCommand(apiHash, deviceId, command)
    }

    override suspend fun createGeofence(apiHash: String, name: String, lat: Double, lng: Double, radiusMeters: Double) {
        val fence = Geofence(id = 0, name = name, type = "circle", centerLat = lat, centerLng = lng, radiusMeters = radiusMeters)
        api.createGeofence(apiHash, fence)
    }
}
