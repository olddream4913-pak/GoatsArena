package com.clouddevicemanager.ui.screens.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.clouddevicemanager.data.repository.AuthRepository
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val loading: Boolean = false,
    val verificationId: String? = null,
    val feedback: String? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val currentUser: StateFlow<FirebaseUser?> = authRepository.currentUser.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )

    fun signInWithEmail(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(feedback = "Email and password are required.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }
            val result = authRepository.signInWithEmail(email.trim(), password)
            _uiState.update {
                it.copy(
                    loading = false,
                    feedback = result.exceptionOrNull()?.localizedMessage ?: "Signed in successfully."
                )
            }
        }
    }

    fun registerWithEmail(email: String, password: String) {
        if (email.isBlank() || password.length < 6) {
            _uiState.update { it.copy(feedback = "Use a valid email and a 6+ character password.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }
            val result = authRepository.registerWithEmail(email.trim(), password)
            _uiState.update {
                it.copy(
                    loading = false,
                    feedback = result.exceptionOrNull()?.localizedMessage ?: "Account created successfully."
                )
            }
        }
    }

    fun startPhoneVerification(activity: Activity?, phoneNumber: String) {
        if (activity == null) {
            _uiState.update { it.copy(feedback = "Unable to start phone auth on this device.") }
            return
        }

        if (phoneNumber.isBlank()) {
            _uiState.update { it.copy(feedback = "Phone number is required in E.164 format.") }
            return
        }

        _uiState.update { it.copy(loading = true) }

        authRepository.startPhoneVerification(
            activity = activity,
            phoneNumber = phoneNumber,
            onCodeSent = { id ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        verificationId = id,
                        feedback = "OTP sent. Enter the code to continue."
                    )
                }
            },
            onVerificationCompleted = { credential ->
                signInWithCredential(credential)
            },
            onError = { error ->
                _uiState.update {
                    it.copy(loading = false, feedback = error.localizedMessage ?: "Phone auth failed.")
                }
            }
        )
    }

    fun verifyOtp(otpCode: String) {
        val verificationId = _uiState.value.verificationId
        if (verificationId == null || otpCode.isBlank()) {
            _uiState.update { it.copy(feedback = "Verification id or OTP is missing.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }
            val result = authRepository.verifyOtp(verificationId, otpCode.trim())
            _uiState.update {
                it.copy(
                    loading = false,
                    feedback = result.exceptionOrNull()?.localizedMessage ?: "Phone sign-in successful."
                )
            }
        }
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            val result = authRepository.signInWithPhoneCredential(credential)
            _uiState.update {
                it.copy(
                    loading = false,
                    feedback = result.exceptionOrNull()?.localizedMessage ?: "Phone sign-in successful."
                )
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
        _uiState.update { it.copy(feedback = "Signed out.") }
    }

    fun consumeFeedback() {
        _uiState.update { it.copy(feedback = null) }
    }

    class Factory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(authRepository) as T
        }
    }
}