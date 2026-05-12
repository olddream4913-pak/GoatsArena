package com.clouddevicemanager.navigation

sealed class Routes(val route: String) {
    data object Onboarding : Routes("onboarding")
    data object Login : Routes("login")
    data object Home : Routes("home")

    data object Device : Routes("device/{deviceId}") {
        const val ARG_DEVICE_ID = "deviceId"

        fun create(deviceId: String): String = "device/$deviceId"
    }
}