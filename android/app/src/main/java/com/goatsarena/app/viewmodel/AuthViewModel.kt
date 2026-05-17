package com.goatsarena.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val phone: String = "",
    val otp: String = "",
    val displayName: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AuthViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onPhoneChange(value: String) {
        _uiState.update { it.copy(phone = value, errorMessage = null) }
    }

    fun onOtpChange(value: String) {
        _uiState.update { it.copy(otp = value, errorMessage = null) }
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(displayName = value, errorMessage = null) }
    }

    fun requestOtp(onSuccess: () -> Unit) {
        if (_uiState.value.phone.length < 8) {
            _uiState.update { it.copy(errorMessage = "Enter a valid phone number") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            delay(700)
            _uiState.update { it.copy(isLoading = false) }
            onSuccess()
        }
    }

    fun verifyOtp(onSuccess: () -> Unit) {
        if (_uiState.value.otp.length < 4) {
            _uiState.update { it.copy(errorMessage = "OTP must be at least 4 digits") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            delay(700)
            _uiState.update { it.copy(isLoading = false) }
            onSuccess()
        }
    }

    fun completeProfile(onSuccess: () -> Unit) {
        if (_uiState.value.displayName.length < 2) {
            _uiState.update { it.copy(errorMessage = "Please enter your name") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            delay(700)
            _uiState.update { it.copy(isLoading = false) }
            onSuccess()
        }
    }
}