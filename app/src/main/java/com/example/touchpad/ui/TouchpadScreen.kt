package com.example.touchpad.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.touchpad.hid.HidManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TouchpadScreen() {
    val context = LocalContext.current
    val hidManager = remember(context.applicationContext) { HidManager(context.applicationContext) }

    DisposableEffect(hidManager) {
        hidManager.register()
        onDispose { hidManager.unregister() }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Bluetooth Touchpad") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "State: ${hidManager.state}")
            Text(text = hidManager.statusMessage)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { hidManager.sendMouseClick() }
                        )
                    }
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            hidManager.sendMouseMove(dragAmount.x.toInt(), dragAmount.y.toInt())
                        }
                    }
            )

            KeyboardView(hidManager = hidManager, modifier = Modifier.fillMaxWidth())
        }
    }
}
