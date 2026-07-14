package com.example.gamebooster.util

import android.view.Choreographer

class PerfMonitor(private val onUpdate: (fps: Int) -> Unit) {

    private var frameCount = 0
    private var lastFpsTime = System.nanoTime()
    private var running = false

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!running) return
            frameCount++
            val now = System.nanoTime()
            val elapsedMs = (now - lastFpsTime) / 1_000_000
            if (elapsedMs >= 1000) {
                val fps = (frameCount * 1000L / elapsedMs).toInt()
                frameCount = 0
                lastFpsTime = now
                onUpdate(fps)
            }
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    fun start() {
        if (running) return
        running = true
        Choreographer.getInstance().postFrameCallback(frameCallback)
    }

    fun stop() {
        running = false
    }
}