package com.modedewa.gamebooster.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.modedewa.gamebooster.model.AppDetectionConfig;
import com.modedewa.gamebooster.model.GameInfo;
import com.modedewa.gamebooster.model.GameSessionStats;
import com.modedewa.gamebooster.util.DeviceDetector;
import com.modedewa.gamebooster.util.GameDetector;
import com.modedewa.gamebooster.util.ShizukuShell;
import com.modedewa.gamebooster.util.SimProtector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GameDetectionService — Background service yang memonitor foreground app.
 *
 * Fitur:
 * - Deteksi otomatis saat game diluncurkan/ditutup
 * - Smart Profile Switching: auto-switch profil per game
 * - Battery-Aware: pause deteksi saat baterai rendah
 * - Auto-disable bloatware saat gaming
 * - Auto re-enable app saat game ditutup
 * - Session stats tracking (durasi, suhu, FPS, RAM)
 * - Broadcast status ke UI (AutoDetectActivity)
 */
public class GameDetectionService extends Service {

    private static final String TAG = "GameDetectionSvc";
    private static final String CHANNEL_ID = "mode_dewa_detection";
    private static final int NOTIFICATION_ID = 2001;

    // Broadcast actions
    public static final String ACTION_GAME_DETECTED = "com.modedewa.GAME_DETECTED";
    public static final String ACTION_GAME_CLOSED = "com.modedewa.GAME_CLOSED";
    public static final String ACTION_STATUS_UPDATE = "com.modedewa.DETECTION_STATUS";
    public static final String ACTION_LOG = "com.modedewa.DETECTION_LOG";
    public static final String EXTRA_PACKAGE = "extra_package";
    public static final String EXTRA_GAME_NAME = "extra_game_name";
    public static final String EXTRA_PROFILE = "extra_profile";
    public static final String EXTRA_LOG_MSG = "extra_log_message";
    public static final String EXTRA_LOG_LEVEL = "extra_log_level";

    // Intent actions untuk kontrol
    public static final String ACTION_START_DETECTION = "com.modedewa.START_DETECTION";
    public static final String ACTION_STOP_DETECTION = "com.modedewa.STOP_DETECTION";
    public static final String ACTION_FORCE_SCAN = "com.modedewa.FORCE_SCAN";
    public static final String ACTION_FORCE_BOOST = "com.modedewa.FORCE_BOOST";
    public static final String ACTION_COOL_DOWN = "com.modedewa.COOL_DOWN";

    private Handler handler;
    private HandlerThread scanThread;
    private Runnable scanRunnable;
    private AppDetectionConfig config;
    private GameDetector gameDetector;
    private DeviceDetector deviceDetector;

    // State tracking
    private boolean isScanning = false;
    private String currentForegroundGame = null;
    private GameSessionStats currentSession = null;
    private List<String> disabledPackages = new ArrayList<>();
    private Map<String, GameInfo> detectedGamesMap = new HashMap<>();

    /**
     * Interval idle (tidak ada game aktif) — lebih lambat untuk hemat baterai.
     * Digunakan saat scan tidak mendeteksi game di foreground.
     */
    private static final long IDLE_SCAN_INTERVAL_MULTIPLIER = 2;

    /**
     * Jumlah scan idle berturut-turut sebelum interval di-double-kan (max 2x).
     * Mencegah polling terlalu sering saat user tidak gaming.
     */
    private static final int IDLE_BACKOFF_THRESHOLD = 10;
    private int consecutiveIdleScans = 0;

    // Bloatware list (dari modeGameON.sh Step 22 & 25)
    private static final String[] BLOATWARE_PACKAGES = {
            "com.heytap.browser", "com.heytap.music",
            "com.heytap.market", "com.coloros.gamespace",
            "com.coloros.weather2", "com.coloros.floatassistant",
            "com.android.chrome", "com.google.android.youtube",
            "com.google.android.apps.maps", "com.google.android.apps.photos",
            "com.google.android.gm", "com.google.android.videos",
    };

