package com.modedewa.gamebooster.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;

import com.modedewa.gamebooster.R;
import com.modedewa.gamebooster.model.AppDetectionConfig;
import com.modedewa.gamebooster.service.GameDetectionService;
import com.modedewa.gamebooster.ui.AutoDetectActivity;

/**
 * AutoDetectWidget — Widget 1x1 untuk home screen.
 *
 * Fungsi:
 * - Tap: Toggle auto-detect ON/OFF
 * - Long press: Buka AutoDetectActivity
 * - Visual: Ikon radar + status text (AKTIF/MATI) dengan warna neon
 *
 * Widget otomatis update saat GameDetectionService broadcast status.
 */
public class AutoDetectWidget extends AppWidgetProvider {

    public static final String ACTION_TOGGLE = "com.modedewa.WIDGET_TOGGLE";
    private static final int REQUEST_TOGGLE = 100;
    private static final int REQUEST_OPEN = 101;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (ACTION_TOGGLE.equals(intent.getAction())) {
            // Toggle auto-detect
            AppDetectionConfig config = AppDetectionConfig.load(context);
            config.isEnabled = !config.isEnabled;
            config.save(context);

            // Start/stop service
            Intent svcIntent = new Intent(context, GameDetectionService.class);
            if (config.isEnabled) {
                svcIntent.setAction(GameDetectionService.ACTION_START_DETECTION);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(svcIntent);
                } else {
                    context.startService(svcIntent);
                }
            } else {
                svcIntent.setAction(GameDetectionService.ACTION_STOP_DETECTION);
                context.startService(svcIntent);
            }

            // Update all widgets
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            int[] ids = manager.getAppWidgetIds(
                    new ComponentName(context, AutoDetectWidget.class));
            for (int id : ids) {
                updateWidget(context, manager, id);
            }
        }
    }

    /**
     * Update tampilan widget berdasarkan state auto-detect.
     */
    private static void updateWidget(Context context, AppWidgetManager manager, int widgetId) {
        AppDetectionConfig config = AppDetectionConfig.load(context);
        boolean isActive = config.isEnabled;

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_auto_detect);

        // Update visual
        if (isActive) {
            views.setTextViewText(R.id.textWidgetStatus, "AKTIF");
            views.setTextColor(R.id.textWidgetStatus,
                    context.getColor(R.color.neon_green));
            views.setInt(R.id.containerWidget, "setBackgroundResource",
                    R.drawable.bg_widget_active);
        } else {
            views.setTextViewText(R.id.textWidgetStatus, "MATI");
            views.setTextColor(R.id.textWidgetStatus,
                    context.getColor(R.color.status_inactive));
            views.setInt(R.id.containerWidget, "setBackgroundResource",
                    R.drawable.bg_widget_inactive);
        }

        // Tap -> Toggle
        Intent toggleIntent = new Intent(context, AutoDetectWidget.class);
        toggleIntent.setAction(ACTION_TOGGLE);
        PendingIntent togglePending = PendingIntent.getBroadcast(context, REQUEST_TOGGLE,
                toggleIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.containerWidget, togglePending);

        // Long press -> Open activity (via secondary tap area)
        Intent openIntent = new Intent(context, AutoDetectActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent openPending = PendingIntent.getActivity(context, REQUEST_OPEN,
                openIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.btnWidgetOpen, openPending);

        manager.updateAppWidget(widgetId, views);
    }

    /**
     * Static method untuk update widget dari luar (misal dari Service).
     */
    public static void notifyUpdate(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] ids = manager.getAppWidgetIds(
                new ComponentName(context, AutoDetectWidget.class));
        for (int id : ids) {
            updateWidget(context, manager, id);
        }
    }
}
