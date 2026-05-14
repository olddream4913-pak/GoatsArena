package com.clouddevicemanager.devices.presentation

import androidx.lifecycle.ViewModel
import com.clouddevicemanager.common.REGION_OPTIONS
import com.clouddevicemanager.common.ANDROID_VERSION_OPTIONS
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.clouddevicemanager.data.model.CloudDevice
import com.clouddevicemanager.data.repository.CloudDeviceRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DevicesUiState(
    val devices: List<CloudDevice> = emptyList(),
    val isLoading: Boolean = true,
    val isCreating: Boolean = false,
    val showCreateDialog: Boolean = false,
    val nameInput: String = "",
    val regionInput: String = REGION_OPTIONS.first(),
    val androidVersionInput: String = ANDROID_VERSION_OPTIONS.firstOrNull() ?: "14",
    val errorMessage: String? = null,
    val infoMessage: String? = null
)

class DevicesViewModel(
    private val repository: CloudDeviceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DevicesUiState())
    val uiState: StateFlow<DevicesUiState> = _uiState.asStateFlow()
    private var observeJob: Job? = null

    init {
        observeDevices()
    }

    fun showCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = true, errorMessage = null, infoMessage = null) }
    }

    fun dismissCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = false) }
    }

    fun onNameChanged(value: String) {
        _uiState.update { it.copy(nameInput = value, errorMessage = null) }
    }

    fun onRegionChanged(value: String) {
        _uiState.update { it.copy(regionInput = value, errorMessage = null) }
    }

    fun onAndroidVersionChanged(value: String) {
        _uiState.update { it.copy(androidVersionInput = value, errorMessage = null) }
    }

    fun createDevice() {
        val state = _uiState.value
        if (state.nameInput.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Device name is required.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true, errorMessage = null, infoMessage = null) }
            val result = repository.createDevice(
                CloudDevice(
                    name = state.nameInput.trim(),
                    region = state.regionInput,
                    androidVersion = state.androidVersionInput,
                    status = "provisioning"
                )
            )

            _uiState.update {
                if (result.isSuccess) {
                    it.copy(
                        isCreating = false,
                        showCreateDialog = false,
                        nameInput = "",
                        regionInput = REGION_OPTIONS.first(),
                        androidVersionInput = ANDROID_VERSION_OPTIONS[2],
                        infoMessage = "Device created successfully."
                    )
                } else {
                    it.copy(
                        isCreating = false,
                        errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Unable to create device."
                    )
                }
            }
        }
    }

    fun consumeMessage() {
        _uiState.update { it.copy(errorMessage = null, infoMessage = null) }
    }

    fun retryLoad() {
        observeDevices()
    }

    private fun observeDevices() {
        observeJob?.cancel()
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        observeJob = viewModelScope.launch {
            repository.observeDevices()
                .catch { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.localizedMessage ?: "Failed to load devices.")
                    }
                }
                .collect { devices ->
                    _uiState.update { it.copy(devices = devices, isLoading = false) }
                }
        }
    }

    class Factory(private val repository: CloudDeviceRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DevicesViewModel(repository) as T
        }
    }

    companion object {
        val REGION_OPTIONS = listOf("us-central1", "us-east1", "europe-west1", "asia-south1")
        val ANDROID_VERSION_OPTIONS = listOf("10", "13", "14", "15")
    }
}