    @Override
    public void onCreate() {
        super.onCreate();
        // Gunakan background thread untuk scan — jangan blokir main/UI thread
        scanThread = new HandlerThread("GameDetectionThread");
        scanThread.start();
        handler = new Handler(scanThread.getLooper());
        gameDetector = new GameDetector(this);
        deviceDetector = new DeviceDetector(this);
        config = AppDetectionConfig.load(this);
        createNotificationChannel();
        broadcastLog("INIT", "Game Detection Service dimulai (background thread)", "info");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case ACTION_START_DETECTION:
                    startDetection();
                    break;
                case ACTION_STOP_DETECTION:
                    stopDetection();
                    break;
                case ACTION_FORCE_SCAN:
                    performScan();
                    break;
                case ACTION_FORCE_BOOST:
                    forceBoost();
                    break;
                case ACTION_COOL_DOWN:
                    coolDown();
                    break;
                default:
                    startDetection();
                    break;
            }
        } else {
            startDetection();
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        stopDetection();
        // Hentikan background thread dengan aman
        if (scanThread != null) {
            scanThread.quitSafely();
            scanThread = null;
        }
        super.onDestroy();
    }

    // ============================================================
    // DETECTION ENGINE
    // ============================================================

    /**
     * Mulai scanning foreground app secara berkala pada background thread.
     * Menggunakan adaptive polling: interval di-double-kan setelah
     * {@link #IDLE_BACKOFF_THRESHOLD} scan idle berturut-turut.
     */
    private void startDetection() {
        if (isScanning) return;
        config = AppDetectionConfig.load(this);
        if (!config.isEnabled) {
            broadcastLog("SYS", "Auto-detect dinonaktifkan di pengaturan", "warn");
            return;
        }

        isScanning = true;
        consecutiveIdleScans = 0;
        startForeground(NOTIFICATION_ID, buildNotification("Monitoring aktif..."));
        broadcastLog("SYS", "Auto-Detect Engine dimulai — interval " +
                config.scanIntervalSec + "s (background thread)", "success");

        // Load detected games
        loadDetectedGames();

        // Start periodic scan pada background thread
        scanRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isScanning) return;
                performScan();

                // Adaptive interval: lebih lambat saat idle, cepat saat game aktif
                long intervalMs = config.scanIntervalSec * 1000L;
                if (currentForegroundGame == null) {
                    // Idle mode: backoff setelah beberapa scan tanpa game
                    consecutiveIdleScans++;
                    if (consecutiveIdleScans > IDLE_BACKOFF_THRESHOLD) {
                        intervalMs *= IDLE_SCAN_INTERVAL_MULTIPLIER;
                    }
                } else {
                    // Game aktif: reset idle counter, scan pada interval normal
                    consecutiveIdleScans = 0;
                }

                handler.postDelayed(this, intervalMs);
            }
        };
        handler.post(scanRunnable);
    }

    /**
     * Stop scanning dan bersihkan state.
     * Menghentikan FloatingMonitorService jika aktif.
     */
    private void stopDetection() {
        isScanning = false;
        if (scanRunnable != null) {
            handler.removeCallbacks(scanRunnable);
        }
        // End current session jika ada
        if (currentSession != null) {
            endGameSession();
        }
        // Re-enable disabled apps
        if (config != null && config.autoReEnableOnExit) {
            reEnableDisabledApps();
        }
        stopForeground(true);
        broadcastLog("SYS", "Auto-Detect Engine dihentikan", "warn");
    }

    /**
     * Perform satu scan cycle: cek foreground app.
     */
    private void performScan() {
        // Battery-aware check
        if (config.batteryAwareEnabled) {
            int battery = getBatteryPercent();
            if (battery >= 0 && battery < config.batteryThreshold) {
                broadcastLog("WARN", "Baterai " + battery + "% — di bawah threshold " +
                        config.batteryThreshold + "%, deteksi di-pause", "warn");
                return;
            }
        }

        String foregroundPkg = getForegroundPackage();
        if (foregroundPkg == null || foregroundPkg.isEmpty()) return;

        // Cek apakah foreground app adalah game yang terdaftar
        boolean isGame = isKnownGame(foregroundPkg);

        if (isGame && !foregroundPkg.equals(currentForegroundGame)) {
            // Game baru terdeteksi di foreground
            onGameDetected(foregroundPkg);
        } else if (!isGame && currentForegroundGame != null) {
            // Game ditutup, kembali ke non-game app
            onGameClosed();
        }

        // Record session data jika game sedang aktif
        if (currentSession != null) {
            recordSessionSample();
        }
    }

    /**
     * Dipanggil saat game terdeteksi di foreground.
     */
    private void onGameDetected(String packageName) {
        GameInfo gameInfo = detectedGamesMap.get(packageName);
        String gameName = gameInfo != null ? gameInfo.displayName : packageName;
        String profile = "balanced";

        // Smart profile switching
        if (config.smartProfileEnabled && gameInfo != null) {
            profile = gameInfo.selectedProfile;
        }

        currentForegroundGame = packageName;
        broadcastLog("SCAN", "Foreground berubah: " + packageName, "info");
        broadcastLog("DETECT", gameName + " diluncurkan → Mode " +
                profile.toUpperCase() + " diaktifkan", "success");

        // Auto-disable bloatware
        if (config.autoDisableBloatware) {
            disableBloatware();
        }

        // Activate game mode via ShizukuShell
        activateGameMode(packageName, profile);

        // Start notification controller
        if (config.notificationControllerEnabled) {
            startNotificationController(packageName, gameName, profile);
        }

        // Start floating monitor overlay (HUD saat gaming)
        startFloatingMonitor(packageName, gameName, profile);

        // Start session tracking
        int battery = getBatteryPercent();
        currentSession = GameSessionStats.startSession(packageName, gameName, profile, battery);

        // Broadcast ke UI
        Intent broadcast = new Intent(ACTION_GAME_DETECTED);
        broadcast.putExtra(EXTRA_PACKAGE, packageName);
        broadcast.putExtra(EXTRA_GAME_NAME, gameName);
        broadcast.putExtra(EXTRA_PROFILE, profile);
        sendBroadcast(broadcast);

        // Update notification
        updateNotification("Game aktif: " + gameName + " [" + profile.toUpperCase() + "]");
    }

    /**
     * Dipanggil saat game ditutup (foreground bukan game lagi).
     */
    private void onGameClosed() {
        String closedGame = currentForegroundGame;
        currentForegroundGame = null;

        broadcastLog("DETECT", "Game ditutup — mode normal dikembalikan", "warn");

        // End session
        endGameSession();

        // Deactivate game mode
        deactivateGameMode();

        // Re-enable apps
        if (config.autoReEnableOnExit) {
            reEnableDisabledApps();
        }

        // Stop notification controller + floating monitor
        stopNotificationController();
        stopFloatingMonitor();

        // SIM health check
        SimProtector.HealthReport simReport = SimProtector.checkHealth();
        broadcastLog("SIM", simReport.message, simReport.phoneProcessRunning ? "info" : "error");

        // Broadcast ke UI
        Intent broadcast = new Intent(ACTION_GAME_CLOSED);
        broadcast.putExtra(EXTRA_PACKAGE, closedGame);
        sendBroadcast(broadcast);

        updateNotification("Monitoring aktif...");
    }

    // ============================================================
    // GAME MODE ACTIVATION
    // ============================================================

    private void activateGameMode(String packageName, String profile) {
        if (!ShizukuShell.isAvailable() || !ShizukuShell.hasPermission()) {
            broadcastLog("ERR", "Shizuku tidak tersedia untuk aktivasi mode game", "error");
            return;
        }

        // Set Android Game Mode API
        ShizukuShell.executeSilent("cmd game mode performance " + packageName);

        // Disable FPS throttling
        ShizukuShell.executeSilent(
                "cmd device_config put game_overlay " + packageName +
                        " mode=2,fps=0:mode=3,fps=0");

        // Set priority tinggi untuk game
        ShizukuShell.executeSilent(
                "cmd activity set-process-limit " + packageName + " 0");

        // Profile-specific optimizations
        switch (profile) {
            case "ultra":
                applyUltraProfile(packageName);
                break;
            case "balanced":
                applyBalancedProfile(packageName);
                break;
            case "light":
                applyLightProfile(packageName);
                break;
        }

        broadcastLog("MODE", "Game Mode API diaktifkan untuk " + packageName, "success");
    }

    private void applyUltraProfile(String packageName) {
        // Matikan animasi
        ShizukuShell.putSetting("global", "window_animation_scale", "0");
        ShizukuShell.putSetting("global", "transition_animation_scale", "0");
        ShizukuShell.putSetting("global", "animator_duration_scale", "0");
        // Force GPU rendering
        ShizukuShell.putSetting("global", "force_hw_ui", "1");
        // Background process limit = 0
        ShizukuShell.putSetting("global", "background_process_limit", "0");
        // DND mode
        ShizukuShell.putSetting("global", "zen_mode", "1");

        broadcastLog("MODE", "Profil ULTRA diterapkan — animasi OFF, GPU forced, BG limit 0", "success");
    }

    private void applyBalancedProfile(String packageName) {
        // Kurangi animasi tapi tidak matikan
        ShizukuShell.putSetting("global", "window_animation_scale", "0.5");
        ShizukuShell.putSetting("global", "transition_animation_scale", "0.5");
        ShizukuShell.putSetting("global", "animator_duration_scale", "0.5");
        // Background limit moderate
        ShizukuShell.putSetting("global", "background_process_limit", "2");

        broadcastLog("MODE", "Profil BALANCED diterapkan — animasi 0.5x, BG limit 2", "info");
    }

    private void applyLightProfile(String packageName) {
        // Hanya set game mode, minimal tweaks
        ShizukuShell.putSetting("global", "window_animation_scale", "0.5");

        broadcastLog("MODE", "Profil LIGHT diterapkan — tweak minimal", "info");
    }

    private void deactivateGameMode() {
        if (!ShizukuShell.isAvailable()) return;

        // Restore animasi
        ShizukuShell.putSetting("global", "window_animation_scale", "1");
        ShizukuShell.putSetting("global", "transition_animation_scale", "1");
        ShizukuShell.putSetting("global", "animator_duration_scale", "1");
        // Restore background limit
        ShizukuShell.putSetting("global", "background_process_limit", "-1");
        // Restore force GPU
        ShizukuShell.putSetting("global", "force_hw_ui", "0");
        // DND off
        ShizukuShell.putSetting("global", "zen_mode", "0");

        broadcastLog("MODE", "Mode game dinonaktifkan — pengaturan dikembalikan", "info");
    }

    // ============================================================
    // BLOATWARE MANAGEMENT
    // ============================================================

    private void disableBloatware() {
        int count = 0;
        for (String pkg : BLOATWARE_PACKAGES) {
            if (SimProtector.isProtected(pkg)) continue;
            if (ShizukuShell.disablePackage(pkg)) {
                disabledPackages.add(pkg);
                count++;
            }
        }
        if (count > 0) {
            broadcastLog("AUTO", count + " app bawaan di-disable untuk RAM", "warn");
        }
    }

    private void reEnableDisabledApps() {
        int count = 0;
        for (String pkg : disabledPackages) {
            if (ShizukuShell.enablePackage(pkg)) {
                count++;
            }
        }
        disabledPackages.clear();
        if (count > 0) {
            broadcastLog("AUTO", count + " app bawaan di-enable kembali", "success");
        }
    }

    // ============================================================
    // SESSION TRACKING
    // ============================================================

    private void recordSessionSample() {
        if (currentSession == null) return;
        float temp = deviceDetector.getBatteryTemperature();
        long ramUsed = deviceDetector.getTotalRam() - deviceDetector.getAvailableRam();
        // FPS: Android tidak punya API native, gunakan estimasi dari frame time
        int estimatedFps = getEstimatedFps();
        currentSession.recordSample(temp, estimatedFps, ramUsed);
    }

    private void endGameSession() {
        if (currentSession == null) return;
        int battery = getBatteryPercent();
        currentSession.endSession(battery);
        currentSession.appsDisabledCount = disabledPackages.size();
        currentSession.saveToHistory(this);
        broadcastLog("STAT", "Sesi selesai: " + currentSession.getFormattedDuration() +
                " | Suhu maks: " + String.format("%.0f°C", currentSession.maxTemp) +
                " | Baterai: -" + currentSession.getBatteryUsed() + "%", "info");
        currentSession = null;
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    private String getForegroundPackage() {
        try {
            UsageStatsManager usm = (UsageStatsManager)
                    getSystemService(Context.USAGE_STATS_SERVICE);
            long now = System.currentTimeMillis();
            UsageEvents events = usm.queryEvents(now - 5000, now);
            String lastPackage = null;

            while (events.hasNextEvent()) {
                UsageEvents.Event event = new UsageEvents.Event();
                events.getNextEvent(event);
                if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    lastPackage = event.getPackageName();
                }
            }
            return lastPackage;
        } catch (Exception e) {
            // Fallback: gunakan Shizuku
            if (ShizukuShell.isAvailable() && ShizukuShell.hasPermission()) {
                String result = ShizukuShell.executeGetLine(
                        "dumpsys activity activities 2>/dev/null | grep -i 'mResumedActivity' | head -1");
                if (result != null && result.contains("/")) {
                    String[] parts = result.split("\\{")[1].split("/")[0].trim().split(" ");
                    return parts[parts.length - 1];
                }
            }
            return null;
        }
    }

    private boolean isKnownGame(String packageName) {
        if (detectedGamesMap.containsKey(packageName)) {
            GameInfo info = detectedGamesMap.get(packageName);
            return info != null && info.isAutoDetectEnabled;
        }
        return false;
    }

    private void loadDetectedGames() {
        // Load dari GameDetector (database bawaan)
        List<GameInfo> installedGames = gameDetector.detectInstalledGames();
        for (GameInfo game : installedGames) {
            detectedGamesMap.put(game.packageName, game);
        }
        // Load custom packages dari config
        String[] customPkgs = config.getCustomPackageArray();
        for (String pkg : customPkgs) {
            if (!pkg.trim().isEmpty() && !detectedGamesMap.containsKey(pkg.trim())) {
                GameInfo custom = GameInfo.createCustom(pkg.trim(), pkg.trim());
                detectedGamesMap.put(pkg.trim(), custom);
            }
        }
        broadcastLog("INIT", "Database loaded: " + detectedGamesMap.size() +
                " game terdaftar", "info");
    }

    private int getBatteryPercent() {
        try {
            BatteryManager bm = (BatteryManager) getSystemService(Context.BATTERY_SERVICE);
            return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        } catch (Exception e) {
            return -1;
        }
    }

    private int getEstimatedFps() {
        // Placeholder: Android tidak punya public FPS API.
        // Bisa diperluas dengan SurfaceFlinger stats via Shizuku.
        if (ShizukuShell.isAvailable() && ShizukuShell.hasPermission()) {
            String result = ShizukuShell.executeGetLine(
                    "dumpsys SurfaceFlinger --latency 2>/dev/null | head -1");
            if (result != null && !result.isEmpty()) {
                try {
                    long refreshPeriod = Long.parseLong(result.trim());
                    if (refreshPeriod > 0) {
                        return (int) (1000000000L / refreshPeriod);
                    }
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
        }
        return 0; // Unknown
    }

    // ============================================================
    // NOTIFICATION CONTROLLER INTEGRATION
    // ============================================================

    private void startNotificationController(String packageName, String gameName, String profile) {
        Intent intent = new Intent(this, NotificationControllerService.class);
        intent.setAction(NotificationControllerService.ACTION_START);
        intent.putExtra(EXTRA_PACKAGE, packageName);
        intent.putExtra(EXTRA_GAME_NAME, gameName);
        intent.putExtra(EXTRA_PROFILE, profile);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    private void stopNotificationController() {
        Intent intent = new Intent(this, NotificationControllerService.class);
        intent.setAction(NotificationControllerService.ACTION_STOP);
        startService(intent);
    }

    // ============================================================
    // FLOATING MONITOR INTEGRATION
    // ============================================================

    /**
     * Mulai FloatingMonitorService saat game terdeteksi.
     * Menampilkan overlay HUD dengan FPS, suhu, RAM, baterai, dan timer.
     */
    private void startFloatingMonitor(String packageName, String gameName, String profile) {
        Intent intent = new Intent(this, FloatingMonitorService.class);
        intent.putExtra(FloatingMonitorService.EXTRA_GAME_PACKAGE, packageName);
        intent.putExtra(FloatingMonitorService.EXTRA_GAME_NAME, gameName);
        intent.putExtra(FloatingMonitorService.EXTRA_PROFILE, profile);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        broadcastLog("HUD", "Floating Monitor dimulai untuk " + gameName, "info");
    }

    /**
     * Hentikan FloatingMonitorService saat game ditutup.
     */
    private void stopFloatingMonitor() {
        Intent intent = new Intent(this, FloatingMonitorService.class);
        intent.setAction(FloatingMonitorService.ACTION_STOP);
        startService(intent);
    }

    // ============================================================
    // FORCE ACTIONS (dari UI Quick Actions)
    // ============================================================

    private void forceBoost() {
        broadcastLog("BOOST", "Force Boost diaktifkan!", "success");
        if (ShizukuShell.isAvailable() && ShizukuShell.hasPermission()) {
            // Kill semua background app
            ShizukuShell.executeSilent("am kill-all 2>/dev/null");
            // Clean cache
            ShizukuShell.executeSilent("sync && echo 3 > /proc/sys/vm/drop_caches 2>/dev/null");
            // TRIM fstrim
            ShizukuShell.executeSilent("fstrim /data 2>/dev/null");
            broadcastLog("BOOST", "Background apps killed + cache cleared + storage trimmed", "success");
        }
    }

    private void coolDown() {
        broadcastLog("COOL", "Cool Down mode diaktifkan", "info");
        if (ShizukuShell.isAvailable() && ShizukuShell.hasPermission()) {
            // Restore thermal throttling
            ShizukuShell.putSetting("global", "window_animation_scale", "1");
            // Reduce GPU load
            ShizukuShell.putSetting("global", "force_hw_ui", "0");
            broadcastLog("COOL", "GPU forcing disabled, animasi restored — device akan mendingin", "info");
        }
    }

    // ============================================================
    // NOTIFICATIONS
    // ============================================================

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Game Detection Service",
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Monitoring foreground app untuk auto-detect game");
            channel.setShowBadge(false);
            NotificationManager nm = getSystemService(NotificationManager.class);
            nm.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification(String text) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("MODE DEWA — Auto-Detect")
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_menu_search)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();
    }

    private void updateNotification(String text) {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(NOTIFICATION_ID, buildNotification(text));
    }

    // ============================================================
    // BROADCAST LOG (ke UI)
    // ============================================================

    private void broadcastLog(String tag, String message, String level) {
        Log.d(TAG, "[" + tag + "] " + message);
        Intent intent = new Intent(ACTION_LOG);
        intent.putExtra(EXTRA_LOG_MSG, "[" + tag + "] " + message);
        intent.putExtra(EXTRA_LOG_LEVEL, level);
        sendBroadcast(intent);
    }
}
