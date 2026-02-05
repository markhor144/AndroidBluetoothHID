
package com.example.touchpad.ui

import androidx.compose.material3.*
import androidx.compose.runtime.*

@Composable
fun TouchpadScreen() {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Bluetooth Touchpad") }) }
    ) {
        Text("Touchpad area (gesture handling here)")
    }
}
