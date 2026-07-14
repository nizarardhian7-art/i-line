package com.example.gamebooster

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.gamebooster.util.Prefs

/**
 * UI yang tampil saat modul dibuka langsung dari launcher (bukan overlay-nya).
 * Dipakai untuk mengaktifkan modul di LSPosed Manager (centang app ini di daftar
 * modul, lalu pilih scope: app ini sendiri, "android" (framework), dan game-nya).
 *
 * NOTE: menulis SharedPreferences biasa dari sini TIDAK otomatis kebaca oleh
 * XSharedPreferences kecuali file prefs mode-nya dibuat readable (world-readable)
 * untuk Android lama, atau pakai skema penyimpanan lain (ContentProvider) untuk
 * Android 7+. Untuk skeleton ini, target game masih memakai default hardcoded
 * di Prefs.kt — silakan sesuaikan dengan mekanisme storage yang aman untuk versi
 * Android yang ditarget.
 */
class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 60, 40, 40)
        }

        layout.addView(TextView(this).apply {
            text = "Game Booster – Aktifkan modul ini di LSPosed Manager,\n" +
                    "centang scope: aplikasi ini, \"android\" (System Framework),\n" +
                    "dan game yang ingin diberi overlay. Lalu reboot / force-stop game."
            textSize = 14f
        })

        layout.addView(TextView(this).apply {
            text = "\nGame target default:\n" + Prefs.defaultTargets.joinToString("\n")
            textSize = 13f
            setPadding(0, 24, 0, 24)
        })

        layout.addView(TextView(this).apply {
            text = "Trik sembunyikan status thermal"
        })
        layout.addView(Switch(this).apply {
            isChecked = true // simpan ke storage sesuai kebutuhan
        })

        scroll.addView(layout)
        setContentView(scroll)
    }
}
