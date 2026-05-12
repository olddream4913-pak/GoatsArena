package com.clouddevicemanager.ui.screens.devices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.clouddevicemanager.data.model.Device
import com.clouddevicemanager.data.repository.DeviceRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DevicesUiState(
    val devices: List<Device> = emptyList(),
    val loading: Boolean = true,
    val feedback: String? = null
)

class DevicesViewModel(
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DevicesUiState())
    val uiState: StateFlow<DevicesUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    fun observeUser(userId: String?) {
        observeJob?.cancel()

        if (userId.isNullOrBlank()) {
            _uiState.value = DevicesUiState(loading = false)
            return
        }

        _uiState.update { it.copy(loading = true) }

        observeJob = viewModelScope.launch {
            deviceRepository.observeDevices(userId)
                .catch { error ->
                    _uiState.update {
                        it.copy(loading = false, feedback = error.localizedMessage ?: "Unable to load devices.")
                    }
                }
                .collect { devices ->
                    _uiState.update { it.copy(devices = devices, loading = false) }
                }
        }
    }

    fun addDevice(userId: String, name: String, osVersion: String, region: String) {
        if (name.isBlank()) {
            _uiState.update { it.copy(feedback = "Device name is required.") }
            return
        }

        viewModelScope.launch {
            val result = deviceRepository.addDevice(userId, name.trim(), osVersion.trim(), region.trim())
            _uiState.update {
                it.copy(feedback = result.exceptionOrNull()?.localizedMessage ?: "Device added.")
            }
        }
    }

    fun togglePower(userId: String, deviceId: String, isOnline: Boolean) {
        viewModelScope.launch {
            val result = deviceRepository.updatePowerState(userId, deviceId, isOnline)
            _uiState.update {
                it.copy(feedback = result.exceptionOrNull()?.localizedMessage ?: "Device state updated.")
            }
        }
    }

    fun deleteDevice(userId: String, deviceId: String) {
        viewModelScope.launch {
            val result = deviceRepository.deleteDevice(userId, deviceId)
            _uiState.update {
                it.copy(feedback = result.exceptionOrNull()?.localizedMessage ?: "Device removed.")
            }
        }
    }

    fun consumeFeedback() {
        _uiState.update { it.copy(feedback = null) }
    }

    class Factory(private val deviceRepository: DeviceRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DevicesViewModel(deviceRepository) as T
        }
    }
}