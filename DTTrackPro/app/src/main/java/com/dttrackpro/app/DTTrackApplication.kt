package com.dttrackpro.app

import android.app.Application
import org.osmdroid.config.Configuration

class DTTrackApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // osmdroid requires a user agent to avoid being throttled by tile servers,
        // and a writable cache dir for offline tile storage.
        Configuration.getInstance().apply {
            userAgentValue = packageName
            osmdroidBasePath = getExternalFilesDir(null) ?: filesDir
            osmdroidTileCache = java.io.File(osmdroidBasePath, "tiles")
        }

        AppContainer.init(this)
    }
}
