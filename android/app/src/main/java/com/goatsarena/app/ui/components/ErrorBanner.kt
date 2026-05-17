package com.goatsarena.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.goatsarena.app.ui.theme.AppSpacing

@Composable
fun ErrorBanner(message: String?, modifier: Modifier = Modifier) {
    if (message.isNullOrBlank()) return

    Text(
        text = message,
        color = MaterialTheme.colorScheme.onPrimary,
        fontWeight = FontWeight.Medium,
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.error)
            .padding(AppSpacing.md)
    )
}