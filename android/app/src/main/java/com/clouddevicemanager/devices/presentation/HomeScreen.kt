package com.clouddevicemanager.devices.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.RowScope.weight
import androidx.compose.foundation.layout.ColumnScope.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.clouddevicemanager.data.model.CloudDevice
import com.clouddevicemanager.di.AppContainer
import com.clouddevicemanager.ui.common.ErrorBanner
import com.clouddevicemanager.ui.common.LoadingOverlay
import com.clouddevicemanager.ui.common.PrimaryButton
import com.clouddevicemanager.ui.theme.AppSpacing

@Composable
fun HomeScreen(
    innerPadding: PaddingValues,
    onOpenDevice: (String) -> Unit,
    onSignOut: () -> Unit
) {
    val viewModel: DevicesViewModel = viewModel(
        factory = DevicesViewModel.Factory(AppContainer.cloudDeviceRepository)
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::showCreateDialog) {
                Text("Create")
            }
        }
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(scaffoldPadding)
                .padding(horizontal = AppSpacing.screenHorizontal, vertical = AppSpacing.screenVertical),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.section)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Cloud Devices", style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = onSignOut) {
                    Text("Sign Out")
                }
            }

            state.errorMessage?.let { message ->
                ErrorBanner(
                    message = message,
                    onRetry = viewModel::retryLoad
                )
            }

            state.infoMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.devices, key = { device -> device.id }) { device ->
                        DeviceCard(device = device, onOpenDevice = onOpenDevice)
                    }

                    item {
                        Spacer(modifier = Modifier.padding(bottom = 72.dp))
                    }
                }
            }
        }

        LoadingOverlay(
            isVisible = state.isCreating,
            message = "Creating device..."
        )
    }

    if (state.showCreateDialog) {
        CreateDeviceDialog(
            state = state,
            onNameChanged = viewModel::onNameChanged,
            onRegionChanged = viewModel::onRegionChanged,
            onVersionChanged = viewModel::onAndroidVersionChanged,
            onDismiss = viewModel::dismissCreateDialog,
            onCreate = viewModel::createDevice
        )
    }
}

@Composable
private fun DeviceCard(
    device: CloudDevice,
    onOpenDevice: (String) -> Unit
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.card),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.item)
        ) {
            Text(text = device.name, style = MaterialTheme.typography.titleLarge)
            Text(
                text = "${device.region} • Android ${device.androidVersion}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Status: ${device.status}",
                    style = MaterialTheme.typography.bodyMedium
                )
                PrimaryButton(
                    text = "Open",
                    onClick = { onOpenDevice(device.id) },
                    enabled = device.id.isNotBlank()
                )
            }
        }
    }
}

@Composable
private fun CreateDeviceDialog(
    state: DevicesUiState,
    onNameChanged: (String) -> Unit,
    onRegionChanged: (String) -> Unit,
    onVersionChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onCreate: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Device") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.card)) {
                OutlinedTextField(
                    value = state.nameInput,
                    onValueChange = onNameChanged,
                    label = { Text("Device Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                SelectionDropdown(
                    label = "Region",
                    value = state.regionInput,
                    options = DevicesViewModel.REGION_OPTIONS,
                    onSelected = onRegionChanged
                )

                SelectionDropdown(
                    label = "Android Version",
                    value = state.androidVersionInput,
                    options = DevicesViewModel.ANDROID_VERSION_OPTIONS,
                    onSelected = onVersionChanged
                )
            }
        },
        confirmButton = {
            PrimaryButton(
                text = if (state.isCreating) "Creating..." else "Create",
                onClick = onCreate,
                enabled = !state.isCreating
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !state.isCreating) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun SelectionDropdown(
    label: String,
    value: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Column {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                        focusManager.clearFocus()
                    }
                )
            }
        }

        TextButton(onClick = { expanded = true }) {
            Text("Select $label")
        }
    }
}
