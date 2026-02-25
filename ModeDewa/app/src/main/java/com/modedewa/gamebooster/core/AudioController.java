package com.modedewa.gamebooster.core;

import com.modedewa.gamebooster.util.ShizukuShell;
import android.util.Log;

/**
 * AudioController — Manages audio settings for gaming.
 * Maps to modeGameON.sh Step 14.
 */
public class AudioController {

    private static final String TAG = "AudioController";

    /**
     * Mutes non-essential system sounds to reduce audio interruptions and
     * free audio resources during gaming. Disables: UI sound effects,
     * DTMF dialer tones, lock-screen sounds, and charging sounds.
     *
     * @return {@code true} if all audio settings were applied successfully
     */
    public static boolean optimizeForGaming() {
        Log.d(TAG, "Optimizing audio for gaming...");
        boolean ok = true;
        ok &= ShizukuShell.putSetting("system", "sound_effects_enabled", "0");
        ok &= ShizukuShell.putSetting("system", "dtmf_tone", "0");
        ok &= ShizukuShell.putSetting("system", "lockscreen_sounds_enabled", "0");
        ok &= ShizukuShell.putSetting("global", "charging_sounds_enabled", "0");
        return ok;
    }

    /**
     * Restores all system sounds to their default enabled state.
     * Re-enables: UI sound effects, DTMF tones, lock-screen sounds,
     * and charging sounds.
     *
     * @return {@code true} if all audio settings were restored successfully
     */
    public static boolean restoreDefaults() {
        Log.d(TAG, "Restoring audio defaults...");
        boolean ok = true;
        ok &= ShizukuShell.putSetting("system", "sound_effects_enabled", "1");
        ok &= ShizukuShell.putSetting("system", "dtmf_tone", "1");
        ok &= ShizukuShell.putSetting("system", "lockscreen_sounds_enabled", "1");
        ok &= ShizukuShell.putSetting("global", "charging_sounds_enabled", "1");
        return ok;
    }
}
