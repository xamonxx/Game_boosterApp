package com.modedewa.gamebooster.util;

import android.util.Log;

/**
 * SimProtector — Safety checks for SIM card and telephony services.
 * Ensures game mode optimizations don't break cellular connectivity.
 * Translated from modeGameON.sh Step 26.
 */
public class SimProtector {

    private static final String TAG = "SimProtector";

    public enum SimStatus {
        READY,        // SIM terdeteksi & siap
        NOT_READY,    // SIM belum siap
        ABSENT,       // Tidak ada SIM
        ERROR,        // Ada masalah
        UNKNOWN       // Tidak bisa baca
    }

    public static class HealthReport {
        public SimStatus simStatus = SimStatus.UNKNOWN;
        public boolean phoneProcessRunning = false;
        public boolean signalAvailable = false;
        public String simStateRaw = "";
        public String signalStrength = "";
        public String message = "";
    }

    /**
     * Run full SIM card & signal health check.
     * This mirrors Step 26 from modeGameON.sh.
     */
    public static HealthReport checkHealth() {
        HealthReport report = new HealthReport();

        // 1. Check com.android.phone process
        report.phoneProcessRunning = checkPhoneProcess();
        if (!report.phoneProcessRunning) {
            report.message = "PERINGATAN: Proses telephony tidak berjalan!";
            Log.w(TAG, report.message);
            // Try to recover
            tryRecoverPhoneProcess();
            report.phoneProcessRunning = checkPhoneProcess();
        }

        // 2. Check SIM card state
        checkSimState(report);

        // 3. Check signal strength
        checkSignal(report);

        // Build final message
        if (report.simStatus == SimStatus.READY && report.phoneProcessRunning) {
            report.message = "SIM Card: OK | Sinyal: " +
                    (report.signalAvailable ? "Tersedia" : "Lemah");
        }

        return report;
    }

    /**
     * Check if com.android.phone process is running.
     */
    private static boolean checkPhoneProcess() {
        if (!ShizukuShell.isAvailable()) return true; // Assume OK without Shizuku

        String result = ShizukuShell.executeGetLine(
                "pidof com.android.phone 2>/dev/null");
        return result != null && !result.trim().isEmpty();
    }

    /**
     * Try to restart the phone process if it died.
     */
    private static void tryRecoverPhoneProcess() {
        Log.w(TAG, "Attempting to recover com.android.phone...");
        ShizukuShell.executeSilent(
                "am startservice -n com.android.phone/.PhoneInterfaceManager 2>/dev/null");
    }

    /**
     * Check SIM card state via telephony registry.
     */
    private static void checkSimState(HealthReport report) {
        if (!ShizukuShell.isAvailable()) {
            report.simStatus = SimStatus.UNKNOWN;
            return;
        }

        String simState = ShizukuShell.executeGetLine(
                "dumpsys telephony.registry 2>/dev/null | grep -i 'mSimState' | head -1");

        if (simState != null && !simState.isEmpty()) {
            report.simStateRaw = simState.trim();

            if (simState.toUpperCase().contains("READY") ||
                    simState.toUpperCase().contains("LOADED")) {
                report.simStatus = SimStatus.READY;
            } else if (simState.toUpperCase().contains("ABSENT")) {
                report.simStatus = SimStatus.ABSENT;
            } else if (simState.toUpperCase().contains("NOT_READY")) {
                report.simStatus = SimStatus.NOT_READY;
            } else if (simState.toUpperCase().contains("ERROR")) {
                report.simStatus = SimStatus.ERROR;
            } else {
                report.simStatus = SimStatus.UNKNOWN;
            }
        } else {
            report.simStatus = SimStatus.UNKNOWN;
        }
    }

    /**
     * Check signal strength.
     */
    private static void checkSignal(HealthReport report) {
        if (!ShizukuShell.isAvailable()) {
            report.signalAvailable = true; // Assume OK
            return;
        }

        String signal = ShizukuShell.executeGetLine(
                "dumpsys telephony.registry 2>/dev/null | grep -i 'mSignalStrength' | head -1");

        if (signal != null && !signal.isEmpty()) {
            report.signalStrength = signal.trim();
            report.signalAvailable = true;
        } else {
            report.signalAvailable = false;
        }
    }

    /**
     * List of protected packages that must NEVER be disabled.
     * From modeGameON.sh APLIKASI_DILINDUNGI array.
     */
    public static final String[] PROTECTED_PACKAGES = {
            "com.android.phone",
            "com.android.server.telecom",
            "com.android.providers.telephony",
            "com.android.ims",
            "com.android.stk",
            "com.android.systemui",
            "com.android.settings",
            "com.android.launcher",
            "com.android.inputmethod.latin",
            "com.google.android.inputmethod.latin",
            "android",
            "com.android.providers.contacts",
            "com.android.providers.media",
            "com.android.shell",
            "com.android.se",
            "com.android.networkstack",
            "moe.shizuku.privileged.api", // Shizuku itself!
    };

    /**
     * Check if a package is protected (should never be disabled).
     */
    public static boolean isProtected(String packageName) {
        for (String p : PROTECTED_PACKAGES) {
            if (packageName.contains(p)) {
                return true;
            }
        }
        return false;
    }
}
