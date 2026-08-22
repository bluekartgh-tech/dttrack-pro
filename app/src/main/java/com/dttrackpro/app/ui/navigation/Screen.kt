package com.dttrackpro.app.ui.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Main : Screen("main")
    data object LiveTracking : Screen("live_tracking/{deviceId}") {
        fun of(deviceId: Long) = "live_tracking/$deviceId"
    }
    data object Geofences : Screen("geofences")
    data object ChangePassword : Screen("change_password")
    data object NotificationSettings : Screen("notification_settings")
    data object VehicleManage : Screen("vehicle_manage")
}
