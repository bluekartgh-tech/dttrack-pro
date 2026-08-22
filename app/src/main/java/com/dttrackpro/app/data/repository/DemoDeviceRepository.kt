package com.dttrackpro.app.data.repository

import com.dttrackpro.app.data.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.math.*
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
            center = 19.076 + i * 0.01 to 72.877 + i * 0.008,
            radiusDeg = 0.01 + i * 0.002,
            speedKmh = listOf(0.0, 42.0, 18.0, 55.0, 30.0)[i % 5],
            phase = Random.nextDouble(0.0, 2 * PI)
        )
    }

    private var t = 0.0

    override fun observeDevices(apiHash: String): Flow<List<Device>> = flow {
        while (true) {
            t += 0.12
            val devices = fleet.mapIndexed { i, (id, name, imei) ->
                val r = routes[i]
                val moving = r.speedKmh > 0.5
                val angle = r.phase + (if (moving) t * (r.speedKmh / 40.0) else 0.0)
                val lat = r.center.first + r.radiusDeg * sin(angle)
                val lng = r.center.second + r.radiusDeg * cos(angle)
                val course = ((Math.toDegrees(angle + PI / 2)) + 360) % 360

                Device(
                    id = id,
                    name = name,
                    imei = imei,
                    icon = when (i % 5) {
                        0, 3 -> "truck"
                        1 -> "van"
                        2 -> "bike"
                        else -> "car"
                    },
                    protocol = "demo",
                    data = DeviceData(
                        lat = lat,
                        lng = lng,
                        course = course.toFloat(),
                        speed = if (moving) r.speedKmh + Random.nextDouble(-3.0, 3.0) else 0.0,
                        altitude = 12.0,
                        params = DeviceParams(
                            ignition = moving,
                            fuelLevel = (40 + (id * 11) % 55).toInt(),
                            batteryLevel = (60 + (id * 7) % 40).toInt(),
                            odometerKm = 12000.0 + id * 3400 + t * 2,
                            engineHours = 812.5 + id * 40
                        ),
                        locationValid = id != 5L || (t % 40 < 32),
                        lastUpdate = "now",
                        address = "Near Route ${('A' + i.toInt())}, Zone ${i + 1}"
                    )
                )
            }
            emit(devices)
            delay(tickMs)
        }
    }

    override suspend fun getHistory(
        apiHash: String,
        deviceId: Long,
        dateStart: String,
        dateEnd: String
    ): List<TripPoint> {
        val r = routes[((deviceId - 1).coerceIn(0, routes.size - 1L)).toInt()]
        return (0 until 60).map { step ->
            val angle = r.phase + step * 0.1
            TripPoint(
                lat = r.center.first + r.radiusDeg * sin(angle),
                lng = r.center.second + r.radiusDeg * cos(angle),
                speed = r.speedKmh,
                course = ((Math.toDegrees(angle + PI / 2)) + 360).toFloat() % 360,
                timestamp = "Step $step"
            )
        }
    }

    override suspend fun getGeofences(apiHash: String): List<Geofence> = listOf(
        Geofence(id = 1, name = "Warehouse Zone", type = "circle", centerLat = 19.076, centerLng = 72.877, radiusMeters = 400.0, color = "#00D4D9"),
        Geofence(id = 2, name = "Restricted Yard", type = "circle", centerLat = 19.086, centerLng = 72.885, radiusMeters = 250.0, color = "#FF5C5C"),
    )

    override suspend fun updateDevice(apiHash: String, deviceId: Long, name: String, icon: String) {
    }

    override suspend fun sendCommand(apiHash: String, deviceId: Long, command: String) {
    }
}
