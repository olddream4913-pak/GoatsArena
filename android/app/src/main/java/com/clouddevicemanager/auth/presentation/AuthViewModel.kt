package com.clouddevicemanager.auth.presentation

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.clouddevicemanager.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AuthMethod {
    PHONE,
    EMAIL
}

data class AuthUiState(
    val method: AuthMethod = AuthMethod.PHONE,
    val phoneNumber: String = "",
    val otpCode: String = "",
    val verificationId: String? = null,
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null
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

    fun selectMethod(method: AuthMethod) {
        _uiState.update { it.copy(method = method, errorMessage = null, infoMessage = null) }
    }

    fun onPhoneChanged(value: String) {
        _uiState.update { it.copy(phoneNumber = value, errorMessage = null) }
    }

    fun onOtpChanged(value: String) {
        _uiState.update { it.copy(otpCode = value, errorMessage = null) }
    }

    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(email = value, errorMessage = null) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun sendOtp(activity: Activity?) {
        if (activity == null) {
            _uiState.update { it.copy(errorMessage = "Unable to access Activity for phone verification.") }
            return
        }

        val phone = _uiState.value.phoneNumber.trim()
        if (phone.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Enter phone number in E.164 format. Example: +15551234567") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }

        authRepository.startPhoneVerification(
            activity = activity,
            phoneNumber = phone,
            onCodeSent = { verificationId ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        verificationId = verificationId,
                        infoMessage = "Verification code sent."
                    )
                }
            },
            onVerificationCompleted = ::onAutoVerification,
            onError = { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.localizedMessage ?: "Phone verification failed."
                    )
                }
            }
        )
    }

    fun verifyOtp() {
        val currentState = _uiState.value
        val verificationId = currentState.verificationId
        val code = currentState.otpCode.trim()

        if (verificationId.isNullOrBlank()) {
            _uiState.update { it.copy(errorMessage = "Send code first.") }
            return
        }

        if (code.length < 6) {
            _uiState.update { it.copy(errorMessage = "Enter valid 6 digit OTP.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            val result = authRepository.verifyOtp(verificationId, code)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.localizedMessage,
                    infoMessage = if (result.isSuccess) "Phone login successful." else null
                )
            }
        }
    }

    fun loginWithEmail() {
        executeEmailAuth(isSignUp = false)
    }

    fun signupWithEmail() {
        executeEmailAuth(isSignUp = true)
    }

    fun clearMessage() {
        _uiState.update { it.copy(errorMessage = null, infoMessage = null) }
    }

    private fun onAutoVerification(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            val result = authRepository.signInWithPhoneCredential(credential)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.localizedMessage,
                    infoMessage = if (result.isSuccess) "Phone login successful." else null
                )
            }
        }
    }

    private fun executeEmailAuth(isSignUp: Boolean) {
        val currentState = _uiState.value
        val email = currentState.email.trim()
        val password = currentState.password

        if (email.isBlank() || !email.contains("@")) {
            _uiState.update { it.copy(errorMessage = "Enter a valid email.") }
            return
        }

        if (password.length < 6) {
            _uiState.update { it.copy(errorMessage = "Password must be at least 6 characters.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            val result = if (isSignUp) {
                authRepository.registerWithEmail(email, password)
            } else {
                authRepository.signInWithEmail(email, password)
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.localizedMessage,
                    infoMessage = if (result.isSuccess) {
                        if (isSignUp) "Account created successfully." else "Logged in successfully."
                    } else {
                        null
                    }
                )
            }
        }
    }

    class Factory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(authRepository) as T
        }
    }
}