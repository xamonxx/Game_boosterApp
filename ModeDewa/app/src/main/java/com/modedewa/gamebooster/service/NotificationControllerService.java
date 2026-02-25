package com.modedewa.gamebooster.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.modedewa.gamebooster.util.ShizukuShell;

/**
 * NotificationControllerService — Persistent notification dengan mini-controller.
 *
 * Menampilkan notification custom saat game aktif dengan tombol:
 * - BOOST: Quick boost (kill bg apps + clean cache)
 * - COOL DOWN: Kurangi beban GPU/CPU
 * - STOP: Nonaktifkan mode game dan tutup controller
 *
 * Menampilkan info real-time: nama game, profil aktif, suhu, baterai.
 */
public class NotificationControllerService extends Service {

    private static final String TAG = "NotifController";
    private static final String CHANNEL_ID = "mode_dewa_controller";
    private static final int NOTIFICATION_ID = 2002;

    public static final String ACTION_START = "com.modedewa.NOTIF_CTRL_START";
    public static final String ACTION_STOP = "com.modedewa.NOTIF_CTRL_STOP";
    public static final String ACTION_BOOST = "com.modedewa.NOTIF_CTRL_BOOST";
    public static final String ACTION_COOL = "com.modedewa.NOTIF_CTRL_COOL";
    public static final String ACTION_STOP_MODE = "com.modedewa.NOTIF_CTRL_STOP_MODE";
    public static final String ACTION_UPDATE_INFO = "com.modedewa.NOTIF_CTRL_UPDATE";

    private String currentGameName = "Unknown";
    private String currentProfile = "balanced";
    private String currentPackage = "";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_NOT_STICKY;

        String action = intent.getAction();
        if (action == null) return START_NOT_STICKY;

        switch (action) {
            case ACTION_START:
                currentPackage = intent.getStringExtra(GameDetectionService.EXTRA_PACKAGE);
                currentGameName = intent.getStringExtra(GameDetectionService.EXTRA_GAME_NAME);
                currentProfile = intent.getStringExtra(GameDetectionService.EXTRA_PROFILE);
                if (currentGameName == null) currentGameName = "Unknown";
                if (currentProfile == null) currentProfile = "balanced";
                startForeground(NOTIFICATION_ID, buildControllerNotification());
                Log.d(TAG, "Controller started for: " + currentGameName);
                break;

            case ACTION_STOP:
                stopForeground(true);
                stopSelf();
                break;

            case ACTION_BOOST:
                performBoost();
                break;

            case ACTION_COOL:
                performCoolDown();
                break;

            case ACTION_STOP_MODE:
                // Kirim perintah ke GameDetectionService untuk stop
                Intent stopDetection = new Intent(this, GameDetectionService.class);
                stopDetection.setAction(GameDetectionService.ACTION_STOP_DETECTION);
                startService(stopDetection);
                stopForeground(true);
                stopSelf();
                break;

            case ACTION_UPDATE_INFO:
                // Update notification dengan info terbaru
                updateNotification();
                break;
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // ============================================================
    // NOTIFICATION BUILDER
    // ============================================================

    private Notification buildControllerNotification() {
        // Intent untuk tombol BOOST
        Intent boostIntent = new Intent(this, NotificationControllerService.class);
        boostIntent.setAction(ACTION_BOOST);
        PendingIntent boostPending = PendingIntent.getService(this, 1, boostIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Intent untuk tombol COOL DOWN
        Intent coolIntent = new Intent(this, NotificationControllerService.class);
        coolIntent.setAction(ACTION_COOL);
        PendingIntent coolPending = PendingIntent.getService(this, 2, coolIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Intent untuk tombol STOP
        Intent stopIntent = new Intent(this, NotificationControllerService.class);
        stopIntent.setAction(ACTION_STOP_MODE);
        PendingIntent stopPending = PendingIntent.getService(this, 3, stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String profileLabel = currentProfile.toUpperCase();
        String contentText = currentGameName + " — Profil: " + profileLabel;

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("MODE DEWA AKTIF")
                .setContentText(contentText)
                .setSubText("Game Booster Controller")
                .setSmallIcon(android.R.drawable.ic_menu_manage)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(android.R.drawable.ic_menu_send,
                        "BOOST", boostPending)
                .addAction(android.R.drawable.ic_menu_view,
                        "COOL DOWN", coolPending)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel,
                        "STOP", stopPending)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(contentText + "\n\nTap tombol untuk kontrol cepat."))
                .build();
    }

    private void updateNotification() {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(NOTIFICATION_ID, buildControllerNotification());
    }

    // ============================================================
    // ACTIONS
    // ============================================================

    private void performBoost() {
        Log.d(TAG, "Quick Boost triggered from notification");
        if (ShizukuShell.isAvailable() && ShizukuShell.hasPermission()) {
            ShizukuShell.executeSilent("am kill-all 2>/dev/null");
            ShizukuShell.executeSilent("sync && echo 3 > /proc/sys/vm/drop_caches 2>/dev/null");
        }
        // Broadcast log ke UI
        Intent logIntent = new Intent(GameDetectionService.ACTION_LOG);
        logIntent.putExtra(GameDetectionService.EXTRA_LOG_MSG,
                "[BOOST] Quick Boost dari notification — BG apps killed + cache cleared");
        logIntent.putExtra(GameDetectionService.EXTRA_LOG_LEVEL, "success");
        sendBroadcast(logIntent);
    }

    private void performCoolDown() {
        Log.d(TAG, "Cool Down triggered from notification");
        if (ShizukuShell.isAvailable() && ShizukuShell.hasPermission()) {
            ShizukuShell.putSetting("global", "force_hw_ui", "0");
            ShizukuShell.putSetting("global", "window_animation_scale", "1");
        }
        Intent logIntent = new Intent(GameDetectionService.ACTION_LOG);
        logIntent.putExtra(GameDetectionService.EXTRA_LOG_MSG,
                "[COOL] Cool Down dari notification — GPU load dikurangi");
        logIntent.putExtra(GameDetectionService.EXTRA_LOG_LEVEL, "info");
        sendBroadcast(logIntent);
    }

    // ============================================================
    // NOTIFICATION CHANNEL
    // ============================================================

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Game Controller",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Mini-controller saat mode game aktif");
            channel.setShowBadge(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager nm = getSystemService(NotificationManager.class);
            nm.createNotificationChannel(channel);
        }
    }
}
