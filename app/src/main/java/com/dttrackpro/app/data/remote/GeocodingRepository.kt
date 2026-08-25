package com.dttrackpro.app.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

object GeocodingRepository {

    private val client = OkHttpClient()
    private val cache = ConcurrentHashMap<String, String>()

    @Volatile
    private var lastRequestAt = 0L

    suspend fun reverseGeocode(lat: Double, lng: Double): String? = withContext(Dispatchers.IO) {
        val key = String.format(Locale.US, "%.4f,%.4f", lat, lng)
        cache[key]?.let { return@withContext it }

        val waitMs = (lastRequestAt + 1100) - System.currentTimeMillis()
        if (waitMs > 0) delay(waitMs)
        lastRequestAt = System.currentTimeMillis()

        val result = runCatching {
            val url = "https://nominatim.openstreetmap.org/reverse" +
                "?format=json&lat=$lat&lon=$lng&zoom=16&addressdetails=0"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "DTTrackPro-Android/1.0 (fleet tracking app)")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use null
                val body = response.body?.string() ?: return@use null
                JSONObject(body).optString("display_name", "").takeIf { it.isNotBlank() }
            }
        }.getOrNull()

        result?.also { cache[key] = it }
    }
}
