package com.modedewa.gamebooster.model;

/**
 * SystemInfo — Data class holding device hardware information.
 * Enhanced: tambah GPU, CPU cores, freq, refresh rate, dan helper methods.
 */
public class SystemInfo {
    public String deviceModel = "";
    public String manufacturer = "";
    public String androidVersion = "";
    public int sdkLevel = 0;
    public String chipset = "";
    public String gpuName = "";
    public long totalRam = 0;      // in MB
    public long availableRam = 0;  // in MB
    public int batteryPercent = 0;
    public float batteryTemp = 0;  // in Celsius
    public int cpuCores = 0;
    public long cpuFreqMhz = 0;
    public int refreshRate = 60;

    /** Format display RAM: "available/total MB". */
    public String getRamDisplay() {
        return availableRam + "/" + totalRam + " MB";
    }

    /** Format display suhu baterai: "XX.X°C", atau "N/A" jika tidak tersedia. */
    public String getTempDisplay() {
        if (batteryTemp <= 0) return "N/A";
        return String.format("%.1f°C", batteryTemp);
    }

    /** Persentase RAM yang terpakai (0-100). */
    public int getRamUsagePercent() {
        if (totalRam == 0) return 0;
        return (int) (((totalRam - availableRam) * 100) / totalRam);
    }

    /** RAM terpakai dalam MB. */
    public long getUsedRam() {
        return totalRam - availableRam;
    }

    /** Status suhu: "cool", "warm", "hot", "critical" */
    public String getTempStatus() {
        if (batteryTemp < 0) return "unknown";
        if (batteryTemp < 40) return "cool";
        if (batteryTemp < 55) return "warm";
        if (batteryTemp < 70) return "hot";
        return "critical";
    }

    /** String ringkasan device untuk display. */
    public String getDeviceSummary() {
        return deviceModel + " | " + chipset + " | Android " + androidVersion;
    }

    @Override
    public String toString() {
        return "SystemInfo{" +
                "device=" + deviceModel +
                ", chipset=" + chipset +
                ", ram=" + availableRam + "/" + totalRam + "MB" +
                ", battery=" + batteryPercent + "%" +
                ", temp=" + batteryTemp + "°C" +
                '}';
    }
}
