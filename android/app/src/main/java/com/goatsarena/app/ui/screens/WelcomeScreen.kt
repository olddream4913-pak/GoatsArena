package com.goatsarena.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.goatsarena.app.ui.components.PrimaryButton
import com.goatsarena.app.ui.theme.AppSpacing

@Composable
fun WelcomeScreen(onContinue: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(AppSpacing.lg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "GoatsArena",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "Join competitive goat tournaments with a clean and fast onboarding flow.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = AppSpacing.sm, bottom = AppSpacing.xl)
        )
        PrimaryButton(text = "Continue with Phone", onClick = onContinue)
    }
}