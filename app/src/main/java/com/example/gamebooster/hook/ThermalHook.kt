package com.example.gamebooster.hook

import com.example.gamebooster.util.Prefs
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

/**
 * PENTING - baca ini dulu:
 * Ini BUKAN kontrol thermal HAL/kernel sungguhan (itu perlu akses vendor driver,
 * di luar kemampuan Xposed). Yang dilakukan di sini hanya "menipu" API yang
 * dibaca aplikasi/game untuk mengecek status panas, supaya game tidak
 * menurunkan kualitas grafis/frame-rate sendiri saat device dianggap panas.
 *
 * Efek sampingnya: HP tetap bisa panas beneran, throttling CPU asli dari
 * kernel/DVFS tetap jalan. Ini cuma mencegah "self-throttle" di level aplikasi.
 *
 * Nama class/method di bawah bisa beda-beda antar versi Android/vendor (MIUI,
 * OneUI, ColorOS, dst), jadi semua dibungkus try-catch dan akan diam-diam
 * di-skip kalau method tidak ditemukan di ROM tersebut.
 */
object ThermalHook {

    fun apply(lpparam: LoadPackageParam) {
        if (!Prefs.isThermalTrickEnabled()) return

        hookThermalService(lpparam)
        hookPowerManagerSafeMode(lpparam)
    }

    // Target umum di AOSP: com.android.server.power.ThermalManagerService
    // method getCurrentThermalStatus() -> int (0 = THERMAL_STATUS_NONE)
    private fun hookThermalService(lpparam: LoadPackageParam) {
        try {
            val clazz = XposedHelpers.findClass(
                "com.android.server.power.ThermalManagerService",
                lpparam.classLoader
            )
            XposedBridge.hookAllMethods(clazz, "getCurrentThermalStatus", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    param.result = 0 // THERMAL_STATUS_NONE
                }
            })
            XposedBridge.log("[GameBooster] ThermalManagerService hooked")
        } catch (e: Throwable) {
            XposedBridge.log("[GameBooster] ThermalManagerService tidak ditemukan di ROM ini: ${e.message}")
        }

        // Beberapa ROM expose lewat IThermalService AIDL stub
        try {
            val stubClass = XposedHelpers.findClass(
                "android.os.IThermalService\$Stub\$Proxy",
                lpparam.classLoader
            )
            XposedBridge.hookAllMethods(stubClass, "getCurrentThermalStatus", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    param.result = 0
                }
            })
        } catch (e: Throwable) {
            // diam-diam skip, tidak semua ROM punya AIDL ini dengan nama sama
        }
    }

    // Beberapa game membaca PowerManager.isPowerSaveMode()/getThermalHeadroom() sebagai sinyal
    private fun hookPowerManagerSafeMode(lpparam: LoadPackageParam) {
        try {
            val pmClass = XposedHelpers.findClass("android.os.PowerManager", lpparam.classLoader)
            XposedBridge.hookAllMethods(pmClass, "getThermalHeadroom", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    param.result = 0.0f // 0 = jauh dari throttling
                }
            })
        } catch (e: Throwable) {
            XposedBridge.log("[GameBooster] getThermalHeadroom tidak tersedia: ${e.message}")
        }
    }
}
