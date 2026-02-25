package com.modedewa.gamebooster.core;

import com.modedewa.gamebooster.util.ShizukuShell;
import android.util.Log;

/**
 * SettingsTweaker — Manages system settings for performance mode.
 * Covers: animations, GPU rendering, background limits, developer settings.
 * Maps to modeGameON.sh Steps 1, 2, 3.
 */
public class SettingsTweaker {

    private static final String TAG = "SettingsTweaker";

    // ═══════════════════════════════════════
    // ACTIVATE (Game Mode ON)
    // ═══════════════════════════════════════

    /** Step 1: Disable all system animations */
    public static boolean disableAnimations() {
        Log.d(TAG, "Disabling animations...");
        boolean ok = true;
        ok &= ShizukuShell.putSetting("global", "window_animation_scale", "0.0");
        ok &= ShizukuShell.putSetting("global", "transition_animation_scale", "0.0");
        ok &= ShizukuShell.putSetting("global", "animator_duration_scale", "0.0");
        return ok;
    }

    /** Step 2: Enable performance developer settings */
    public static boolean enablePerformanceMode() {
        Log.d(TAG, "Enabling performance mode...");
        boolean ok = true;
        // Sustain performance mode
        ok &= ShizukuShell.putSetting("global", "always_on_display_constants", "\"\"");
        // Force GPU rendering
        ok &= ShizukuShell.putSetting("global", "force_gpu_rendering", "1");
        // Disable MSAA (reduce GPU load for low-end)
        ok &= ShizukuShell.putSetting("global", "gpu_force_4x_msaa", "0");
        // Background process limit = 0
        ok &= ShizukuShell.putSetting("global", "background_process_limit", "0");
        // Disable battery saver
        ok &= ShizukuShell.putSetting("global", "low_power", "0");
        ok &= ShizukuShell.putSetting("global", "low_power_sticky", "0");
        // Fixed performance mode
        ok &= ShizukuShell.putSetting("global", "sustain_performance_mode", "1");
        return ok;
    }

    /** Step 3: Set display refresh rate */
    public static boolean setRefreshRate(float rate) {
        Log.d(TAG, "Setting refresh rate to " + rate + "Hz...");
        boolean ok = true;
        ok &= ShizukuShell.putSetting("system", "peak_refresh_rate", String.valueOf(rate));
        ok &= ShizukuShell.putSetting("system", "min_refresh_rate", String.valueOf(rate));
        return ok;
    }

    // ═══════════════════════════════════════
    // DEACTIVATE (Game Mode OFF)
    // ═══════════════════════════════════════

    /** Restore all animations */
    public static boolean restoreAnimations() {
        Log.d(TAG, "Restoring animations...");
        boolean ok = true;
        ok &= ShizukuShell.putSetting("global", "window_animation_scale", "1.0");
        ok &= ShizukuShell.putSetting("global", "transition_animation_scale", "1.0");
        ok &= ShizukuShell.putSetting("global", "animator_duration_scale", "1.0");
        return ok;
    }

    /** Restore normal performance settings */
    public static boolean restorePerformanceMode() {
        Log.d(TAG, "Restoring normal performance...");
        boolean ok = true;
        ok &= ShizukuShell.putSetting("global", "force_gpu_rendering", "0");
        ok &= ShizukuShell.putSetting("global", "gpu_force_4x_msaa", "0");
        ok &= ShizukuShell.putSetting("global", "background_process_limit", "-1");
        ok &= ShizukuShell.putSetting("global", "sustain_performance_mode", "0");
        return ok;
    }

    /** Restore default refresh rate */
    public static boolean restoreRefreshRate() {
        Log.d(TAG, "Restoring default refresh rate...");
        boolean ok = true;
        ok &= ShizukuShell.putSetting("system", "peak_refresh_rate", "60.0");
        ok &= ShizukuShell.putSetting("system", "min_refresh_rate", "60.0");
        return ok;
    }
}
