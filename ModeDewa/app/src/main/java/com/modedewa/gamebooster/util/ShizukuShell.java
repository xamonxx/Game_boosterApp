package com.modedewa.gamebooster.util;

import android.content.pm.PackageManager;
import android.os.RemoteException;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import rikka.shizuku.Shizuku;

/**
 * ShizukuShell — Executes shell commands via Shizuku (ADB-level access).
 * This is the core engine that replaces root shell execution.
 */
public class ShizukuShell {

    private static final String TAG = "ShizukuShell";
    private static Method sNewProcessMethod;

    /**
     * Hasil eksekusi command shell via Shizuku.
     * Berisi flag sukses/gagal, output lines, dan error message.
     */
    public static class Result {
        public final boolean success;
        public final List<String> output;
        public final String error;

        /**
         * @param success true jika exit code == 0
         * @param output  daftar baris output dari stdout
         * @param error   pesan error dari stderr (bisa kosong)
         */
        public Result(boolean success, List<String> output, String error) {
            this.success = success;
            this.output = output;
            this.error = error;
        }

        /** Ambil baris pertama output, atau string kosong jika tidak ada. */
        public String getFirstLine() {
            if (output != null && !output.isEmpty()) {
                return output.get(0);
            }
            return "";
        }

        /** Gabungkan semua baris output menjadi satu string (dipisah newline). */
        public String getFullOutput() {
            if (output == null) return "";
            StringBuilder sb = new StringBuilder();
            for (String line : output) {
                sb.append(line).append("\n");
            }
            return sb.toString().trim();
        }
    }

    /**
     * Check if Shizuku service is available and running.
     */
    public static boolean isAvailable() {
        try {
            return Shizuku.pingBinder();
        } catch (Exception e) {
            Log.w(TAG, "Shizuku not available: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if we have permission to use Shizuku.
     */
    public static boolean hasPermission() {
        try {
            if (!isAvailable()) return false;
            return Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
        } catch (Exception e) {
            Log.w(TAG, "Permission check failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Request Shizuku permission from user.
     */
    public static void requestPermission(int requestCode) {
        try {
            if (isAvailable()) {
                Shizuku.requestPermission(requestCode);
            }
        } catch (Exception e) {
            Log.e(TAG, "Request permission failed: " + e.getMessage());
        }
    }

    /**
     * Execute a shell command via Shizuku.
     * This runs at ADB-level privileges (not root).
     */
    public static Result execute(String command) {
        if (!isAvailable()) {
            return new Result(false, null, "Shizuku not available");
        }
        if (!hasPermission()) {
            return new Result(false, null, "Shizuku permission not granted");
        }

        try {
            Process process = createShizukuProcess(command);

            List<String> outputLines = new ArrayList<>();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                outputLines.add(line);
            }
            reader.close();

            StringBuilder errorBuilder = new StringBuilder();
            BufferedReader errReader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()));
            while ((line = errReader.readLine()) != null) {
                errorBuilder.append(line).append("\n");
            }
            errReader.close();

            int exitCode = process.waitFor();
            boolean success = exitCode == 0;

            if (!success && errorBuilder.length() > 0) {
                Log.w(TAG, "Command failed: " + command + " -> " + errorBuilder);
            }

            return new Result(success, outputLines, errorBuilder.toString().trim());

        } catch (Exception e) {
            Log.e(TAG, "Execute failed: " + command, e);
            return new Result(false, null, e.getMessage());
        }
    }

    /**
     * Create a Process via Shizuku using reflection to access the private newProcess method.
     * Required for Shizuku API 13+ where newProcess() is private.
     */
    private static Process createShizukuProcess(String command) throws Exception {
        if (sNewProcessMethod == null) {
            sNewProcessMethod = Shizuku.class.getDeclaredMethod(
                    "newProcess", String[].class, String[].class, String.class);
            sNewProcessMethod.setAccessible(true);
        }
        return (Process) sNewProcessMethod.invoke(null,
                new String[]{"sh", "-c", command}, null, null);
    }

    /**
     * Execute a command and return only the first line of output.
     */
    public static String executeGetLine(String command) {
        Result result = execute(command);
        return result.getFirstLine();
    }

    /**
     * Execute a command silently (ignore output).
     */
    public static boolean executeSilent(String command) {
        Result result = execute(command);
        return result.success;
    }

    /**
     * Execute a settings put command.
     */
    public static boolean putSetting(String namespace, String key, String value) {
        return executeSilent("settings put " + namespace + " " + key + " " + value);
    }

    /**
     * Execute a settings get command.
     */
    public static String getSetting(String namespace, String key) {
        return executeGetLine("settings get " + namespace + " " + key);
    }

    /**
     * Force stop a package.
     */
    public static boolean forceStop(String packageName) {
        return executeSilent("am force-stop " + packageName);
    }

    /**
     * Disable a package for user 0.
     */
    public static boolean disablePackage(String packageName) {
        return executeSilent("pm disable-user --user 0 " + packageName);
    }

    /**
     * Enable a package for user 0.
     */
    public static boolean enablePackage(String packageName) {
        return executeSilent("pm enable " + packageName);
    }
}
