package com.dttrackpro.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.dttrackpro.app.ui.screens.dashboard.DashboardScreen
import com.dttrackpro.app.ui.screens.geofence.GeofenceScreen
import com.dttrackpro.app.ui.screens.history.HistoryScreen
import com.dttrackpro.app.ui.screens.login.LoginScreen
import com.dttrackpro.app.ui.screens.settings.SettingsScreen

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
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            })
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onOpenHistory = { deviceId -> navController.navigate(Screen.History.of(deviceId)) },
                onOpenGeofences = { navController.navigate(Screen.Geofences.route) },
                onOpenSettings = { navController.navigate(Screen.Settings.route) },
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

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onLoggedOut = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0)
                    }
                }
            )
        }
    }
}
