package com.clouddevicemanager.ui.screens.devices

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.clouddevicemanager.data.model.Device

@Composable
fun DevicesScreen(
    paddingValues: PaddingValues,
    state: DevicesUiState,
    onAddDevice: (String, String, String) -> Unit,
    onTogglePower: (String, Boolean) -> Unit,
    onDeleteDevice: (String) -> Unit,
    onSignOut: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var osVersion by remember { mutableStateOf("Android 14") }
    var region by remember { mutableStateOf("US-East") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Cloud Device Manager", style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = onSignOut) {
                Text("Sign out")
            }
        }

        Text("Add a cloud device", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Device Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = osVersion,
                onValueChange = { osVersion = it },
                label = { Text("OS") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = region,
                onValueChange = { region = it },
                label = { Text("Region") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Button(
            onClick = {
                onAddDevice(name, osVersion, region)
                name = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Provision Device")
        }

        HorizontalDivider()

        Text("My devices", style = MaterialTheme.typography.titleMedium)

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.devices, key = { it.id }) { device ->
                DeviceRow(
                    device = device,
                    onTogglePower = { enabled -> onTogglePower(device.id, enabled) },
                    onDelete = { onDeleteDevice(device.id) }
                )
            }
        }
    }
}

@Composable
private fun DeviceRow(
    device: Device,
    onTogglePower: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(device.name, style = MaterialTheme.typography.titleMedium)
        Text("${device.osVersion} · ${device.region}", style = MaterialTheme.typography.bodySmall)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Switch(checked = device.isOnline, onCheckedChange = onTogglePower)
                Text(if (device.isOnline) "Online" else "Offline")
            }
            TextButton(onClick = onDelete) {
                Text("Delete")
            }
        }
        HorizontalDivider()
    }
}