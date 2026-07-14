package com.example.gamebooster.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import com.example.gamebooster.util.PerfMonitor
import com.example.gamebooster.util.Prefs

/**
 * Overlay ini dibuat dari dalam process game target (hasil hook Activity.onResume).
 * Karena berjalan di context aplikasi target yang sudah punya window token aktif,
 * kita pakai TYPE_APPLICATION_OVERLAY / fallback TYPE_PHONE agar tidak perlu
 * meminta izin "Draw over other apps" secara terpisah dalam banyak kasus root+Xposed.
 */
@SuppressLint("ClickableViewAccessibility", "SetTextI18n")
class OverlayManager(private val context: Context) {

    private val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var bubbleView: View? = null
    private var panelView: View? = null
    private var perfMonitor: PerfMonitor? = null
    private var panelShown = false

    fun attach() {
        if (bubbleView != null) return // sudah terpasang
        showBubble()
    }

    fun detach() {
        perfMonitor?.stop()
        bubbleView?.let { runCatching { wm.removeView(it) } }
        panelView?.let { runCatching { wm.removeView(it) } }
        bubbleView = null
        panelView = null
    }

    private fun overlayType(): Int =
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE

    private fun showBubble() {
        val bubble = TextView(context).apply {
            text = "⚡"
            textSize = 20f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#CC1E88E5"))
            setPadding(28, 28, 28, 28)
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

        // Drag untuk memindah bubble + tap untuk buka panel
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
                    if (Math.abs(dx) > 10 || Math.abs(dy) > 10) moved = true
                    params.x = initialX + dx
                    params.y = initialY + dy
                    wm.updateViewLayout(v, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!moved) togglePanel()
                    true
                }
                else -> false
            }
        }

        wm.addView(bubble, params)
        bubbleView = bubble
    }

    private fun togglePanel() {
        if (panelShown) hidePanel() else showPanel()
    }

    private fun showPanel() {
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#E6212121"))
            setPadding(32, 24, 32, 24)
        }

        val fpsText = TextView(context).apply {
            setTextColor(Color.WHITE)
            text = "FPS: --"
        }
        val cpuText = TextView(context).apply {
            setTextColor(Color.WHITE)
            text = "CPU: --%  |  --- MHz"
        }
        val thermalRow = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
        val thermalLabel = TextView(context).apply {
            setTextColor(Color.WHITE)
            text = "Trik thermal (sembunyikan status panas)"
            textSize = 12f
        }
        val thermalSwitch = Switch(context).apply {
            isChecked = Prefs.isThermalTrickEnabled()
            setOnCheckedChangeListener { _, isChecked ->
                // Status ini dibaca oleh ThermalHook secara live lewat Prefs;
                // di produksi sebaiknya simpan lewat ContentProvider/file shared, bukan langsung tulis di sini
                // karena XSharedPreferences read-only dari sisi hook.
            }
        }

        thermalRow.addView(thermalLabel)
        thermalRow.addView(thermalSwitch)

        val closeBtn = TextView(context).apply {
            setTextColor(Color.parseColor("#FF5252"))
            text = "Tutup"
            setPadding(0, 16, 0, 0)
            setOnClickListener { hidePanel() }
        }

        root.addView(fpsText)
        root.addView(cpuText)
        root.addView(thermalRow)
        root.addView(closeBtn)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 20
            y = 360
        }

        wm.addView(root, params)
        panelView = root
        panelShown = true

        perfMonitor = PerfMonitor { fps, cpu, freq ->
            fpsText.post {
                fpsText.text = "FPS: $fps"
                cpuText.text = "CPU: ${if (cpu >= 0) "$cpu%" else "n/a"}  |  ${if (freq > 0) "$freq MHz" else "n/a"}"
            }
        }.also { it.start() }
    }

    private fun hidePanel() {
        perfMonitor?.stop()
        panelView?.let { runCatching { wm.removeView(it) } }
        panelView = null
        panelShown = false
    }
}
