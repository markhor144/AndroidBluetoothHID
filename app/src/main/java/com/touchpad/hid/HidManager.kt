package com.touchpad.hid

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.touchpad.util.Throttle
import java.util.concurrent.Executor

enum class HidConnectionState {
    Idle,
    Registering,
    Registered,
    Connecting,
    Connected,
    Disconnected,
    Error
}

class HidManager(private val context: Context) {
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private var hidDevice: BluetoothHidDevice? = null
    private var hostDevice: BluetoothDevice? = null

    private val mouseThrottle = Throttle(minIntervalMs = 8)
    private val keyboardThrottle = Throttle(minIntervalMs = 12)

    var state by mutableStateOf(HidConnectionState.Idle)
        private set

    var statusMessage by mutableStateOf("Idle")
        private set

    private val profileListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            if (profile != BluetoothProfile.HID_DEVICE) return
            hidDevice = proxy as BluetoothHidDevice
            registerAppInternal()
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                hidDevice = null
                hostDevice = null
                state = HidConnectionState.Disconnected
                statusMessage = "HID service disconnected"
            }
        }
    }

    private val callback = object : BluetoothHidDevice.Callback() {
        override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
            if (registered) {
                state = HidConnectionState.Registered
                statusMessage = "HID app registered"
            } else {
                state = HidConnectionState.Disconnected
                statusMessage = "HID app unregistered"
                hostDevice = null
            }
        }

        override fun onConnectionStateChanged(device: BluetoothDevice, newState: Int) {
            hostDevice = device
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    state = HidConnectionState.Connected
                    statusMessage = "Connected to ${device.name ?: device.address}"
                }
                BluetoothProfile.STATE_CONNECTING -> {
                    state = HidConnectionState.Connecting
                    statusMessage = "Connecting to ${device.name ?: device.address}"
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    state = HidConnectionState.Disconnected
                    statusMessage = "Disconnected"
                    hostDevice = null
                }
                else -> {
                    statusMessage = "Connection state: $newState"
                }
            }
        }
    }

    private val directExecutor = Executor { command -> command.run() }

    fun register() {
        if (!hasBluetoothConnectPermission()) {
            state = HidConnectionState.Error
            statusMessage = "Missing BLUETOOTH_CONNECT permission"
            return
        }
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            state = HidConnectionState.Error
            statusMessage = "Bluetooth is unavailable or disabled"
            return
        }
        state = HidConnectionState.Registering
        statusMessage = "Requesting HID profile"
        bluetoothAdapter.getProfileProxy(context, profileListener, BluetoothProfile.HID_DEVICE)
    }

    fun unregister() {
        hidDevice?.unregisterApp()
        bluetoothAdapter?.closeProfileProxy(BluetoothProfile.HID_DEVICE, hidDevice)
        hostDevice = null
        hidDevice = null
        state = HidConnectionState.Disconnected
        statusMessage = "HID profile released"
    }

    fun connect(device: BluetoothDevice): Boolean {
        val profile = hidDevice ?: return false
        state = HidConnectionState.Connecting
        statusMessage = "Connecting to ${device.name ?: device.address}"
        return profile.connect(device)
    }

    fun disconnect(): Boolean {
        val device = hostDevice ?: return false
        return hidDevice?.disconnect(device) ?: false
    }

    fun sendMouseMove(deltaX: Int, deltaY: Int, buttonsMask: Int = 0, wheel: Int = 0): Boolean {
        val device = hostDevice ?: return false
        val profile = hidDevice ?: return false
        if (!mouseThrottle.shouldSend()) return false
        val report = byteArrayOf(
            buttonsMask.toByte(),
            deltaX.coerceIn(-127, 127).toByte(),
            deltaY.coerceIn(-127, 127).toByte(),
            wheel.coerceIn(-127, 127).toByte()
        )
        return profile.sendReport(device, HidReportDescriptors.REPORT_ID_MOUSE, report)
    }

    fun sendMouseClick(buttonMask: Int = 1): Boolean {
        val device = hostDevice ?: return false
        val profile = hidDevice ?: return false
        val pressed = profile.sendReport(device, HidReportDescriptors.REPORT_ID_MOUSE, byteArrayOf(buttonMask.toByte(), 0, 0, 0))
        val released = profile.sendReport(device, HidReportDescriptors.REPORT_ID_MOUSE, byteArrayOf(0, 0, 0, 0))
        return pressed && released
    }

    fun sendKeyboardReport(modifier: Int = 0, keycodes: List<Int>): Boolean {
        val device = hostDevice ?: return false
        val profile = hidDevice ?: return false
        if (!keyboardThrottle.shouldSend()) return false

        val keys = IntArray(6)
        keycodes.take(6).forEachIndexed { index, code ->
            keys[index] = code.coerceIn(0, 255)
        }
        val payload = byteArrayOf(
            modifier.toByte(),
            0,
            keys[0].toByte(),
            keys[1].toByte(),
            keys[2].toByte(),
            keys[3].toByte(),
            keys[4].toByte(),
            keys[5].toByte()
        )
        return profile.sendReport(device, HidReportDescriptors.REPORT_ID_KEYBOARD, payload)
    }

    fun sendKeyPress(keycode: Int, modifier: Int = 0): Boolean {
        val down = sendKeyboardReport(modifier = modifier, keycodes = listOf(keycode))
        val up = sendKeyboardReport(modifier = 0, keycodes = emptyList())
        return down && up
    }

    private fun registerAppInternal() {
        val profile = hidDevice ?: return
        val sdp = BluetoothHidDeviceAppSdpSettings(
            "Android Touchpad",
            "Phone as touchpad + keyboard",
            "Android",
            0xC0.toByte(),
            HidReportDescriptors.COMBINED_REPORT_DESCRIPTOR
        )
        val registered = profile.registerApp(sdp, null, null, directExecutor, callback)
        if (!registered) {
            state = HidConnectionState.Error
            statusMessage = "Failed to register HID app"
        }
    }

    private fun hasBluetoothConnectPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}
