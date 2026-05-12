package com.clouddevicemanager.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.clouddevicemanager.auth.presentation.LoginScreen
import com.clouddevicemanager.devices.presentation.DeviceDetailsScreen
import com.clouddevicemanager.devices.presentation.HomeScreen
import com.clouddevicemanager.ui.screens.onboarding.OnboardingScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NavGraph(
    navController: NavHostController,
    innerPadding: PaddingValues,
    isLoggedIn: Boolean
) {
    val startRoute = if (isLoggedIn) Routes.Home.route else Routes.Onboarding.route

    NavHost(
        navController = navController,
        startDestination = startRoute
    ) {
        composable(Routes.Onboarding.route) {
            OnboardingScreen(
                innerPadding = innerPadding,
                onGetStarted = { navController.navigate(Routes.Login.route) }
            )
        }

        composable(Routes.Login.route) {
            LoginScreen(
                innerPadding = innerPadding,
                onLoginSuccess = {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Home.route) {
            HomeScreen(
                innerPadding = innerPadding,
                onOpenDevice = { deviceId ->
                    navController.navigate(Routes.Device.create(deviceId))
                },
                onSignOut = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate(Routes.Login.route) {
                        popUpTo(Routes.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.Device.route,
            arguments = listOf(
                navArgument(Routes.Device.ARG_DEVICE_ID) { type = NavType.StringType }
            )
        ) { entry ->
            val deviceId = entry.arguments?.getString(Routes.Device.ARG_DEVICE_ID).orEmpty()
            DeviceDetailsScreen(
                innerPadding = innerPadding,
                deviceId = deviceId
            )
        }
    }
}