package com.example.gamebooster

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.gamebooster.util.Prefs

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 60, 40, 40)
        }

        layout.addView(TextView(this).apply {
            text = "Game Booster (LSPosed) – Simpel\n\n" +
                    "Aktifkan modul ini di LSPosed Manager,\n" +
                    "lalu pilih/centang game target yang ingin diberi overlay. " +
                    "Setelah selesai, buka atau restart game tersebut."
            textSize = 15f
        })

        layout.addView(TextView(this).apply {
            text = "\nCara Pakai di Dalam Game:\n" +
                    "1. Ketuk ikon ⚡ untuk menampilkan FPS.\n" +
                    "2. Ketuk ikon FPS untuk mengembalikannya menjadi ⚡."
            textSize = 13f
        })

        layout.addView(TextView(this).apply {
            text = "\nGame target default:\n" + Prefs.defaultTargets.joinToString("\n")
            textSize = 13f
        })

        scroll.addView(layout)
        setContentView(scroll)
    }
}