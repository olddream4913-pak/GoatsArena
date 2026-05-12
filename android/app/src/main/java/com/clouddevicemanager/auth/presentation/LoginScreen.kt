package com.clouddevicemanager.auth.presentation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.clouddevicemanager.di.AppContainer
import com.clouddevicemanager.ui.common.ErrorBanner
import com.clouddevicemanager.ui.common.LoadingOverlay
import com.clouddevicemanager.ui.common.PrimaryButton
import com.clouddevicemanager.ui.theme.AppSpacing
import androidx.compose.foundation.text.KeyboardOptions

@Composable
fun LoginScreen(
    innerPadding: PaddingValues,
    onLoginSuccess: () -> Unit
) {
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.Factory(AppContainer.authRepository)
    )
    val uiState = authViewModel.uiState.collectAsStateWithLifecycle().value
    val currentUser = authViewModel.currentUser.collectAsStateWithLifecycle().value
    val activity = LocalContext.current.findActivity()

    LaunchedEffect(currentUser?.uid) {
        if (currentUser != null) {
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = AppSpacing.screenHorizontal, vertical = AppSpacing.screenVertical),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.section)
    ) {
        Text(
            text = "Authenticate",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Use Phone OTP or Email to sign in to Cloud Device Manager.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        TabRow(selectedTabIndex = uiState.method.ordinal) {
            Tab(
                selected = uiState.method == AuthMethod.PHONE,
                onClick = { authViewModel.selectMethod(AuthMethod.PHONE) },
                text = { Text("Phone OTP") }
            )
            Tab(
                selected = uiState.method == AuthMethod.EMAIL,
                onClick = { authViewModel.selectMethod(AuthMethod.EMAIL) },
                text = { Text("Email") }
            )
        }

        if (uiState.method == AuthMethod.PHONE) {
            OutlinedTextField(
                value = uiState.phoneNumber,
                onValueChange = authViewModel::onPhoneChanged,
                label = { Text("Phone Number (+15551234567)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            Button(
                onClick = { authViewModel.sendOtp(activity) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                Text("Send Code")
            }

            OutlinedTextField(
                value = uiState.otpCode,
                onValueChange = authViewModel::onOtpChanged,
                label = { Text("OTP Code") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            PrimaryButton(
                text = "Verify OTP",
                onClick = authViewModel::verifyOtp,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading && uiState.verificationId != null
            )
        } else {
            OutlinedTextField(
                value = uiState.email,
                onValueChange = authViewModel::onEmailChanged,
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            OutlinedTextField(
                value = uiState.password,
                onValueChange = authViewModel::onPasswordChanged,
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PrimaryButton(
                    text = "Login",
                    onClick = authViewModel::loginWithEmail,
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isLoading
                )
                TextButton(
                    onClick = authViewModel::signupWithEmail,
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isLoading
                ) {
                    Text("Sign Up")
                }
            }
        }

        uiState.errorMessage?.let { message ->
            ErrorBanner(
                message = message,
                onRetry = {
                    if (uiState.method == AuthMethod.PHONE) {
                        authViewModel.sendOtp(activity)
                    } else {
                        authViewModel.loginWithEmail()
                    }
                }
            )
        }

        uiState.infoMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        LoadingOverlay(
            isVisible = uiState.isLoading,
            message = "Authenticating..."
        )
    }
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}