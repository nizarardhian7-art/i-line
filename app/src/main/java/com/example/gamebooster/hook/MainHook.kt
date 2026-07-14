package com.example.gamebooster.hook

import android.app.Activity
import com.example.gamebooster.overlay.OverlayManager
import com.example.gamebooster.util.Prefs
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class MainHook : IXposedHookLoadPackage {

    override fun handleLoadPackage(lpparam: LoadPackageParam) {

        // 1) Hook system framework: trik status thermal (berjalan di package "android")
        if (lpparam.packageName == "android") {
            ThermalHook.apply(lpparam)
            return
        }

        // 2) Hook game target: pasang bubble overlay saat Activity resume
        val targets = Prefs.getTargetPackages()
        if (lpparam.packageName !in targets) return

        XposedBridge.log("[GameBooster] Loaded into ${lpparam.packageName}")

        val overlayManagers = HashMap<Activity, OverlayManager>()

        XposedHelpers.findAndHookMethod(
            Activity::class.java,
            "onResume",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val activity = param.thisObject as Activity
                    val manager = overlayManagers.getOrPut(activity) { OverlayManager(activity) }
                    manager.attach()
                }
            }
        )

        XposedHelpers.findAndHookMethod(
            Activity::class.java,
            "onDestroy",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val activity = param.thisObject as Activity
                    overlayManagers.remove(activity)?.detach()
                }
            }
        )
    }
}
