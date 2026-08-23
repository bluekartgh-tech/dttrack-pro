package com.dttrackpro.app.data.repository

import com.dttrackpro.app.data.model.Device
import com.dttrackpro.app.data.model.DeviceData
import com.dttrackpro.app.data.model.DeviceParams
import com.dttrackpro.app.data.model.Geofence
import com.dttrackpro.app.data.model.TripPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class DemoDeviceRepository(
    private val tickMs: Long = 3_000L
) : DeviceRepository {

    private data class Route(val center: Pair<Double, Double>, val radiusDeg: Double, val speedKmh: Double, val phase: Double)

    private val fleet = listOf(
        Triple(1L, "Truck 01 - MH12 AB 1234", "861234050012345"),
        Triple(2L, "Van 07 - Delivery", "861234050012346"),
        Triple(3L, "Bike 22 - Courier", "861234050012347"),
        Triple(4L, "Truck 09 - Cold Chain", "861234050012348"),
        Triple(5L, "Car 03 - Sales", "861234050012349"),
    )

    private val routes = fleet.mapIndexed { i, _ ->
        Route(
            center = Pair(19.076 + i * 0.01, 72.877 + i * 0.008),
            radiusDeg = 0.01 + i * 0.002,
            speedKmh = listOf(0.0, 42.0, 18.0, 55.0, 30.0)[i % 5],
            phase = Random.nextDouble(0.0, 2 * PI)
        )
    }

    private var t = 0.0

    override fun observeDevices(apiHash: String): Flow<Result<List<Device>>> = flow {
        while (true) {
            t += 0.12
            val devices: List<Device> = fleet.mapIndexed { i, triple ->
                val id: Long = triple.first
                val name: String = triple.second
                val imei: String = triple.third
                val route = routes[i]
                val moving = route.speedKmh > 0.5
                val angle = route.phase + (if (moving) t * (route.speedKmh / 40.0) else 0.0)
                val lat = route.center.first + route.radiusDeg * sin(angle)
                val lng = route.center.second + route.radiusDeg * cos(angle)
                val course = ((Math.toDegrees(angle + PI / 2)) + 360) % 360

                val iconName: String = when (i % 5) {
                    0, 3 -> "truck"
                    1 -> "van"
                    2 -> "bike"
                    else -> "car"
                }

                val params = DeviceParams(
                    ignition = moving,
                    fuelLevel = (40 + (id * 11) % 55).toInt(),
                    batteryLevel = (60 + (id * 7) % 40).toInt(),
                    odometerKm = 12000.0 + id * 3400 + t * 2,
                    engineHours = 812.5 + id * 40
                )

                val deviceData = DeviceData(
                    lat = lat,
                    lng = lng,
                    course = course.toFloat(),
                    speed = if (moving) route.speedKmh + Random.nextDouble(-3.0, 3.0) else 0.0,
                    altitude = 12.0,
                    params = params,
                    locationValid = id != 5L || (t % 40 < 32),
                    lastUpdate = "now",
                    address = "Near Route " + ('A' + i) + ", Zone " + (i + 1)
                )

                Device(
                    id = id,
                    name = name,
                    imei = imei,
                    icon = iconName,
                    protocol = "demo",
                    simNumber = null,
                    data = deviceData
                )
            }
            emit(Result.success(devices))
            delay(tickMs)
        }
    }

    override suspend fun getHistory(
        apiHash: String,
        deviceId: Long,
        dateStart: String,
        dateEnd: String
    ): List<TripPoint> {
        val index = (deviceId - 1).coerceIn(0, routes.size - 1L).toInt()
        val route = routes[index]
        return (0 until 60).map { step ->
            val angle = route.phase + step * 0.1
            TripPoint(
                lat = route.center.first + route.radiusDeg * sin(angle),
                lng = route.center.second + route.radiusDeg * cos(angle),
                speed = route.speedKmh,
                course = ((Math.toDegrees(angle + PI / 2)) + 360).toFloat() % 360,
                timestamp = "Step $step"
            )
        }
    }

    override suspend fun getGeofences(apiHash: String): List<Geofence> {
        return listOf(
            Geofence(id = 1, name = "Warehouse Zone", type = "circle", centerLat = 19.076, centerLng = 72.877, radiusMeters = 400.0, color = "#00D4D9"),
            Geofence(id = 2, name = "Restricted Yard", type = "circle", centerLat = 19.086, centerLng = 72.885, radiusMeters = 250.0, color = "#FF5C5C"),
        )
    }

    override suspend fun updateDevice(apiHash: String, deviceId: Long, name: String, icon: String) {
    }

    override suspend fun sendCommand(apiHash: String, deviceId: Long, command: String) {
    }

    override suspend fun createGeofence(apiHash: String, name: String, lat: Double, lng: Double, radiusMeters: Double) {
    }
}
