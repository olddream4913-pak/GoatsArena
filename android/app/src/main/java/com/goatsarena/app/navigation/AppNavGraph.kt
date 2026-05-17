package com.goatsarena.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.goatsarena.app.ui.screens.HomeScreen
import com.goatsarena.app.ui.screens.LoginPhoneScreen
import com.goatsarena.app.ui.screens.OtpVerifyScreen
import com.goatsarena.app.ui.screens.ProfileSetupScreen
import com.goatsarena.app.ui.screens.SplashScreen
import com.goatsarena.app.ui.screens.WelcomeScreen
import com.goatsarena.app.viewmodel.AuthViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val authViewModel: AuthViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        modifier = modifier
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(onReady = {
                navController.navigate(Routes.WELCOME) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            })
        }

        composable(Routes.WELCOME) {
            WelcomeScreen(onContinue = { navController.navigate(Routes.LOGIN_PHONE) })
        }

        composable(Routes.LOGIN_PHONE) {
            LoginPhoneScreen(
                viewModel = authViewModel,
                onContinue = { navController.navigate(Routes.OTP_VERIFY) }
            )
        }

        composable(Routes.OTP_VERIFY) {
            OtpVerifyScreen(
                viewModel = authViewModel,
                onVerified = { navController.navigate(Routes.PROFILE_SETUP) }
            )
        }

        composable(Routes.PROFILE_SETUP) {
            ProfileSetupScreen(
                viewModel = authViewModel,
                onComplete = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(displayName = authViewModel.uiState.value.displayName)
        }
    }
}