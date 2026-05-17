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
fun ProfileSetupScreen(
    viewModel: AuthViewModel,
    onComplete: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            Text(text = "Profile Setup", style = MaterialTheme.typography.headlineMedium)
            Text(text = "Set your display name to enter the arena.", style = MaterialTheme.typography.bodyLarge)

            ErrorBanner(message = state.errorMessage)

            OutlinedTextField(
                value = state.displayName,
                onValueChange = viewModel::onNameChange,
                label = { Text("Display Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            PrimaryButton(
                text = "Finish",
                onClick = { viewModel.completeProfile(onComplete) }
            )
        }

        LoadingOverlay(isLoading = state.isLoading)
    }
}