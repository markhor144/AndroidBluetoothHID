package com.example.touchpad.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun TouchpadScreen() {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Bluetooth Touchpad") }) }
    ) { innerPadding ->
        Text(
            text = "Touchpad area (gesture handling here)",
            modifier = Modifier.padding(innerPadding)
        )
    }
}
