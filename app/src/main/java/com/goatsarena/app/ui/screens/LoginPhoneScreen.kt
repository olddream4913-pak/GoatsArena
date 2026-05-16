package com.goatsarena.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
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
fun LoginPhoneScreen(
    viewModel: AuthViewModel,
    onContinue: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            Text(text = "Login", style = MaterialTheme.typography.headlineMedium)
            Text(text = "Enter your phone number to receive an OTP.", style = MaterialTheme.typography.bodyLarge)

            ErrorBanner(message = state.errorMessage)

            OutlinedTextField(
                value = state.phone,
                onValueChange = viewModel::onPhoneChange,
                label = { Text("Phone Number") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            PrimaryButton(
                text = "Send OTP",
                onClick = { viewModel.requestOtp(onContinue) }
            )
        }

        LoadingOverlay(isLoading = state.isLoading)
    }
}