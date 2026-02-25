package com.modedewa.gamebooster.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

import com.modedewa.gamebooster.R;
import com.modedewa.gamebooster.service.FloatingMonitorService;
import com.modedewa.gamebooster.service.GameDetectionService;

/**
 * ThermalWarningDialog — Displays a system overlay warning dialog when the
 * device temperature exceeds a critical threshold during gaming.
 *
 * <p>Matches the design from {@code thermal_warning_dialog/code.html}:
 * hazard stripes, pulsing warning icon, temperature readout, danger callout,
 * and two action buttons (deactivate / continue).
 *
 * <p>This dialog is shown as a {@link WindowManager} overlay (TYPE_APPLICATION_OVERLAY)
 * so it can appear on top of games, even when called from a background service.
 * Requires {@code SYSTEM_ALERT_WINDOW} permission.
 *
 * <p>Usage from a Service:
 * <pre>{@code
 *   ThermalWarningDialog dialog = new ThermalWarningDialog(context);
 *   dialog.show(currentTemperature);
 * }</pre>
 *
 * <p>The dialog auto-dismisses if not interacted with after
 * {@link #AUTO_DISMISS_MS} milliseconds, and has a cooldown period
 * ({@link #COOLDOWN_MS}) to prevent spamming the user.
 */
public class ThermalWarningDialog {

    private static final String TAG = "ThermalWarningDialog";

    /** Auto-dismiss timeout in milliseconds (30 seconds). */
    private static final long AUTO_DISMISS_MS = 30_000;

    /** Minimum interval between consecutive dialog shows (60 seconds). */
    private static final long COOLDOWN_MS = 60_000;

    /** Timestamp of last dialog show, used for cooldown. */
    private static long lastShownTimestamp = 0;

    private final Context context;
    private final WindowManager windowManager;
    private final Handler handler;

    private View dialogView;
    private boolean isShowing = false;

    /** Listener for dialog actions. */
    private OnThermalActionListener listener;

    /**
     * Callback interface for thermal warning dialog actions.
     */
    public interface OnThermalActionListener {
        /**
         * Called when the user chooses to deactivate game mode.
         *
         * @param temperature the temperature at which deactivation was requested
         */
        void onDeactivateRequested(float temperature);

        /**
         * Called when the user dismisses the warning and chooses to continue.
         *
         * @param temperature the temperature at which the warning was dismissed
         */
        void onContinueRequested(float temperature);
    }

    /**
     * Creates a new ThermalWarningDialog bound to the given context.
     * The context should be a Service context with overlay permission.
     *
     * @param context application or service context (must have SYSTEM_ALERT_WINDOW)
     */
    public ThermalWarningDialog(Context context) {
        this.context = context.getApplicationContext();
        this.windowManager = (WindowManager) this.context.getSystemService(Context.WINDOW_SERVICE);
        this.handler = new Handler(Looper.getMainLooper());
    }

    /**
     * Sets the action listener for deactivate/continue callbacks.
     *
     * @param listener the listener to receive action callbacks
     */
    public void setOnThermalActionListener(OnThermalActionListener listener) {
        this.listener = listener;
    }

    /**
     * Shows the thermal warning dialog as a system overlay.
     * Respects the cooldown period — if called again within {@link #COOLDOWN_MS}
     * of the last show, the call is silently ignored.
     *
     * @param currentTemp the current device temperature in Celsius
     */
    public void show(float currentTemp) {
        // Cooldown check
        long now = System.currentTimeMillis();
        if (now - lastShownTimestamp < COOLDOWN_MS) {
            Log.d(TAG, "Cooldown active, skipping thermal dialog");
            return;
        }

        // Already showing
        if (isShowing) {
            Log.d(TAG, "Dialog already showing, updating temperature");
            updateTemperature(currentTemp);
            return;
        }

        lastShownTimestamp = now;

        try {
            createAndShowDialog(currentTemp);
        } catch (Exception e) {
            Log.e(TAG, "Failed to show thermal warning dialog", e);
        }
    }

    /**
     * Dismisses the dialog if it is currently showing.
     */
    public void dismiss() {
        if (!isShowing || dialogView == null) return;

        handler.removeCallbacksAndMessages(null);

        // Animate out
        AnimationSet fadeOut = new AnimationSet(true);
        fadeOut.addAnimation(new AlphaAnimation(1f, 0f));
        fadeOut.addAnimation(new ScaleAnimation(
                1f, 0.9f, 1f, 0.9f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f));
        fadeOut.setDuration(200);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                removeDialogView();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        dialogView.startAnimation(fadeOut);
    }

