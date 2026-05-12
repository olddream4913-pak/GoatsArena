package com.clouddevicemanager.devices.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.clouddevicemanager.data.model.CloudDevice
import com.clouddevicemanager.data.repository.CloudDeviceRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DeviceDetailUiState(
    val device: CloudDevice? = null,
    val isLoading: Boolean = true,
    val isConnecting: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null
)

class DeviceDetailViewModel(
    private val repository: CloudDeviceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeviceDetailUiState())
    val uiState: StateFlow<DeviceDetailUiState> = _uiState.asStateFlow()
    private var observedDeviceId: String? = null
    private var observeJob: Job? = null

    fun observeDevice(deviceId: String) {
        observedDeviceId = deviceId
        if (deviceId.isBlank()) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Invalid device id.") }
            return
        }

        observeJob?.cancel()
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        observeJob = viewModelScope.launch {
            repository.observeDevices()
                .catch { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.localizedMessage ?: "Failed to load device.")
                    }
                }
                .collect { devices ->
                    val device = devices.firstOrNull { it.id == deviceId }
                    _uiState.update {
                        it.copy(
                            device = device,
                            isLoading = false,
                            errorMessage = if (device == null) "Device not found." else null
                        )
                    }
                }
        }
    }

    fun retryLoad() {
        observeDevice(observedDeviceId.orEmpty())
    }

    fun startSession(deviceId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isConnecting = true, errorMessage = null, infoMessage = "Starting session...") }
            repository.updateStatus(deviceId, "connecting")
            delay(1200)
            val result = repository.updateStatus(deviceId, "online")
            _uiState.update {
                it.copy(
                    isConnecting = false,
                    errorMessage = result.exceptionOrNull()?.localizedMessage,
                    infoMessage = if (result.isSuccess) "Session started." else null
                )
            }
        }
    }

    fun stopSession(deviceId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isConnecting = true, errorMessage = null, infoMessage = "Stopping session...") }
            repository.updateStatus(deviceId, "stopping")
            delay(900)
            val result = repository.updateStatus(deviceId, "offline")
            _uiState.update {
                it.copy(
                    isConnecting = false,
                    errorMessage = result.exceptionOrNull()?.localizedMessage,
                    infoMessage = if (result.isSuccess) "Session stopped." else null
                )
            }
        }
    }

    fun consumeMessage() {
        _uiState.update { it.copy(errorMessage = null, infoMessage = null) }
    }

    class Factory(private val repository: CloudDeviceRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DeviceDetailViewModel(repository) as T
        }
    }
}