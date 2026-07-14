# Game Booster (LSPosed Module) — Skeleton Project

Modul LSPosed: bubble overlay mengambang di dalam game → panel FPS, CPU, dan trik sembunyikan status thermal.

## Struktur

```
app/src/main/java/com/example/gamebooster/
├── hook/
│   ├── MainHook.kt        <- entry point (didaftarkan di assets/xposed_init)
│   └── ThermalHook.kt     <- hook system framework ("android") untuk trik thermal
├── overlay/
│   └── OverlayManager.kt  <- bubble + panel WindowManager
├── util/
│   ├── PerfMonitor.kt     <- hitung FPS (Choreographer) & CPU (/proc/stat)
│   └── Prefs.kt           <- baca daftar game target via XSharedPreferences
└── SettingsActivity.kt    <- UI saat modul dibuka manual dari launcher
```

## Cara pakai

1. Buka folder ini di **Android Studio** (Giraffe/Koala ke atas).
2. Biarkan Gradle sync (butuh internet untuk download dependency
   `de.robv.android.xposed:api:82` dan AndroidX).
3. Build APK: `Build > Build Bundle(s)/APK(s) > Build APK(s)`.
4. Install APK ke device yang sudah root + **LSPosed** terpasang (via Magisk/KernelSU).
5. Buka **LSPosed Manager** → tab Modules → aktifkan "Game Booster".
6. Set scope-nya: centang **package "android"** (System Framework, untuk trik thermal)
   dan **game yang ingin diberi overlay** (mis. PUBG Mobile, Free Fire, dst — daftar
   default ada di `Prefs.kt`).
7. Reboot device (untuk hook framework/`android`) lalu buka game targetnya.
8. Bubble ⚡ akan muncul di pojok layar → tap untuk expand panel FPS/CPU/thermal.

## Yang perlu disesuaikan sebelum dipakai serius

- **Daftar game target**: edit `Prefs.defaultTargets` sesuai game yang kamu mau,
  atau bangun mekanisme penyimpanan (ContentProvider) supaya bisa diedit dari
  `SettingsActivity` dan langsung terbaca oleh hook (saat ini `SettingsActivity`
  belum menyambungkan switch/preferensi ke storage yang bisa dibaca `XSharedPreferences`
  di Android modern — perlu ContentProvider karena SharedPreferences biasa
  di-sandbox sejak Android 7+).
- **Nama class thermal service** di `ThermalHook.kt` itu berbasis AOSP; di ROM
  vendor (MIUI, OneUI, ColorOS, dll) kadang beda nama/paket. Cek lewat
  `adb shell dumpsys thermalservice` atau decompile `services.jar` ROM
  targetmu kalau mau presisi.
- **Trik thermal ini tidak benar-benar mendinginkan HP** — hanya membuat game
  berhenti membaca status "panas" dari API resmi Android, supaya game tidak
  menurunkan grafis/FPS sendiri. Throttling nyata dari kernel/DVFS tetap berjalan.
- Izin `SYSTEM_ALERT_WINDOW` di manifest untuk jaga-jaga; karena overlay dipasang
  lewat context aplikasi target (hasil hook), pada banyak setup root+LSPosed ini
  cukup tanpa perlu user approve izin overlay secara manual, tapi bisa berbeda
  tergantung ROM.

## Testing tanpa device root

Kode ini tidak bisa di-build/jalan tanpa Android SDK + device/emulator rooted
dengan LSPosed. Kompilasi hanya bisa divalidasi di Android Studio kamu sendiri.
