package com.modedewa.gamebooster.core;

import com.modedewa.gamebooster.util.ShizukuShell;
import android.util.Log;

/**
 * SyncController — Manages auto-sync and update settings.
 * Maps to modeGameON.sh Step 18.
 */
public class SyncController {

    private static final String TAG = "SyncController";

    /**
     * Disables auto-sync and background update services to reduce CPU/network
     * overhead during gaming. Targets: account auto-sync, package verifier,
     * app error reporting, and automatic time sync.
     *
     * @return {@code true} if all settings were applied successfully
     */
    public static boolean disableSync() {
        Log.d(TAG, "Disabling sync & updates...");
        boolean ok = true;
        ok &= ShizukuShell.putSetting("global", "auto_sync", "0");
        ok &= ShizukuShell.putSetting("global", "package_verifier_enable", "0");
        ok &= ShizukuShell.putSetting("global", "send_action_app_error", "0");
        ok &= ShizukuShell.putSetting("global", "auto_time", "0");
        return ok;
    }

    /**
     * Restores all sync and update services to their default enabled state.
     * Re-enables: account auto-sync, package verifier, app error reporting,
     * and automatic time sync.
     *
     * @return {@code true} if all settings were restored successfully
     */
    public static boolean restoreSync() {
        Log.d(TAG, "Restoring sync & updates...");
        boolean ok = true;
        ok &= ShizukuShell.putSetting("global", "auto_sync", "1");
        ok &= ShizukuShell.putSetting("global", "package_verifier_enable", "1");
        ok &= ShizukuShell.putSetting("global", "send_action_app_error", "1");
        ok &= ShizukuShell.putSetting("global", "auto_time", "1");
        return ok;
    }
}
