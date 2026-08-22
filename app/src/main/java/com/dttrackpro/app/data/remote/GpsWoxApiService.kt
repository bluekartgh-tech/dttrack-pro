package com.dttrackpro.app.data.remote

import com.dttrackpro.app.data.model.*
import retrofit2.http.*

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

    @FormUrlEncoded
    @POST("api/device/update")
    suspend fun updateDevice(
        @Field("user_api_hash") apiHash: String,
        @Field("device_id") deviceId: Long,
        @Field("name") name: String,
        @Field("icon") icon: String
    ): ApiEnvelope<Unit>

    @FormUrlEncoded
    @POST("api/change_password")
    suspend fun changePassword(
        @Field("user_api_hash") apiHash: String,
        @Field("old_password") oldPassword: String,
        @Field("new_password") newPassword: String
    ): ApiEnvelope<Unit>
}
