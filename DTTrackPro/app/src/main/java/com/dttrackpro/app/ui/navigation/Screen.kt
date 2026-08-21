package com.dttrackpro.app.ui.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Dashboard : Screen("dashboard")
    data object History : Screen("history/{deviceId}") {
        fun of(deviceId: Long) = "history/$deviceId"
    }
    data object Geofences : Screen("geofences")
    data object Settings : Screen("settings")
}
