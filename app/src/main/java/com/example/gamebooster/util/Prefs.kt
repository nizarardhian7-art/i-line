package com.example.gamebooster.util

import de.robv.android.xposed.XSharedPreferences

object Prefs {
    private const val PACKAGE = "com.example.gamebooster"
    private const val FILE = "gamebooster_prefs"

    // Daftar default game yang akan mendapat overlay. Bisa ditambah dari SettingsActivity.
    val defaultTargets = setOf(
        "com.tencent.ig",              // PUBG Mobile
        "com.dts.freefireth",          // Free Fire
        "com.miHoYo.GenshinImpact",    // Genshin Impact
        "com.mobile.legends"           // Mobile Legends (contoh)
    )

    fun getTargetPackages(): Set<String> {
        return try {
            val xsp = XSharedPreferences(PACKAGE, FILE)
            xsp.reload()
            val saved = xsp.getStringSet("target_packages", null)
            if (saved.isNullOrEmpty()) defaultTargets else saved
        } catch (e: Throwable) {
            defaultTargets
        }
    }

    fun isThermalTrickEnabled(): Boolean {
        return try {
            val xsp = XSharedPreferences(PACKAGE, FILE)
            xsp.reload()
            xsp.getBoolean("thermal_trick_enabled", true)
        } catch (e: Throwable) {
            true
        }
    }
}
