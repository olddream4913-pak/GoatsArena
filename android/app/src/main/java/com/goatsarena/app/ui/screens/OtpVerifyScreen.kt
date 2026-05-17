package com.goatsarena.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goatsarena.app.ui.components.ErrorBanner
import com.goatsarena.app.ui.components.LoadingOverlay
import com.goatsarena.app.ui.components.PrimaryButton
import com.goatsarena.app.ui.theme.AppSpacing
import com.goatsarena.app.viewmodel.AuthViewModel

@Composable
fun OtpVerifyScreen(
    viewModel: AuthViewModel,
    onVerified: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            Text(text = "Verify OTP", style = MaterialTheme.typography.headlineMedium)
            Text(text = "Enter the code sent to ${state.phone}", style = MaterialTheme.typography.bodyLarge)

            ErrorBanner(message = state.errorMessage)

            OutlinedTextField(
                value = state.otp,
                onValueChange = viewModel::onOtpChange,
                label = { Text("OTP") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            PrimaryButton(
                text = "Verify",
                onClick = { viewModel.verifyOtp(onVerified) }
            )
        }

        LoadingOverlay(isLoading = state.isLoading)
    }
}