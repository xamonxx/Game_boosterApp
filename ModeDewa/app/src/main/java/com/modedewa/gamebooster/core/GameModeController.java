package com.modedewa.gamebooster.core;

import com.modedewa.gamebooster.util.ShizukuShell;
import android.util.Log;

/**
 * GameModeController — Manages Android Game Mode API, FPS unlock, and game priority.
 * Maps to modeGameON.sh Steps 9, 10 (game priority section).
 */
public class GameModeController {

    private static final String TAG = "GameModeController";

    // ═══════════════════════════════════════
    // ACTIVATE (Game Mode ON)
    // ═══════════════════════════════════════

    /** Set game to performance mode via Game Mode API */
    public static boolean setPerformanceMode(String packageName) {
        Log.d(TAG, "Setting performance mode for: " + packageName);
        return ShizukuShell.executeSilent(
                "cmd game mode performance " + packageName);
    }

    /** Unlock FPS - remove OEM FPS cap */
    public static boolean unlockFPS(String packageName) {
        Log.d(TAG, "Unlocking FPS for: " + packageName);
        return ShizukuShell.executeSilent(
                "device_config put game_overlay " + packageName + " mode=2,fps=0");
    }

    /** Set WindowManager downscale factor for performance */
    public static boolean setDownscale(String packageName, float factor) {
        Log.d(TAG, "Setting downscale " + factor + " for: " + packageName);
        return ShizukuShell.executeSilent(
                "device_config put game_overlay " + packageName +
                        " mode=2,downscaleFactor=" + factor);
    }

    /** Whitelist game from battery optimization (Doze) */
    public static boolean whitelistFromDoze(String packageName) {
        Log.d(TAG, "Whitelisting from Doze: " + packageName);
        return ShizukuShell.executeSilent(
                "dumpsys deviceidle whitelist +" + packageName);
    }

    /** Set app standby bucket to ACTIVE (highest priority) */
    public static boolean setStandbyActive(String packageName) {
        Log.d(TAG, "Setting standby bucket ACTIVE: " + packageName);
        return ShizukuShell.executeSilent(
                "am set-standby-bucket " + packageName + " active");
    }

    /** Allow game to run in background */
    public static boolean allowBackground(String packageName) {
        Log.d(TAG, "Allowing background run: " + packageName);
        boolean ok = true;
        ok &= ShizukuShell.executeSilent(
                "cmd appops set " + packageName + " RUN_IN_BACKGROUND allow");
        ok &= ShizukuShell.executeSilent(
                "cmd appops set " + packageName + " RUN_ANY_IN_BACKGROUND allow");
        return ok;
    }

    /** Set immersive/fullscreen mode for game */
    public static boolean setImmersiveMode(String packageName) {
        Log.d(TAG, "Setting immersive mode: " + packageName);
        return ShizukuShell.putSetting("global", "policy_control",
                "immersive.full=" + packageName);
    }

    /** Launch the game */
    public static boolean launchGame(String packageName) {
        Log.d(TAG, "Launching game: " + packageName);
        return ShizukuShell.executeSilent(
                "monkey -p " + packageName + " -c android.intent.category.LAUNCHER 1 2>/dev/null");
    }

    /** Apply all game optimizations */
    public static boolean applyAll(String packageName) {
        boolean ok = true;
        ok &= setPerformanceMode(packageName);
        ok &= unlockFPS(packageName);
        ok &= whitelistFromDoze(packageName);
        ok &= setStandbyActive(packageName);
        ok &= allowBackground(packageName);
        ok &= setImmersiveMode(packageName);
        return ok;
    }

    // ═══════════════════════════════════════
    // DEACTIVATE (Game Mode OFF)
    // ═══════════════════════════════════════

    /** Restore game to standard mode */
    public static boolean restoreStandardMode(String packageName) {
        Log.d(TAG, "Restoring standard mode for: " + packageName);
        boolean ok = true;
        ok &= ShizukuShell.executeSilent("cmd game mode standard " + packageName);
        ok &= ShizukuShell.executeSilent("dumpsys deviceidle whitelist -" + packageName);
        ok &= ShizukuShell.putSetting("global", "policy_control", "null*");
        return ok;
    }
}
