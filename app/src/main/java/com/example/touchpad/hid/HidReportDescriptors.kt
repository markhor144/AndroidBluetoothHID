package com.example.touchpad.hid

object HidReportDescriptors {
    const val REPORT_ID_MOUSE = 1
    const val REPORT_ID_KEYBOARD = 2

    // Boot mouse-like report: buttons(1), x(1), y(1), wheel(1)
    val MOUSE_REPORT_DESCRIPTOR = byteArrayOf(
        0x05, 0x01,       // Usage Page (Generic Desktop)
        0x09, 0x02,       // Usage (Mouse)
        0xA1.toByte(), 0x01, // Collection (Application)
        0x85.toByte(), REPORT_ID_MOUSE.toByte(), // Report ID
        0x09, 0x01,       // Usage (Pointer)
        0xA1.toByte(), 0x00, // Collection (Physical)
        0x05, 0x09,       // Usage Page (Buttons)
        0x19, 0x01,       // Usage Minimum (1)
        0x29, 0x03,       // Usage Maximum (3)
        0x15, 0x00,       // Logical Minimum (0)
        0x25, 0x01,       // Logical Maximum (1)
        0x95.toByte(), 0x03, // Report Count (3)
        0x75, 0x01,       // Report Size (1)
        0x81.toByte(), 0x02, // Input (Data,Var,Abs)
        0x95.toByte(), 0x01, // Report Count (1)
        0x75, 0x05,       // Report Size (5)
        0x81.toByte(), 0x03, // Input (Const,Var,Abs)
        0x05, 0x01,       // Usage Page (Generic Desktop)
        0x09, 0x30,       // Usage (X)
        0x09, 0x31,       // Usage (Y)
        0x09, 0x38,       // Usage (Wheel)
        0x15, 0x81.toByte(), // Logical Minimum (-127)
        0x25, 0x7F,       // Logical Maximum (127)
        0x75, 0x08,       // Report Size (8)
        0x95.toByte(), 0x03, // Report Count (3)
        0x81.toByte(), 0x06, // Input (Data,Var,Rel)
        0xC0.toByte(),    // End Collection (Physical)
        0xC0.toByte()     // End Collection (Application)
    )

    // Standard keyboard report: modifiers(1), reserved(1), keycodes(6)
    val KEYBOARD_REPORT_DESCRIPTOR = byteArrayOf(
        0x05, 0x01,       // Usage Page (Generic Desktop)
        0x09, 0x06,       // Usage (Keyboard)
        0xA1.toByte(), 0x01, // Collection (Application)
        0x85.toByte(), REPORT_ID_KEYBOARD.toByte(), // Report ID
        0x05, 0x07,       // Usage Page (Keyboard/Keypad)
        0x19, 0xE0.toByte(), // Usage Minimum (LeftControl)
        0x29, 0xE7.toByte(), // Usage Maximum (Right GUI)
        0x15, 0x00,       // Logical Minimum (0)
        0x25, 0x01,       // Logical Maximum (1)
        0x75, 0x01,       // Report Size (1)
        0x95.toByte(), 0x08, // Report Count (8)
        0x81.toByte(), 0x02, // Input (Data,Var,Abs)
        0x95.toByte(), 0x01, // Report Count (1)
        0x75, 0x08,       // Report Size (8)
        0x81.toByte(), 0x01, // Input (Const)
        0x95.toByte(), 0x06, // Report Count (6)
        0x75, 0x08,       // Report Size (8)
        0x15, 0x00,       // Logical Minimum (0)
        0x25, 0x65,       // Logical Maximum (101)
        0x05, 0x07,       // Usage Page (Keyboard/Keypad)
        0x19, 0x00,       // Usage Minimum (0)
        0x29, 0x65,       // Usage Maximum (101)
        0x81.toByte(), 0x00, // Input (Data,Array)
        0xC0.toByte()     // End Collection
    )

    val COMBINED_REPORT_DESCRIPTOR: ByteArray = MOUSE_REPORT_DESCRIPTOR + KEYBOARD_REPORT_DESCRIPTOR
}
