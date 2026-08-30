package com.dttrackpro.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.dttrackpro.app.ui.main.MainScaffoldScreen
import com.dttrackpro.app.ui.screens.geofence.GeofenceScreen
import com.dttrackpro.app.ui.screens.history.HistoryScreen
import com.dttrackpro.app.ui.screens.livetracking.LiveTrackingScreen
import com.dttrackpro.app.ui.screens.login.LoginScreen
import com.dttrackpro.app.ui.screens.notifications.NotificationsScreen
import com.dttrackpro.app.ui.screens.reports.ReportsScreen
import com.dttrackpro.app.ui.screens.settings.ChangePasswordScreen
import com.dttrackpro.app.ui.screens.settings.NotificationSettingsScreen
import com.dttrackpro.app.ui.screens.settings.VehicleManageScreen

@Composable
fun DTTrackNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        enterTransition = { fadeIn(tween(220)) + slideInHorizontally(tween(220)) { it / 6 } },
        exitTransition = { fadeOut(tween(180)) },
        popEnterTransition = { fadeIn(tween(220)) },
        popExitTransition = { fadeOut(tween(180)) + slideOutHorizontally(tween(180)) { it / 6 } },
    ) {
        composable(Screen.Login.route) {
            LoginScreen(onLoggedIn = {
                navController.navigate(Screen.Main.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            })
        }

        composable(Screen.Main.route) {
            MainScaffoldScreen(
                onVehicleTapped = { deviceId -> navController.navigate(Screen.LiveTracking.of(deviceId)) },
                onOpenChangePassword = { navController.navigate(Screen.ChangePassword.route) },
                onOpenNotifications = { navController.navigate(Screen.Notifications.route) },
                onOpenNotificationSettings = { navController.navigate(Screen.NotificationSettings.route) },
                onOpenGeofences = { navController.navigate(Screen.Geofences.route) },
                onOpenVehicleManage = { navController.navigate(Screen.VehicleManage.route) },
                onOpenReports = { navController.navigate(Screen.Reports.route) },
                onLoggedOut = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0)
                    }
                }
            )
        }

        composable(
            route = Screen.LiveTracking.route,
            arguments = listOf(navArgument("deviceId") { type = NavType.LongType }),
            deepLinks = listOf(navDeepLink { uriPattern = "dttrackpro://track/{deviceId}" })
        ) {
            LiveTrackingScreen(
                onBack = { navController.popBackStack() },
                onBellClick = { navController.navigate(Screen.Notifications.route) },
                onViewHistory = { deviceId -> navController.navigate(Screen.History.of(deviceId)) },
            )
        }

        composable(
            route = Screen.History.route,
            arguments = listOf(navArgument("deviceId") { type = NavType.LongType })
        ) {
            HistoryScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Geofences.route) {
            GeofenceScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.ChangePassword.route) {
            ChangePasswordScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.NotificationSettings.route) {
            NotificationSettingsScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen(
                onBack = { navController.popBackStack() },
                onOpenSettings = { navController.navigate(Screen.NotificationSettings.route) },
            )
        }

        composable(Screen.Reports.route) {
            ReportsScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.VehicleManage.route) {
            VehicleManageScreen(onBack = { navController.popBackStack() })
        }
    }
}
