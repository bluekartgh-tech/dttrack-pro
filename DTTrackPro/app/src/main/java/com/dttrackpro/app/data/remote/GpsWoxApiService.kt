package com.dttrackpro.app.data.remote

import com.dttrackpro.app.data.model.*
import retrofit2.http.*

/**
 * Retrofit contract for a GpsWox-style backend.
 *
 * GpsWox-family APIs typically authenticate via a `user_api_hash` issued
 * at login, then pass it as a query param on every subsequent call rather
 * than an Authorization header. This mirrors that pattern. If your backend
 * uses a Bearer token instead, swap the @Query("user_api_hash") params for
 * an interceptor-injected header (see ApiClient.kt) and drop them here.
 */
interface GpsWoxApiService {

    @POST("api/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @GET("api/get_devices")
    suspend fun getDevices(
        @Query("user_api_hash") apiHash: String
    ): ApiEnvelope<List<Device>>

    @GET("api/get_device_data")
    suspend fun getDeviceData(
        @Query("user_api_hash") apiHash: String,
        @Query("device_id") deviceId: Long
    ): ApiEnvelope<Device>

    @GET("api/get_history")
    suspend fun getHistory(
        @Query("user_api_hash") apiHash: String,
        @Query("device_id") deviceId: Long,
        @Query("date_start") dateStart: String,
        @Query("date_end") dateEnd: String
    ): ApiEnvelope<List<TripPoint>>

    @GET("api/get_geofences")
    suspend fun getGeofences(
        @Query("user_api_hash") apiHash: String
    ): ApiEnvelope<List<Geofence>>

    @POST("api/geofence/create")
    suspend fun createGeofence(
        @Query("user_api_hash") apiHash: String,
        @Body geofence: Geofence
    ): ApiEnvelope<Geofence>

    @FormUrlEncoded
    @POST("api/device/command")
    suspend fun sendCommand(
        @Field("user_api_hash") apiHash: String,
        @Field("device_id") deviceId: Long,
        @Field("command") command: String
    ): ApiEnvelope<Unit>
}
