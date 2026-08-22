package com.dttrackpro.app.data.repository

import com.dttrackpro.app.data.model.Device
import com.dttrackpro.app.data.model.Geofence
import com.dttrackpro.app.data.model.TripPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DemoDeviceRepository : DeviceRepository {

    private val demoDevices = listOf(
        Device(
            id = 1L,
            name = "Demo Tracker 1",
            imei = "123456789012345",
            lat = 30.7362,
            lng = 76.6450,
            speed = 45.0,
            onlineStatus = "ack"
        ),
        Device(
            id = 2L,
            name = "Demo Tracker 2",
            imei = "987654321098765",
            lat = 30.7320,
            lng = 76.6400,
            speed = 0.0,
            onlineStatus = "ack"
        )
    )

    override fun observeDevices(apiHash: String): Flow<List<Device>> = flow {
        while (true) {
            emit(demoDevices)
            delay(5_000L)
        }
    }

    override suspend fun getHistory(
        apiHash: String,
        deviceId: Long,
        dateStart: String,
        dateEnd: String
    ): List<TripPoint> = emptyList()

    override suspend fun getGeofences(apiHash: String): List<Geofence> = emptyList()

    override suspend fun updateDevice(apiHash: String, deviceId: Long, name: String, icon: String) {}

    override suspend fun sendCommand(apiHash: String, deviceId: Long, command: String) {}
}
