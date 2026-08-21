package com.dttrackpro.app

import android.content.Context
import com.dttrackpro.app.data.remote.ApiClient
import com.dttrackpro.app.data.repository.AuthRepository
import com.dttrackpro.app.data.repository.DemoDeviceRepository
import com.dttrackpro.app.data.repository.DeviceRepository
import com.dttrackpro.app.data.repository.RemoteDeviceRepository

/**
 * Lightweight service locator. Flip USE_DEMO_DATA to false once your
 * GpsWox-style backend is live and reachable at BuildConfig.API_BASE_URL.
 */
object AppContainer {

    const val USE_DEMO_DATA = true

    lateinit var authRepository: AuthRepository
        private set

    lateinit var deviceRepository: DeviceRepository
        private set

    fun init(context: Context) {
        authRepository = AuthRepository(context.applicationContext, ApiClient.service)
        deviceRepository = if (USE_DEMO_DATA) {
            DemoDeviceRepository()
        } else {
            RemoteDeviceRepository(ApiClient.service)
        }
    }
}
