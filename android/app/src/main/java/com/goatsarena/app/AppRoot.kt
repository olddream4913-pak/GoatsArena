package com.goatsarena.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.goatsarena.app.navigation.AppNavGraph

@Composable
fun AppRoot() {
    Surface(modifier = Modifier.fillMaxSize()) {
        AppNavGraph()
    }
}