    /**
     * Returns whether the dialog is currently visible.
     *
     * @return {@code true} if the dialog overlay is showing
     */
    public boolean isShowing() {
        return isShowing;
    }

    /**
     * Checks whether the cooldown period has elapsed since the last show.
     *
     * @return {@code true} if the dialog can be shown again
     */
    public static boolean isCooldownElapsed() {
        return System.currentTimeMillis() - lastShownTimestamp >= COOLDOWN_MS;
    }

    /**
     * Resets the cooldown timer, allowing the dialog to be shown immediately.
     */
    public static void resetCooldown() {
        lastShownTimestamp = 0;
    }

    // ========================================================================
    // INTERNAL
    // ========================================================================

    /**
     * Inflates the dialog layout, wires up buttons, and adds it to the window.
     */
    private void createAndShowDialog(float currentTemp) {
        LayoutInflater inflater = LayoutInflater.from(context);
        dialogView = inflater.inflate(R.layout.dialog_thermal_warning, null);

        // Bind temperature value
        TextView tempValue = dialogView.findViewById(R.id.thermal_temp_value);
        if (tempValue != null) {
            tempValue.setText(String.format("%.0f°C", currentTemp));
        }

        // Bind primary button: Matikan Mode
        View btnDeactivate = dialogView.findViewById(R.id.btn_thermal_deactivate);
        if (btnDeactivate != null) {
            btnDeactivate.setOnClickListener(v -> {
                Log.d(TAG, "User chose to deactivate at " + currentTemp + "°C");
                if (listener != null) {
                    listener.onDeactivateRequested(currentTemp);
                } else {
                    // Fallback: stop services directly
                    stopGameModeServices();
                }
                dismiss();
            });
        }

        // Bind secondary button: Lanjutkan
        View btnContinue = dialogView.findViewById(R.id.btn_thermal_continue);
        if (btnContinue != null) {
            btnContinue.setOnClickListener(v -> {
                Log.d(TAG, "User chose to continue at " + currentTemp + "°C");
                if (listener != null) {
                    listener.onContinueRequested(currentTemp);
                }
                dismiss();
            });
        }

        // Window params
        int overlayType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            overlayType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            overlayType = WindowManager.LayoutParams.TYPE_PHONE;
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                overlayType,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.CENTER;

        // Dim the background behind the dialog
        params.dimAmount = 0.6f;
        params.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;

        try {
            windowManager.addView(dialogView, params);
            isShowing = true;
            Log.d(TAG, "Thermal warning dialog shown at " + currentTemp + "°C");
        } catch (Exception e) {
            Log.e(TAG, "Failed to add dialog view to window", e);
            isShowing = false;
            return;
        }

        // Entrance animation
        AnimationSet fadeIn = new AnimationSet(true);
        fadeIn.addAnimation(new AlphaAnimation(0f, 1f));
        fadeIn.addAnimation(new ScaleAnimation(
                0.85f, 1f, 0.85f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f));
        fadeIn.setDuration(300);
        dialogView.startAnimation(fadeIn);

        // Auto-dismiss after timeout
        handler.postDelayed(() -> {
            if (isShowing) {
                Log.d(TAG, "Auto-dismissing thermal dialog after timeout");
                dismiss();
            }
        }, AUTO_DISMISS_MS);
    }

    /**
     * Updates the temperature display if the dialog is already showing.
     *
     * @param newTemp updated temperature value
     */
    private void updateTemperature(float newTemp) {
        if (dialogView == null) return;
        TextView tempValue = dialogView.findViewById(R.id.thermal_temp_value);
        if (tempValue != null) {
            tempValue.setText(String.format("%.0f°C", newTemp));
        }
    }

    /**
     * Removes the dialog view from the WindowManager.
     */
    private void removeDialogView() {
        if (dialogView != null && windowManager != null) {
            try {
                windowManager.removeView(dialogView);
            } catch (Exception e) {
                Log.w(TAG, "Dialog view already removed", e);
            }
            dialogView = null;
        }
        isShowing = false;
    }

    /**
     * Fallback: directly stop game mode services if no listener is set.
     * Sends stop intents to both FloatingMonitorService and GameDetectionService.
     */
    private void stopGameModeServices() {
        try {
            // Stop floating monitor
            Intent stopMonitor = new Intent(context, FloatingMonitorService.class);
            stopMonitor.setAction(FloatingMonitorService.ACTION_STOP);
            context.startService(stopMonitor);

            // Stop game detection
            Intent stopDetection = new Intent(context, GameDetectionService.class);
            stopDetection.setAction(GameDetectionService.ACTION_STOP_DETECTION);
            context.startService(stopDetection);
        } catch (Exception e) {
            Log.e(TAG, "Failed to stop services", e);
        }
    }
}
