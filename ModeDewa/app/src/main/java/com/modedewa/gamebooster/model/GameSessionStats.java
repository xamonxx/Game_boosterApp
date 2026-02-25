package com.modedewa.gamebooster.model;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * GameSessionStats — Data statistik per sesi gaming.
 * Mencatat durasi, suhu, FPS, RAM, dan baterai selama sesi bermain.
 */
public class GameSessionStats {

    private static final String PREFS_NAME = "mode_dewa_sessions";
    private static final String KEY_SESSIONS = "session_history";
    private static final int MAX_SESSIONS = 50;

    /** Package name game yang dimainkan */
    public String gamePackage;

    /** Nama tampilan game */
    public String gameName;

    /** Profil yang digunakan saat sesi */
    public String profileUsed;

    /** Timestamp mulai sesi (epoch ms) */
    public long startTime;

    /** Timestamp akhir sesi (epoch ms) */
    public long endTime;

    /** Durasi bermain dalam milidetik */
    public long durationMs;

    /** Suhu maksimum selama sesi (Celsius) */
    public float maxTemp;

    /** Suhu rata-rata selama sesi (Celsius) */
    public float avgTemp;

    /** FPS rata-rata selama sesi */
    public int avgFps;

    /** FPS minimum selama sesi */
    public int minFps;

    /** RAM terpakai rata-rata (MB) */
    public long avgRamUsedMb;

    /** Baterai saat mulai (%) */
    public int batteryStart;

    /** Baterai saat selesai (%) */
    public int batteryEnd;

    /** Jumlah app yang di-disable selama sesi */
    public int appsDisabledCount;

    // --- Tracking fields (digunakan selama sesi berjalan) ---
    private transient int tempSampleCount = 0;
    private transient float tempSum = 0;
    private transient int fpsSampleCount = 0;
    private transient long fpsSum = 0;
    private transient int ramSampleCount = 0;
    private transient long ramSum = 0;

    /** Default constructor untuk deserialisasi dari JSON. */
    public GameSessionStats() {}

    /**
     * Mulai sesi baru.
     */
    public static GameSessionStats startSession(String gamePackage, String gameName,
                                                 String profile, int batteryPercent) {
        GameSessionStats stats = new GameSessionStats();
        stats.gamePackage = gamePackage;
        stats.gameName = gameName;
        stats.profileUsed = profile;
        stats.startTime = System.currentTimeMillis();
        stats.batteryStart = batteryPercent;
        stats.maxTemp = 0;
        return stats;
    }

    /**
     * Record sample data selama sesi (dipanggil periodik).
     */
    public void recordSample(float currentTemp, int currentFps, long ramUsedMb) {
        // Temperature
        tempSampleCount++;
        tempSum += currentTemp;
        if (currentTemp > maxTemp) {
            maxTemp = currentTemp;
        }
        avgTemp = tempSum / tempSampleCount;

        // FPS
        if (currentFps > 0) {
            fpsSampleCount++;
            fpsSum += currentFps;
            avgFps = (int) (fpsSum / fpsSampleCount);
            if (minFps == 0 || currentFps < minFps) {
                minFps = currentFps;
            }
        }

        // RAM
        ramSampleCount++;
        ramSum += ramUsedMb;
        avgRamUsedMb = ramSum / ramSampleCount;
    }

    /**
     * Akhiri sesi dan hitung durasi.
     */
    public void endSession(int batteryPercent) {
        endTime = System.currentTimeMillis();
        durationMs = endTime - startTime;
        batteryEnd = batteryPercent;
    }

    /**
     * Baterai yang terpakai selama sesi (%).
     */
    public int getBatteryUsed() {
        return Math.max(0, batteryStart - batteryEnd);
    }

    /**
     * Format durasi sebagai "HH:MM:SS".
     */
    public String getFormattedDuration() {
        long totalSec = durationMs / 1000;
        long hours = totalSec / 3600;
        long minutes = (totalSec % 3600) / 60;
        long seconds = totalSec % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    /**
     * Format durasi pendek untuk list (e.g. "1j 24m").
     */
    public String getShortDuration() {
        long totalMin = durationMs / 60000;
        if (totalMin < 60) return totalMin + "m";
        long hours = totalMin / 60;
        long mins = totalMin % 60;
        return hours + "j " + mins + "m";
    }

    // === PERSISTENCE ===

    /**
     * Simpan sesi ke history.
     */
    public void saveToHistory(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String existing = prefs.getString(KEY_SESSIONS, "[]");

        try {
            JSONArray sessions = new JSONArray(existing);

            // Tambahkan sesi baru di depan
            JSONObject session = new JSONObject();
            session.put("pkg", gamePackage);
            session.put("name", gameName);
            session.put("profile", profileUsed);
            session.put("start", startTime);
            session.put("end", endTime);
            session.put("duration", durationMs);
            session.put("maxTemp", maxTemp);
            session.put("avgTemp", avgTemp);
            session.put("avgFps", avgFps);
            session.put("minFps", minFps);
            session.put("avgRam", avgRamUsedMb);
            session.put("batStart", batteryStart);
            session.put("batEnd", batteryEnd);
            session.put("disabled", appsDisabledCount);

            // Insert di posisi 0 (terbaru di atas)
            JSONArray newSessions = new JSONArray();
            newSessions.put(session);
            for (int i = 0; i < Math.min(sessions.length(), MAX_SESSIONS - 1); i++) {
                newSessions.put(sessions.get(i));
            }

            prefs.edit().putString(KEY_SESSIONS, newSessions.toString()).apply();
        } catch (JSONException e) {
            // Ignore
        }
    }

    /**
     * Load riwayat sesi dari storage.
     */
    public static List<GameSessionStats> loadHistory(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_SESSIONS, "[]");
        List<GameSessionStats> list = new ArrayList<>();

        try {
            JSONArray sessions = new JSONArray(json);
            for (int i = 0; i < sessions.length(); i++) {
                JSONObject obj = sessions.getJSONObject(i);
                GameSessionStats stats = new GameSessionStats();
                stats.gamePackage = obj.optString("pkg");
                stats.gameName = obj.optString("name");
                stats.profileUsed = obj.optString("profile");
                stats.startTime = obj.optLong("start");
                stats.endTime = obj.optLong("end");
                stats.durationMs = obj.optLong("duration");
                stats.maxTemp = (float) obj.optDouble("maxTemp", 0);
                stats.avgTemp = (float) obj.optDouble("avgTemp", 0);
                stats.avgFps = obj.optInt("avgFps");
                stats.minFps = obj.optInt("minFps");
                stats.avgRamUsedMb = obj.optLong("avgRam");
                stats.batteryStart = obj.optInt("batStart");
                stats.batteryEnd = obj.optInt("batEnd");
                stats.appsDisabledCount = obj.optInt("disabled");
                list.add(stats);
            }
        } catch (JSONException e) {
            // Return empty list
        }
        return list;
    }

    /**
     * Ambil sesi terakhir (paling baru).
     */
    public static GameSessionStats getLastSession(Context context) {
        List<GameSessionStats> history = loadHistory(context);
        return history.isEmpty() ? null : history.get(0);
    }

    /**
     * Hapus semua riwayat sesi.
     */
    public static void clearHistory(Context context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putString(KEY_SESSIONS, "[]").apply();
    }
}
