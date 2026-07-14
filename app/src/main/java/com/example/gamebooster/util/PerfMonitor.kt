package com.example.gamebooster.util

import android.view.Choreographer
import java.io.RandomAccessFile

/**
 * Monitor performa ringan: FPS real-time dan estimasi pemakaian CPU.
 * Dijalankan di process game target (karena di-inject via Xposed hook),
 * jadi tidak butuh permission tambahan untuk baca /proc/stat milik sendiri.
 */
class PerfMonitor(private val onUpdate: (fps: Int, cpuPercent: Int, freqMhz: Int) -> Unit) {

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
                val cpu = readCpuUsagePercent()
                val freq = readCpuFreqMhz()
                onUpdate(fps, cpu, freq)
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

    // --- CPU usage total (seluruh sistem), dihitung dari delta /proc/stat ---
    private var lastIdle = 0L
    private var lastTotal = 0L

    private fun readCpuUsagePercent(): Int {
        return try {
            RandomAccessFile("/proc/stat", "r").use { reader ->
                val load = reader.readLine()
                val toks = load.split(" ").filter { it.isNotEmpty() }
                // toks[0] = "cpu", lalu user, nice, system, idle, iowait, irq, softirq
                val user = toks[1].toLong()
                val nice = toks[2].toLong()
                val system = toks[3].toLong()
                val idle = toks[4].toLong()
                val iowait = toks[5].toLong()
                val irq = toks[6].toLong()
                val softirq = toks[7].toLong()

                val total = user + nice + system + idle + iowait + irq + softirq
                val idleAll = idle + iowait

                val deltaTotal = total - lastTotal
                val deltaIdle = idleAll - lastIdle
                lastTotal = total
                lastIdle = idleAll

                if (deltaTotal <= 0) 0
                else (100 * (deltaTotal - deltaIdle) / deltaTotal).toInt().coerceIn(0, 100)
            }
        } catch (e: Exception) {
            -1 // gagal baca (device tertentu membatasi akses /proc/stat)
        }
    }

    private fun readCpuFreqMhz(): Int {
        return try {
            RandomAccessFile("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq", "r").use {
                (it.readLine().trim().toLong() / 1000).toInt()
            }
        } catch (e: Exception) {
            -1
        }
    }
}
