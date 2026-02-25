package com.modedewa.gamebooster.model;

import android.graphics.drawable.Drawable;

/**
 * GameInfo — Data class holding detected game information.
 * Enhanced: tambah field auto-detect, profil, custom app, dan session tracking.
 */
public class GameInfo {
    public String packageName = "";
    public String displayName = "";
    public Drawable icon = null;
    public boolean isInstalled = false;
    public boolean isSelected = false;

    /** Apakah auto-detect aktif untuk game ini */
    public boolean isAutoDetectEnabled = true;

    /** Apakah game ini ditambahkan manual oleh user (bukan dari database) */
    public boolean isCustomAdded = false;

    /** Profil optimisasi terpilih: "light", "balanced", "ultra" */
    public String selectedProfile = "balanced";

    /** Apakah game sedang berjalan di foreground */
    public boolean isRunning = false;

    /** Timestamp terakhir kali game dideteksi running */
    public long lastDetectedTimestamp = 0;

    /** Total durasi bermain (dalam milidetik) sepanjang history */
    public long totalPlayTimeMs = 0;

    /** Default constructor. */
    public GameInfo() {}

    /**
     * Constructor dengan package name dan display name.
     *
     * @param packageName package name aplikasi game
     * @param displayName nama tampilan game untuk UI
     */
    public GameInfo(String packageName, String displayName) {
        this.packageName = packageName;
        this.displayName = displayName;
        this.isInstalled = true;
    }

    /**
     * Buat GameInfo untuk custom app yang ditambahkan user.
     */
    public static GameInfo createCustom(String packageName, String displayName) {
        GameInfo info = new GameInfo(packageName, displayName);
        info.isCustomAdded = true;
        info.selectedProfile = "light";
        return info;
    }

    /**
     * Get label profil untuk display di UI.
     */
    public String getProfileLabel() {
        switch (selectedProfile) {
            case "ultra": return "ULTRA";
            case "balanced": return "BALANCED";
            case "light": return "LIGHT";
            default: return "BALANCED";
        }
    }

    /**
     * Get status label untuk display (AUTO/MANUAL/MATI).
     */
    public String getStatusLabel() {
        if (!isAutoDetectEnabled) return "MATI";
        if (isCustomAdded) return "MANUAL";
        return "AUTO";
    }

    @Override
    public String toString() {
        return displayName + " (" + packageName + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameInfo gameInfo = (GameInfo) o;
        return packageName != null && packageName.equals(gameInfo.packageName);
    }

    @Override
    public int hashCode() {
        return packageName != null ? packageName.hashCode() : 0;
    }
}
