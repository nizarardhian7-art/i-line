package com.example.gamebooster.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.TextView
import com.example.gamebooster.util.PerfMonitor

@SuppressLint("ClickableViewAccessibility", "SetTextI18n")
class OverlayManager(private val context: Context) {

    private val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var bubbleView: TextView? = null
    private var perfMonitor: PerfMonitor? = null
    private var isFpsEnabled = false

    fun attach() {
        if (bubbleView != null) return
        showBubble()
    }

    fun detach() {
        stopFpsMonitor()
        bubbleView?.let { runCatching { wm.removeView(it) } }
        bubbleView = null
    }

    private fun overlayType(): Int =
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE

    private fun showBubble() {
        val bubble = TextView(context).apply {
            text = "⚡"
            textSize = 18f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#CC1E88E5"))
            setPadding(24, 24, 24, 24)
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 300
        }

        var initialX = 0
        var initialY = 0
        var touchX = 0f
        var touchY = 0f
        var moved = false

        bubble.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    touchX = event.rawX
                    touchY = event.rawY
                    moved = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - touchX).toInt()
                    val dy = (event.rawY - touchY).toInt()
                    // Toleransi pergerakan agar tidak dianggap klik tidak sengaja saat menggeser
                    if (Math.abs(dx) > 10 || Math.abs(dy) > 10) moved = true
                    params.x = initialX + dx
                    params.y = initialY + dy
                    wm.updateViewLayout(v, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!moved) {
                        toggleFps()
                    }
                    true
                }
                else -> false
            }
        }

        wm.addView(bubble, params)
        bubbleView = bubble
    }

    private fun toggleFps() {
        isFpsEnabled = !isFpsEnabled
        if (isFpsEnabled) {
            startFpsMonitor()
        } else {
            stopFpsMonitor()
            bubbleView?.text = "⚡"
        }
    }

    private fun startFpsMonitor() {
        perfMonitor = PerfMonitor { fps ->
            bubbleView?.post {
                bubbleView?.text = "$fps FPS"
            }
        }.also { it.start() }
    }

    private fun stopFpsMonitor() {
        perfMonitor?.stop()
        perfMonitor = null
    }
}