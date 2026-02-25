package com.modedewa.gamebooster.util;

import android.app.ActivityManager;
import android.content.Context;
import android.os.BatteryManager;
import android.os.Build;
import android.util.Log;

import com.modedewa.gamebooster.model.SystemInfo;

/**
 * DeviceDetector — Reads hardware info: chipset, RAM, battery, temperature.
 */
public class DeviceDetector {

    private static final String TAG = "DeviceDetector";

    private final Context context;

    /**
     * Constructor. Menyimpan context untuk akses system services (ActivityManager, BatteryManager).
     *
     * @param context Android context
     */
    public DeviceDetector(Context context) {
        this.context = context;
    }

    /**
     * Get complete system information.
     */
    public SystemInfo getSystemInfo() {
        SystemInfo info = new SystemInfo();
        info.deviceModel = Build.MODEL;
        info.manufacturer = Build.MANUFACTURER;
        info.androidVersion = Build.VERSION.RELEASE;
        info.sdkLevel = Build.VERSION.SDK_INT;
        info.chipset = getChipset();
        info.totalRam = getTotalRam();
        info.availableRam = getAvailableRam();
        info.batteryPercent = getBatteryPercent();
        info.batteryTemp = getBatteryTemperature();
        return info;
    }

    /**
     * Detect chipset name.
     */
    public String getChipset() {
        // Try reading from Build properties
        String hardware = Build.HARDWARE;
        String board = Build.BOARD;

        // Try Shizuku to get more detail
        if (ShizukuShell.isAvailable() && ShizukuShell.hasPermission()) {
            String prop = ShizukuShell.executeGetLine("getprop ro.board.platform");
            if (prop != null && !prop.isEmpty()) {
                hardware = prop;
            }
        }

        // Identify known chipsets
        if (hardware.toLowerCase().contains("sc9863") || board.toLowerCase().contains("sc9863")) {
            return "Unisoc SC9863A";
        } else if (hardware.toLowerCase().contains("mt6")) {
            return "MediaTek " + hardware.toUpperCase();
        } else if (hardware.toLowerCase().contains("sm") || hardware.toLowerCase().contains("sdm")) {
            return "Snapdragon " + hardware.toUpperCase();
        } else if (hardware.toLowerCase().contains("exynos")) {
            return "Exynos " + hardware.toUpperCase();
        }

        return hardware.toUpperCase();
    }

    /**
     * Check if this is the target Unisoc SC9863A device.
     */
    public boolean isUnisocSC9863A() {
        return getChipset().contains("SC9863");
    }

    /**
     * Get total RAM in MB.
     */
    public long getTotalRam() {
        try {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
            am.getMemoryInfo(memInfo);
            return memInfo.totalMem / (1024 * 1024);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Get available RAM in MB.
     */
    public long getAvailableRam() {
        try {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
            am.getMemoryInfo(memInfo);
            return memInfo.availMem / (1024 * 1024);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Get battery percentage.
     */
    public int getBatteryPercent() {
        try {
            BatteryManager bm = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
            return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Get battery temperature in Celsius.
     */
    public float getBatteryTemperature() {
        try {
            // Battery temp from Android system (in tenths of a degree)
            if (ShizukuShell.isAvailable() && ShizukuShell.hasPermission()) {
                String tempStr = ShizukuShell.executeGetLine(
                        "dumpsys battery | grep temperature | head -1 | grep -oE '[0-9]+'");
                if (tempStr != null && !tempStr.isEmpty()) {
                    int tempTenths = Integer.parseInt(tempStr.trim());
                    return tempTenths / 10.0f;
                }
            }
            return -1;
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Get CPU core count.
     */
    public int getCpuCoreCount() {
        return Runtime.getRuntime().availableProcessors();
    }

    /**
     * Get GPU name (if detectable).
     */
    public String getGpuName() {
        if (isUnisocSC9863A()) {
            return "IMG8322 PowerVR";
        }
        // Try reading from props
        if (ShizukuShell.isAvailable() && ShizukuShell.hasPermission()) {
            String gpu = ShizukuShell.executeGetLine("getprop ro.hardware.egl");
            if (gpu != null && !gpu.isEmpty()) {
                return gpu;
            }
        }
        return "Unknown GPU";
    }
}
