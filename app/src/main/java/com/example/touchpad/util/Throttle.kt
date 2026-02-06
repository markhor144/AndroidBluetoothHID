package com.example.touchpad.util

import android.os.SystemClock

class Throttle(
    private val minIntervalMs: Long,
    private val timeProvider: () -> Long = { SystemClock.elapsedRealtime() }
) {
    private var lastSentAtMs: Long = 0L

    fun shouldSend(nowMs: Long = timeProvider()): Boolean {
        if (nowMs - lastSentAtMs < minIntervalMs) return false
        lastSentAtMs = nowMs
        return true
    }

    fun reset() {
        lastSentAtMs = 0L
    }
}
