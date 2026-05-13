package com.clouddevicemanager.devices.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.clouddevicemanager.di.AppContainer
import com.clouddevicemanager.ui.common.ErrorBanner
import com.clouddevicemanager.ui.common.LoadingOverlay
import com.clouddevicemanager.ui.common.PrimaryButton
import com.clouddevicemanager.ui.theme.AppSpacing

@Composable
fun DeviceDetailsScreen(
    innerPadding: PaddingValues,
    deviceId: String
) {
    val viewModel: DeviceDetailViewModel = viewModel(
        factory = DeviceDetailViewModel.Factory(AppContainer.cloudDeviceRepository)
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(deviceId) {
        viewModel.observeDevice(deviceId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = AppSpacing.screenHorizontal, vertical = AppSpacing.screenVertical)
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            val device = state.device
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.card)
            ) {
                Text(text = "Device Details", style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "ID: ${device?.id ?: deviceId}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                state.errorMessage?.let { message ->
                    ErrorBanner(message = message, onRetry = viewModel::retryLoad)
                }

                state.infoMessage?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppSpacing.card),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Name: ${device?.name ?: "Unknown"}", style = MaterialTheme.typography.bodyLarge)
                        Text("Region: ${device?.region ?: "-"}", style = MaterialTheme.typography.bodyMedium)
                        Text("Android: ${device?.androidVersion ?: "-"}", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "Status: ${device?.status ?: "-"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                RowActions(
                    isConnecting = state.isConnecting,
                    onStart = { viewModel.startSession(deviceId) },
                    onStop = { viewModel.stopSession(deviceId) }
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Streaming will appear here",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        LoadingOverlay(
            isVisible = state.isConnecting,
            message = "Updating session..."
        )
    }
}

@Composable
private fun RowActions(
    isConnecting: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        PrimaryButton(
            text = if (isConnecting) "Connecting..." else "Start Session",
            onClick = onStart,
            enabled = !isConnecting,
            modifier = Modifier.weight(1f)
        )

        TextButton(
            onClick = onStop,
            enabled = !isConnecting,
            modifier = Modifier.weight(1f)
        ) {
            Text("Stop Session")
        }
    }
}
