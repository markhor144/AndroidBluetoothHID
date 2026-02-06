package com.example.touchpad.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.touchpad.hid.HidManager

private val keyUsageMap = mapOf(
    'a' to 0x04,
    'b' to 0x05,
    'c' to 0x06,
    'd' to 0x07,
    'e' to 0x08,
    'f' to 0x09,
    'g' to 0x0A,
    'h' to 0x0B,
    'i' to 0x0C,
    'j' to 0x0D,
    'k' to 0x0E,
    'l' to 0x0F,
    'm' to 0x10,
    'n' to 0x11,
    'o' to 0x12,
    'p' to 0x13,
    'q' to 0x14,
    'r' to 0x15,
    's' to 0x16,
    't' to 0x17,
    'u' to 0x18,
    'v' to 0x19,
    'w' to 0x1A,
    'x' to 0x1B,
    'y' to 0x1C,
    'z' to 0x1D,
    '1' to 0x1E,
    '2' to 0x1F,
    '3' to 0x20,
    '4' to 0x21,
    '5' to 0x22,
    '6' to 0x23,
    '7' to 0x24,
    '8' to 0x25,
    '9' to 0x26,
    '0' to 0x27,
    ' ' to 0x2C
)

@Composable
fun KeyboardView(
    hidManager: HidManager,
    modifier: Modifier = Modifier
) {
    var typedText by rememberSaveable { mutableStateOf("") }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = typedText,
            onValueChange = {
                val last = it.lastOrNull()
                if (last != null) {
                    sendCharacter(hidManager, last)
                }
                typedText = ""
            },
            label = { Text("Type to send") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            QuickKeyButton("Space", modifier = Modifier.weight(1f)) {
                hidManager.sendKeyPress(0x2C)
            }
            QuickKeyButton("Enter", modifier = Modifier.weight(1f)) {
                hidManager.sendKeyPress(0x28)
            }
            QuickKeyButton("Backspace", modifier = Modifier.weight(1f)) {
                hidManager.sendKeyPress(0x2A)
            }
        }
    }
}

@Composable
private fun QuickKeyButton(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = modifier.padding(vertical = 2.dp)) {
        Text(label)
    }
}

private fun sendCharacter(hidManager: HidManager, character: Char) {
    val lower = character.lowercaseChar()
    val usage = keyUsageMap[lower] ?: return
    val needsShift = character.isLetter() && character.isUpperCase()
    val modifier = if (needsShift) 0x02 else 0x00 // Left shift
    hidManager.sendKeyPress(usage, modifier)
}
