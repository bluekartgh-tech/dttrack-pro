package com.dttrackpro.app.data.model

import com.google.gson.annotations.SerializedName

// ---- Auth ----

data class LoginRequest(
    @SerializedName("user_email") val email: String,
    @SerializedName("user_password") val password: String
)

data class LoginResponse(
    @SerializedName("status") val status: String,
    @SerializedName("user_api_hash") val userApiHash: String?,
    @SerializedName("user") val user: UserProfile?
)

data class UserProfile(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String
)

// ---- Generic envelope used by most GpsWox-style endpoints ----

data class ApiEnvelope<T>(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: T? = null
)

// ---- Trip history / playback ----

data class TripPoint(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double,
    @SerializedName("speed") val speed: Double,
    @SerializedName("course") val course: Float,
    @SerializedName("date") val timestamp: String
)

// ---- Geofences ----

data class Geofence(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("type") val type: String, // "circle" | "polygon"
    @SerializedName("center_lat") val centerLat: Double? = null,
    @SerializedName("center_lng") val centerLng: Double? = null,
    @SerializedName("radius") val radiusMeters: Double? = null,
    @SerializedName("color") val color: String = "#00D4D9"
)
