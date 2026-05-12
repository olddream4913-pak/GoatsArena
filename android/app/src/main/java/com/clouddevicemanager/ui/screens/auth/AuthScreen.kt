package com.clouddevicemanager.ui.screens.auth

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun AuthScreen(
    paddingValues: PaddingValues,
    state: AuthUiState,
    activity: Activity?,
    onEmailSignIn: (String, String) -> Unit,
    onEmailRegister: (String, String) -> Unit,
    onStartPhoneAuth: (Activity?, String) -> Unit,
    onVerifyOtp: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Cloud Device Manager",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Sign in with email or phone OTP to manage your cloud devices.",
            style = MaterialTheme.typography.bodyMedium
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        Button(
            onClick = { onEmailSignIn(email, password) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.loading
        ) {
            Text("Sign In")
        }

        TextButton(
            onClick = { onEmailRegister(email, password) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.loading
        ) {
            Text("Create Account")
        }

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone (+1...) ") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Button(
            onClick = { onStartPhoneAuth(activity, phone) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.loading
        ) {
            Text("Send OTP")
        }

        OutlinedTextField(
            value = otp,
            onValueChange = { otp = it },
            label = { Text("OTP Code") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Button(
            onClick = { onVerifyOtp(otp) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.loading && state.verificationId != null
        ) {
            Text("Verify OTP")
        }

        if (state.loading) {
            CircularProgressIndicator()
        }
    }
}