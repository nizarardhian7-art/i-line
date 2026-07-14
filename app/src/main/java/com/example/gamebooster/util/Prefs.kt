package com.example.gamebooster.util

import de.robv.android.xposed.XSharedPreferences

object Prefs {
    private const val PACKAGE = "com.example.gamebooster"
    private const val FILE = "gamebooster_prefs"

    val defaultTargets = setOf(
        "com.mobiin.gp",              // PUBG Mobile
        "com.dts.freefireth",          // Free Fire
        "com.miHoYo.GenshinImpact",    // Genshin Impact
        "com.mobile.legends"           // Mobile Legends
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
}