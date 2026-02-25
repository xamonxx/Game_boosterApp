package com.modedewa.gamebooster.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.modedewa.gamebooster.R;
import com.modedewa.gamebooster.model.GameSessionStats;
import com.modedewa.gamebooster.model.SystemInfo;
import com.modedewa.gamebooster.ui.HomeActivity;
import com.modedewa.gamebooster.ui.ThermalWarningDialog;
import com.modedewa.gamebooster.util.DeviceDetector;

/**
 * FloatingMonitorService — Foreground service yang menampilkan overlay HUD
 * selama game mode aktif. Menampilkan FPS, suhu, RAM, baterai, dan timer sesi.
 *
 * <p>Fitur:
 * <ul>
 *   <li>Draggable overlay window (bisa digeser ke mana saja)</li>
 *   <li>Expand/collapse toggle (mini circle FPS only vs full panel)</li>
 *   <li>Real-time stats update setiap {@link #UPDATE_INTERVAL_MS} ms</li>
 *   <li>Warna indikator otomatis berubah berdasarkan status suhu</li>
 *   <li>Ikon peringatan suhu muncul saat suhu >= {@link #TEMP_WARNING_THRESHOLD}</li>
 *   <li>Session timer berjalan selama overlay aktif</li>
 *   <li>Notification fallback untuk Android < 8.0</li>
 * </ul>
 *
 * <p>Digunakan oleh {@link GameDetectionService} saat game terdeteksi aktif,
 * atau dipicu manual dari {@link com.modedewa.gamebooster.ui.AutoDetectActivity}.
 *
 * <p>Target hardware: Unisoc SC9863A, 2-3GB RAM, Android 11.
 * Update interval dijaga agar hemat resource (5 detik default).
 */
public class FloatingMonitorService extends Service {

    private static final String TAG = "FloatingMonitor";

    /** Notification channel ID untuk foreground service. */
    private static final String CHANNEL_ID = "mode_dewa_monitor";

    /** Notification ID unik untuk service ini. */
    private static final int NOTIFICATION_ID = 1001;

    /** Interval update stats overlay dalam milidetik (5 detik). */
    private static final long UPDATE_INTERVAL_MS = 5000;

    /** Batas suhu untuk menampilkan ikon peringatan (Celsius). */
    private static final float TEMP_WARNING_THRESHOLD = 45.0f;

    /** Batas suhu untuk warna merah/kritis (Celsius). */
    private static final float TEMP_HOT_THRESHOLD = 55.0f;

    /** Batas suhu kritis untuk tampilkan dialog peringatan penuh (Celsius). */
    private static final float TEMP_CRITICAL_DIALOG_THRESHOLD = 60.0f;

    // --- Intent extras ---

    /** Extra key: package name game yang sedang dimainkan. */
    public static final String EXTRA_GAME_PACKAGE = "game_package";

    /** Extra key: nama tampilan game yang sedang dimainkan. */
    public static final String EXTRA_GAME_NAME = "game_name";

    /** Extra key: profil optimisasi yang aktif. */
    public static final String EXTRA_PROFILE = "profile";

    /** Intent action untuk menghentikan service. */
    public static final String ACTION_STOP = "STOP";

    /** Intent action untuk toggle expand/collapse dari notifikasi. */
    public static final String ACTION_TOGGLE = "TOGGLE";

    // --- Window & Views ---
    private WindowManager windowManager;
    private View overlayView;
    private WindowManager.LayoutParams layoutParams;

    // Expanded panel views
    private LinearLayout expandedView;
    private TextView statFps;
    private TextView statTemp;
    private TextView statRam;
    private TextView statBattery;
    private TextView statSessionTime;
    private ImageView tempWarningIcon;
    private View statusDot;
    private View timerDot;

    // Collapsed mini views
    private FrameLayout collapsedView;
    private TextView collapsedFps;

    // --- State ---
    private Handler handler;
    private DeviceDetector deviceDetector;
    private boolean isRunning = false;
    private boolean isExpanded = true;

    /** Waktu mulai sesi untuk timer. */
    private long sessionStartTime;

    /** Game session stats tracker (opsional, bisa null jika tidak dari auto-detect). */
    private GameSessionStats currentSession;

    /** Package name game yang sedang dimainkan. */
    private String gamePackage = "";

    /** Nama tampilan game. */
    private String gameName = "";

