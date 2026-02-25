package com.modedewa.gamebooster.core;

import com.modedewa.gamebooster.util.ShizukuShell;
import com.modedewa.gamebooster.util.SimProtector;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

/**
 * AppKiller — Aggressively kills background apps and disables non-essential packages.
 * Maps to modeGameON.sh Steps 4, 5, 25.
 */
public class AppKiller {

    private static final String TAG = "AppKiller";

    // Heavy apps to kill (from modeGameON.sh Step 4)
    private static final String[] HEAVY_APPS = {
            "com.whatsapp", "com.instagram.android", "com.facebook.katana",
            "com.facebook.orca", "com.facebook.lite", "com.twitter.android",
            "com.zhiliaoapp.musically", "com.ss.android.ugc.trill",
            "com.google.android.youtube", "com.spotify.music",
            "com.netflix.mediaclient", "com.shopee.id", "com.tokopedia.tkpd",
            "com.lazada.android", "id.dana", "com.gojek.app",
            "com.grabtaxi.passenger", "com.bukalapak.android",
            "com.imo.android.imoim", "com.pinterest", "com.linkedin.android",
            "com.discord", "org.telegram.messenger", "com.viber.voip",
            "com.snapchat.android", "com.google.android.apps.tachyon",
            "com.google.android.apps.photos", "com.google.android.apps.maps",
            "com.google.android.apps.docs", "com.google.android.gm",
    };

    // Apps to disable during gaming (from modeGameON.sh Step 25)
    private static final String[] APPS_TO_DISABLE = {
            "com.google.android.youtube",
            "com.google.android.apps.photos",
            "com.google.android.apps.maps",
            "com.google.android.apps.docs",
            "com.google.android.apps.tachyon",
            "com.google.android.videos",
            "com.google.android.music",
            "com.google.android.apps.magazines",
            "com.google.android.apps.books",
            "com.google.android.calendar",
            "com.google.android.keep",
            "com.android.chrome",
            "com.google.android.googlequicksearchbox",
            "com.google.android.marvin.talkback",
            "com.android.printspooler",
            "com.android.bips",
            "com.android.dreams.basic",
            "com.android.wallpaper.livepicker",
    };

    // ═══════════════════════════════════════
    // ACTIVATE (Game Mode ON)
    // ═══════════════════════════════════════

    /** Kill all heavy background apps */
    public static int killHeavyApps() {
        Log.d(TAG, "Killing heavy background apps...");
        int killed = 0;
        for (String app : HEAVY_APPS) {
            if (SimProtector.isProtected(app)) continue;
            if (ShizukuShell.forceStop(app)) {
                killed++;
            }
        }
        Log.d(TAG, "Killed " + killed + " heavy apps");
        return killed;
    }

    /** Kill all non-essential running user processes */
    public static int killAllBackground(String protectPackage) {
        Log.d(TAG, "Scanning and killing all background processes...");
        int killed = 0;

        ShizukuShell.Result result = ShizukuShell.execute(
                "pm list packages -3 2>/dev/null | cut -d: -f2");

        if (result.success && result.output != null) {
            for (String pkg : result.output) {
                pkg = pkg.trim();
                if (pkg.isEmpty()) continue;
                if (SimProtector.isProtected(pkg)) continue;
                if (pkg.equals(protectPackage)) continue; // Don't kill the target game
                if (pkg.equals("com.modedewa.gamebooster")) continue; // Don't kill ourselves
                if (pkg.contains("shizuku")) continue; // Don't kill Shizuku

                ShizukuShell.executeSilent("am force-stop " + pkg);
                killed++;
            }
        }

        Log.d(TAG, "Killed " + killed + " background processes");
        return killed;
    }

    /** Trim caches system-wide */
    public static boolean trimCaches() {
        Log.d(TAG, "Trimming caches...");
        return ShizukuShell.executeSilent("pm trim-caches 999999999999");
    }

    /** Send trim memory to all running apps */
    public static void trimMemory() {
        Log.d(TAG, "Trimming memory...");
        ShizukuShell.executeSilent(
                "am send-trim-memory --user 0 com.android.systemui RUNNING_CRITICAL 2>/dev/null");
    }

    /** Disable non-essential apps to free resources */
    public static int disableNonEssentialApps() {
        Log.d(TAG, "Disabling non-essential apps...");
        int disabled = 0;
        for (String app : APPS_TO_DISABLE) {
            if (SimProtector.isProtected(app)) continue;
            if (ShizukuShell.disablePackage(app)) {
                disabled++;
            }
        }
        Log.d(TAG, "Disabled " + disabled + " apps");
        return disabled;
    }

    // ═══════════════════════════════════════
    // DEACTIVATE (Game Mode OFF)
    // ═══════════════════════════════════════

    /** Re-enable all previously disabled apps */
    public static int reEnableApps() {
        Log.d(TAG, "Re-enabling disabled apps...");
        int enabled = 0;
        for (String app : APPS_TO_DISABLE) {
            if (ShizukuShell.enablePackage(app)) {
                enabled++;
            }
        }
        // Restore background process limit
        ShizukuShell.putSetting("global", "background_process_limit", "-1");
        Log.d(TAG, "Re-enabled " + enabled + " apps");
        return enabled;
    }
}
