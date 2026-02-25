package com.modedewa.gamebooster.model;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * AppDetectionConfig — Konfigurasi untuk fitur auto-detect game.
 * Menyimpan dan membaca setting dari SharedPreferences.
 */
public class AppDetectionConfig {

    private static final String PREFS_NAME = "mode_dewa_auto_detect";
    private static final String KEY_ENABLED = "auto_detect_enabled";
    private static final String KEY_INTERVAL = "scan_interval_sec";
    private static final String KEY_BATTERY_AWARE = "battery_aware_enabled";
    private static final String KEY_BATTERY_THRESHOLD = "battery_threshold";
    private static final String KEY_NOTIF_CONTROLLER = "notification_controller";
    private static final String KEY_AUTO_DISABLE_BLOAT = "auto_disable_bloatware";
    private static final String KEY_AUTO_RE_ENABLE = "auto_re_enable_on_exit";
    private static final String KEY_SMART_PROFILE = "smart_profile_switching";
    private static final String KEY_CUSTOM_PACKAGES = "custom_app_packages";

    /** Auto-detect engine aktif atau tidak */
    public boolean isEnabled = true;

    /** Interval scan foreground app (dalam detik, 1-10) */
    public int scanIntervalSec = 3;

    /** Battery-aware mode: pause deteksi saat baterai rendah */
    public boolean batteryAwareEnabled = true;

    /** Threshold baterai untuk pause (dalam persen) */
    public int batteryThreshold = 20;

    /** Tampilkan notification controller saat gaming */
    public boolean notificationControllerEnabled = true;

    /** Otomatis disable app bawaan/bloatware saat gaming */
    public boolean autoDisableBloatware = false;

    /** Otomatis re-enable app yang di-disable setelah game ditutup */
    public boolean autoReEnableOnExit = true;

    /** Smart profile switching: auto-switch profil per game */
    public boolean smartProfileEnabled = true;

    /** Daftar package custom yang ditambahkan user (dipisah koma) */
    public String customPackages = "";

    /** Default constructor dengan nilai default. */
    public AppDetectionConfig() {}

    /**
     * Load konfigurasi dari SharedPreferences.
     */
    public static AppDetectionConfig load(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        AppDetectionConfig config = new AppDetectionConfig();

        config.isEnabled = prefs.getBoolean(KEY_ENABLED, true);
        config.scanIntervalSec = prefs.getInt(KEY_INTERVAL, 3);
        config.batteryAwareEnabled = prefs.getBoolean(KEY_BATTERY_AWARE, true);
        config.batteryThreshold = prefs.getInt(KEY_BATTERY_THRESHOLD, 20);
        config.notificationControllerEnabled = prefs.getBoolean(KEY_NOTIF_CONTROLLER, true);
        config.autoDisableBloatware = prefs.getBoolean(KEY_AUTO_DISABLE_BLOAT, false);
        config.autoReEnableOnExit = prefs.getBoolean(KEY_AUTO_RE_ENABLE, true);
        config.smartProfileEnabled = prefs.getBoolean(KEY_SMART_PROFILE, true);
        config.customPackages = prefs.getString(KEY_CUSTOM_PACKAGES, "");

        return config;
    }

    /**
     * Simpan konfigurasi ke SharedPreferences.
     */
    public void save(Context context) {
        SharedPreferences.Editor editor = context
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();

        editor.putBoolean(KEY_ENABLED, isEnabled);
        editor.putInt(KEY_INTERVAL, scanIntervalSec);
        editor.putBoolean(KEY_BATTERY_AWARE, batteryAwareEnabled);
        editor.putInt(KEY_BATTERY_THRESHOLD, batteryThreshold);
        editor.putBoolean(KEY_NOTIF_CONTROLLER, notificationControllerEnabled);
        editor.putBoolean(KEY_AUTO_DISABLE_BLOAT, autoDisableBloatware);
        editor.putBoolean(KEY_AUTO_RE_ENABLE, autoReEnableOnExit);
        editor.putBoolean(KEY_SMART_PROFILE, smartProfileEnabled);
        editor.putString(KEY_CUSTOM_PACKAGES, customPackages);

        editor.apply();
    }

    /**
     * Tambahkan custom package ke daftar.
     */
    public void addCustomPackage(String packageName) {
        if (customPackages.isEmpty()) {
            customPackages = packageName;
        } else if (!customPackages.contains(packageName)) {
            customPackages += "," + packageName;
        }
    }

    /**
     * Hapus custom package dari daftar.
     */
    public void removeCustomPackage(String packageName) {
        if (customPackages.contains("," + packageName)) {
            customPackages = customPackages.replace("," + packageName, "");
        } else if (customPackages.contains(packageName + ",")) {
            customPackages = customPackages.replace(packageName + ",", "");
        } else {
            customPackages = customPackages.replace(packageName, "");
        }
    }

    /**
     * Dapatkan array custom packages.
     */
    public String[] getCustomPackageArray() {
        if (customPackages == null || customPackages.trim().isEmpty()) {
            return new String[0];
        }
        return customPackages.split(",");
    }
}