    /** Profil optimisasi aktif. */
    private String activeProfile = "balanced";

    // --- Drag handling ---
    private float dragStartX, dragStartY;
    private int dragInitialX, dragInitialY;
    private boolean isDragging = false;

    /** Threshold jarak minimum untuk dianggap drag (pixel). */
    private static final int DRAG_THRESHOLD = 10;

    // --- Thermal warning dialog ---
    private ThermalWarningDialog thermalDialog;

    // --- Battery receiver ---
    private BroadcastReceiver batteryReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());
        deviceDetector = new DeviceDetector(this);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        createNotificationChannel();
        thermalDialog = new ThermalWarningDialog(this);
        thermalDialog.setOnThermalActionListener(new ThermalWarningDialog.OnThermalActionListener() {
            @Override
            public void onDeactivateRequested(float temperature) {
                Log.d(TAG, "Thermal dialog: user requested deactivation at " + temperature + "°C");
                stopSelf();
            }

            @Override
            public void onContinueRequested(float temperature) {
                Log.d(TAG, "Thermal dialog: user continued at " + temperature + "°C");
            }
        });
        Log.d(TAG, "Service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            // Restart setelah system kill
            startForeground(NOTIFICATION_ID, buildNotification("Memulai ulang..."));
            stopSelf();
            return START_NOT_STICKY;
        }

        String action = intent.getAction();

        if (ACTION_STOP.equals(action)) {
            Log.d(TAG, "Stop action received");
            stopSelf();
            return START_NOT_STICKY;
        }

        if (ACTION_TOGGLE.equals(action)) {
            toggleExpandCollapse();
            return START_STICKY;
        }

        // Parse extras
        gamePackage = intent.getStringExtra(EXTRA_GAME_PACKAGE);
        gameName = intent.getStringExtra(EXTRA_GAME_NAME);
        activeProfile = intent.getStringExtra(EXTRA_PROFILE);

        if (gamePackage == null) gamePackage = "";
        if (gameName == null) gameName = "Game";
        if (activeProfile == null) activeProfile = "balanced";

        // Start foreground + overlay
        Notification notification = buildNotification("Memulai monitoring...");
        startForeground(NOTIFICATION_ID, notification);

        if (!isRunning) {
            sessionStartTime = System.currentTimeMillis();
            isRunning = true;
            createOverlay();
            startStatsUpdater();
            registerBatteryReceiver();

            // Start session tracking
            SystemInfo info = deviceDetector.getSystemInfo();
            currentSession = GameSessionStats.startSession(
                    gamePackage, gameName, activeProfile, info.batteryPercent);

            Log.d(TAG, "Overlay started for: " + gameName + " (" + gamePackage + ")");
        }

        return START_STICKY;
    }

    // ========================================================================
    // OVERLAY WINDOW
    // ========================================================================

    /**
     * Membuat overlay window dan inflate layout.
     * Menggunakan TYPE_APPLICATION_OVERLAY untuk Android 8.0+.
     */
    private void createOverlay() {
        if (overlayView != null) return; // already created

        LayoutInflater inflater = LayoutInflater.from(this);
        overlayView = inflater.inflate(R.layout.floating_monitor_overlay, null);

        // Bind expanded views
        expandedView = overlayView.findViewById(R.id.expanded_view);
        statFps = overlayView.findViewById(R.id.stat_fps);
        statTemp = overlayView.findViewById(R.id.stat_temp);
        statRam = overlayView.findViewById(R.id.stat_ram);
        statBattery = overlayView.findViewById(R.id.stat_battery);
        statSessionTime = overlayView.findViewById(R.id.stat_session_time);
        tempWarningIcon = overlayView.findViewById(R.id.temp_warning_icon);
        statusDot = overlayView.findViewById(R.id.status_dot);
        timerDot = overlayView.findViewById(R.id.timer_dot);

        // Bind collapsed views
        collapsedView = overlayView.findViewById(R.id.collapsed_view);
        collapsedFps = overlayView.findViewById(R.id.collapsed_fps);

        // Bind buttons
        ImageView btnCollapse = overlayView.findViewById(R.id.btn_collapse);
        ImageView btnClose = overlayView.findViewById(R.id.btn_close);

        btnCollapse.setOnClickListener(v -> toggleExpandCollapse());
        btnClose.setOnClickListener(v -> stopSelf());

        // Collapsed view: tap to expand
        collapsedView.setOnClickListener(v -> {
            if (!isDragging) {
                toggleExpandCollapse();
            }
        });

        // Setup window layout params
        int overlayType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            overlayType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            overlayType = WindowManager.LayoutParams.TYPE_PHONE;
        }

        layoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                overlayType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);

        layoutParams.gravity = Gravity.TOP | Gravity.START;
        layoutParams.x = 20;
        layoutParams.y = 200;

        // Attach drag listener to entire overlay
        overlayView.setOnTouchListener(new DragTouchListener());

        try {
            windowManager.addView(overlayView, layoutParams);
            Log.d(TAG, "Overlay view added to window");
        } catch (Exception e) {
            Log.e(TAG, "Failed to add overlay view", e);
            // Fallback: overlay tidak tersedia, tetap jalan via notification
        }
    }

    /**
     * Hapus overlay window dari WindowManager.
     */
    private void removeOverlay() {
        if (overlayView != null && windowManager != null) {
            try {
                windowManager.removeView(overlayView);
            } catch (Exception e) {
                Log.w(TAG, "Overlay already removed or not attached", e);
            }
            overlayView = null;
        }
    }

    /**
     * Toggle antara expanded (full panel) dan collapsed (mini circle).
     */
    private void toggleExpandCollapse() {
        if (overlayView == null) return;

        isExpanded = !isExpanded;

        if (isExpanded) {
            expandedView.setVisibility(View.VISIBLE);
            collapsedView.setVisibility(View.GONE);
        } else {
            expandedView.setVisibility(View.GONE);
            collapsedView.setVisibility(View.VISIBLE);
        }

        // Re-layout
        try {
            windowManager.updateViewLayout(overlayView, layoutParams);
        } catch (Exception e) {
            Log.w(TAG, "Failed to update layout on toggle", e);
        }
    }

    // ========================================================================
    // STATS UPDATER
    // ========================================================================

    /**
     * Mulai loop update stats secara periodik.
     * Dipanggil setiap {@link #UPDATE_INTERVAL_MS} milidetik.
     */
    private void startStatsUpdater() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isRunning) return;

                updateStats();

                // Repeat
                handler.postDelayed(this, UPDATE_INTERVAL_MS);
            }
        }, 1000); // delay awal 1 detik
    }

    /**
     * Ambil data terbaru dari DeviceDetector dan perbarui semua view.
     */
    private void updateStats() {
        SystemInfo info = deviceDetector.getSystemInfo();

        // FPS: estimasi sederhana dari SurfaceFlinger (atau dummy jika tidak tersedia)
        int fps = estimateFps();

        // Update session stats tracker
        if (currentSession != null) {
            currentSession.recordSample(
                    info.batteryTemp,
                    fps,
                    info.getUsedRam());
        }

        // --- Update expanded panel ---
        if (statFps != null) {
            statFps.setText(String.valueOf(fps));
            statFps.setTextColor(getColorForFps(fps));
        }

        if (statTemp != null) {
            statTemp.setText(info.getTempDisplay());
            statTemp.setTextColor(getColorForTemp(info.batteryTemp));
        }

        if (statRam != null) {
            statRam.setText(info.availableRam + "MB");
        }

        if (statBattery != null) {
            statBattery.setText(info.batteryPercent + "%");
            statBattery.setTextColor(getColorForBattery(info.batteryPercent));
        }

        // Session timer
        if (statSessionTime != null) {
            long elapsedMs = System.currentTimeMillis() - sessionStartTime;
            statSessionTime.setText(formatDuration(elapsedMs));
        }

        // Temp warning icon
        if (tempWarningIcon != null) {
            if (info.batteryTemp >= TEMP_WARNING_THRESHOLD) {
                tempWarningIcon.setVisibility(View.VISIBLE);
                if (info.batteryTemp >= TEMP_HOT_THRESHOLD) {
                    tempWarningIcon.setColorFilter(getResources().getColor(R.color.status_danger));
                } else {
                    tempWarningIcon.setColorFilter(getResources().getColor(R.color.status_warning));
                }
            } else {
                tempWarningIcon.setVisibility(View.GONE);
            }
        }

        // Show full thermal warning dialog if temperature exceeds critical threshold
        if (info.batteryTemp >= TEMP_CRITICAL_DIALOG_THRESHOLD && thermalDialog != null) {
            thermalDialog.show(info.batteryTemp);
        }

        // --- Update collapsed view ---
        if (collapsedFps != null) {
            collapsedFps.setText(String.valueOf(fps));
            collapsedFps.setTextColor(getColorForFps(fps));
        }

        // --- Update notification ---
        String notifText = String.format(
                "FPS: %d | Suhu: %s | RAM: %dMB | Bat: %d%%",
                fps, info.getTempDisplay(), info.availableRam, info.batteryPercent);
        NotificationManager nm = getSystemService(NotificationManager.class);
        if (nm != null) {
            nm.notify(NOTIFICATION_ID, buildNotification(notifText));
        }
    }

    /**
     * Estimasi FPS menggunakan SurfaceFlinger via Shizuku.
     * Jika gagal, return 0 (tidak tersedia).
     *
     * @return FPS estimasi, atau 0 jika tidak bisa dibaca
     */
    private int estimateFps() {
        try {
            if (com.modedewa.gamebooster.util.ShizukuShell.isAvailable()
                    && com.modedewa.gamebooster.util.ShizukuShell.hasPermission()) {
                String result = com.modedewa.gamebooster.util.ShizukuShell.executeGetLine(
                        "dumpsys SurfaceFlinger --latency 2>/dev/null | head -2 | tail -1");
                if (result != null && !result.isEmpty()) {
                    // SurfaceFlinger latency output: refresh_period_ns
                    // FPS = 1e9 / refresh_period
                    try {
                        long periodNs = Long.parseLong(result.trim());
                        if (periodNs > 0) {
                            return (int) (1_000_000_000L / periodNs);
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "FPS estimation failed", e);
        }
        return 0;
    }

    // ========================================================================
    // COLOR HELPERS
    // ========================================================================

    /**
     * Warna berdasarkan FPS: hijau (>=50), kuning (>=30), merah (<30).
     */
    private int getColorForFps(int fps) {
        if (fps <= 0) return getResources().getColor(R.color.text_tertiary);
        if (fps >= 50) return getResources().getColor(R.color.neon_green);
        if (fps >= 30) return getResources().getColor(R.color.neon_orange);
        return getResources().getColor(R.color.status_danger);
    }

    /**
     * Warna berdasarkan suhu: cyan (<40), kuning (<55), merah (>=55).
     */
    private int getColorForTemp(float temp) {
        if (temp <= 0) return getResources().getColor(R.color.text_tertiary);
        if (temp < 40) return getResources().getColor(R.color.neon_cyan);
        if (temp < 55) return getResources().getColor(R.color.neon_orange);
        return getResources().getColor(R.color.status_danger);
    }

    /**
     * Warna berdasarkan baterai: hijau (>50), kuning (>20), merah (<=20).
     */
    private int getColorForBattery(int percent) {
        if (percent > 50) return getResources().getColor(R.color.neon_green);
        if (percent > 20) return getResources().getColor(R.color.neon_orange);
        return getResources().getColor(R.color.status_danger);
    }

    // ========================================================================
    // FORMAT HELPERS
    // ========================================================================

    /**
     * Format milidetik ke "HH:MM:SS".
     */
    private String formatDuration(long ms) {
        long totalSec = ms / 1000;
        long hours = totalSec / 3600;
        long minutes = (totalSec % 3600) / 60;
        long seconds = totalSec % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    // ========================================================================
    // DRAG TOUCH LISTENER
    // ========================================================================

    /**
     * Touch listener untuk drag overlay window.
     * Membedakan tap dan drag menggunakan {@link #DRAG_THRESHOLD}.
     */
    private class DragTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    dragStartX = event.getRawX();
                    dragStartY = event.getRawY();
                    dragInitialX = layoutParams.x;
                    dragInitialY = layoutParams.y;
                    isDragging = false;
                    return true;

                case MotionEvent.ACTION_MOVE:
                    float dx = event.getRawX() - dragStartX;
                    float dy = event.getRawY() - dragStartY;

                    if (Math.abs(dx) > DRAG_THRESHOLD || Math.abs(dy) > DRAG_THRESHOLD) {
                        isDragging = true;
                    }

                    if (isDragging) {
                        layoutParams.x = dragInitialX + (int) dx;
                        layoutParams.y = dragInitialY + (int) dy;
                        try {
                            windowManager.updateViewLayout(overlayView, layoutParams);
                        } catch (Exception e) {
                            // View mungkin sudah dihapus
                        }
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                    if (!isDragging) {
                        // Ini tap, bukan drag — biarkan onClick handler
                        v.performClick();
                    }
                    return true;
            }
            return false;
        }
    }

    // ========================================================================
    // NOTIFICATION
    // ========================================================================

    /**
     * Build notification foreground service dengan stats text.
     *
     * @param contentText teks stats yang ditampilkan di notification body
     * @return Notification yang siap ditampilkan
     */
    private Notification buildNotification(String contentText) {
        Intent openIntent = new Intent(this, HomeActivity.class);
        PendingIntent pendingOpen = PendingIntent.getActivity(
                this, 0, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent stopIntent = new Intent(this, FloatingMonitorService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent pendingStop = PendingIntent.getService(
                this, 1, stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent toggleIntent = new Intent(this, FloatingMonitorService.class);
        toggleIntent.setAction(ACTION_TOGGLE);
        PendingIntent pendingToggle = PendingIntent.getService(
                this, 2, toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String title = getString(R.string.monitor_notification_title);
        if (!gameName.isEmpty()) {
            title += " — " + gameName;
        }

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .setContentTitle(title)
                .setContentText(contentText)
                .setContentIntent(pendingOpen)
                .addAction(android.R.drawable.ic_menu_view,
                        "Toggle HUD", pendingToggle)
                .addAction(android.R.drawable.ic_delete,
                        "Stop Monitor", pendingStop)
                .setOngoing(true)
                .setSilent(true)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build();
    }

    /**
     * Buat notification channel (wajib untuk Android 8.0+).
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Game Mode Monitor",
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Monitoring performa saat mode dewa aktif");
            channel.setShowBadge(false);
            getSystemService(NotificationManager.class)
                    .createNotificationChannel(channel);
        }
    }

    // ========================================================================
    // BATTERY RECEIVER
    // ========================================================================

    /**
     * Register broadcast receiver untuk perubahan baterai.
     * Digunakan untuk update cepat saat charging state berubah.
     */
    private void registerBatteryReceiver() {
        if (batteryReceiver != null) return;

        batteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Trigger immediate stats refresh on battery change
                if (isRunning) {
                    handler.post(() -> updateStats());
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(batteryReceiver, filter);
    }

    /**
     * Unregister battery receiver.
     */
    private void unregisterBatteryReceiver() {
        if (batteryReceiver != null) {
            try {
                unregisterReceiver(batteryReceiver);
            } catch (Exception ignored) {}
            batteryReceiver = null;
        }
    }

    // ========================================================================
    // LIFECYCLE
    // ========================================================================

    /**
     * Simpan session stats dan bersihkan semua resource.
     */
    @Override
    public void onDestroy() {
        Log.d(TAG, "Service destroying");
        isRunning = false;

        // Stop handler
        handler.removeCallbacksAndMessages(null);

        // Dismiss thermal dialog if showing
        if (thermalDialog != null && thermalDialog.isShowing()) {
            thermalDialog.dismiss();
        }

        // Save session stats
        if (currentSession != null) {
            SystemInfo info = deviceDetector.getSystemInfo();
            currentSession.endSession(info.batteryPercent);
            currentSession.saveToHistory(this);
            Log.d(TAG, "Session saved: " + currentSession.getFormattedDuration());
        }

        // Remove overlay
        removeOverlay();

        // Unregister receiver
        unregisterBatteryReceiver();

        super.onDestroy();
    }

    /**
     * Service ini tidak mendukung binding.
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // ========================================================================
    // PUBLIC HELPER: Session access
    // ========================================================================

    /**
     * Mendapatkan sesi gaming yang sedang berjalan.
     * Bisa null jika service belum dimulai dengan benar.
     *
     * @return GameSessionStats saat ini, atau null
     */
    public GameSessionStats getCurrentSession() {
        return currentSession;
    }

    /**
     * Mendapatkan durasi sesi saat ini dalam milidetik.
     *
     * @return durasi sesi dalam ms, atau 0 jika belum dimulai
     */
    public long getSessionDurationMs() {
        if (sessionStartTime <= 0) return 0;
        return System.currentTimeMillis() - sessionStartTime;
    }
}
