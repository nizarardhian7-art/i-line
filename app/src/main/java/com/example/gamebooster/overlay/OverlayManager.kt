package com.example.gamebooster.overlay

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.view.Gravity
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.example.gamebooster.util.PerfMonitor

@SuppressLint("ClickableViewAccessibility", "SetTextI18n")
class OverlayManager(private val activity: Activity) {

    private var bubbleView: TextView? = null
    private var perfMonitor: PerfMonitor? = null
    private var isFpsEnabled = false

    fun attach() {
        if (bubbleView != null) return
        showBubble()
    }

    fun detach() {
        stopFpsMonitor()
        val decorView = activity.window.decorView as? ViewGroup
        bubbleView?.let { decorView?.removeView(it) }
        bubbleView = null
    }

    private fun showBubble() {
        val decorView = activity.window.decorView as? ViewGroup ?: return

        val bubble = TextView(activity).apply {
            text = "⚡"
            textSize = 18f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#CC1E88E5"))
            setPadding(24, 24, 24, 24)
        }

        // Gunakan FrameLayout.LayoutParams karena DecorView pada dasarnya adalah FrameLayout
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            leftMargin = 50
            topMargin = 300
        }

        var initialX = 0
        var initialY = 0
        var touchX = 0f
        var touchY = 0f
        var moved = false

        bubble.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.leftMargin
                    initialY = params.topMargin
                    touchX = event.rawX
                    touchY = event.rawY
                    moved = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - touchX).toInt()
                    val dy = (event.rawY - touchY).toInt()
                    if (Math.abs(dx) > 10 || Math.abs(dy) > 10) moved = true
                    
                    params.leftMargin = initialX + dx
                    params.topMargin = initialY + dy
                    v.layoutParams = params
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

        decorView.addView(bubble, params)
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