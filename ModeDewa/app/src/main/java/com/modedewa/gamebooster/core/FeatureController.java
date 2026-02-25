package com.modedewa.gamebooster.core;

import com.modedewa.gamebooster.util.ShizukuShell;
import android.util.Log;

/**
 * FeatureController — Disables unnecessary features during gaming.
 * Maps to modeGameON.sh Step 19.
 */
public class FeatureController {

    private static final String TAG = "FeatureController";

    /**
     * Disables non-essential system features to free resources during gaming.
     * Targets: NFC, auto-rotate, haptic feedback, display magnification,
     * Always-On Display (Doze), and edge lighting notifications.
     * All changes are applied via {@link ShizukuShell} settings commands.
     *
     * @return {@code true} if all settings were applied successfully
     */
    public static boolean disableUnnecessary() {
        Log.d(TAG, "Disabling unnecessary features...");
        boolean ok = true;

        // Disable NFC
        ok &= ShizukuShell.executeSilent("svc nfc disable 2>/dev/null");

        // Disable auto-rotate
        ok &= ShizukuShell.putSetting("system", "accelerometer_rotation", "0");

        // Disable haptic feedback
        ok &= ShizukuShell.putSetting("system", "haptic_feedback_enabled", "0");

        // Disable accessibility animations
        ok &= ShizukuShell.putSetting("secure", "accessibility_display_magnification_enabled", "0");

        // Disable Always-On Display
        ok &= ShizukuShell.putSetting("secure", "doze_enabled", "0");
        ok &= ShizukuShell.putSetting("secure", "doze_always_on", "0");

        // Disable edge lighting notifications
        ok &= ShizukuShell.putSetting("system", "edge_lighting_enabled", "0");

        return ok;
    }

    /**
     * Restores previously-disabled features to their default enabled state.
     * Re-enables auto-rotate, haptic feedback, and Always-On Display (Doze).
     * NFC and edge lighting are intentionally left unchanged as their original
     * state is not tracked.
     *
     * @return {@code true} if all settings were restored successfully
     */
    public static boolean restoreFeatures() {
        Log.d(TAG, "Restoring features...");
        boolean ok = true;
        ok &= ShizukuShell.putSetting("system", "accelerometer_rotation", "1");
        ok &= ShizukuShell.putSetting("system", "haptic_feedback_enabled", "1");
        ok &= ShizukuShell.putSetting("secure", "doze_enabled", "1");
        ok &= ShizukuShell.putSetting("secure", "doze_always_on", "1");
        return ok;
    }
}
