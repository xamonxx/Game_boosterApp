package com.modedewa.gamebooster.core;

import com.modedewa.gamebooster.util.ShizukuShell;
import android.util.Log;

/**
 * NotificationController — Blocks notifications and enables DND for gaming.
 * Maps to modeGameON.sh Step 10.
 */
public class NotificationController {

    private static final String TAG = "NotificationController";

    // Apps to block notifications from during gaming
    private static final String[] BLOCK_NOTIF_APPS = {
            "com.whatsapp", "com.instagram.android", "com.facebook.katana",
            "com.facebook.orca", "com.twitter.android", "com.google.android.gm",
            "com.google.android.youtube", "org.telegram.messenger",
            "com.shopee.id", "com.tokopedia.tkpd", "com.discord",
            "com.linkedin.android", "com.pinterest",
    };

    // ═══════════════════════════════════════
    // ACTIVATE
    // ═══════════════════════════════════════

    /** Enable Do Not Disturb mode */
    public static boolean enableDND() {
        Log.d(TAG, "Enabling DND mode...");
        return ShizukuShell.putSetting("global", "zen_mode", "2");
    }

    /** Block notifications from distracting apps */
    public static int blockNotifications() {
        Log.d(TAG, "Blocking notifications...");
        int blocked = 0;
        for (String app : BLOCK_NOTIF_APPS) {
            if (ShizukuShell.executeSilent(
                    "cmd appops set " + app + " POST_NOTIFICATION ignore")) {
                blocked++;
            }
        }
        return blocked;
    }

    // ═══════════════════════════════════════
    // DEACTIVATE
    // ═══════════════════════════════════════

    /** Disable DND mode */
    public static boolean disableDND() {
        Log.d(TAG, "Disabling DND mode...");
        return ShizukuShell.putSetting("global", "zen_mode", "0");
    }

    /** Restore notifications for all apps */
    public static int restoreNotifications() {
        Log.d(TAG, "Restoring notifications...");
        int restored = 0;
        for (String app : BLOCK_NOTIF_APPS) {
            if (ShizukuShell.executeSilent(
                    "cmd appops set " + app + " POST_NOTIFICATION allow")) {
                restored++;
            }
        }
        return restored;
    }
}
