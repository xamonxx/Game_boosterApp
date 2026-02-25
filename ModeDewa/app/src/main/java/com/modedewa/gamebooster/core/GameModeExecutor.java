package com.modedewa.gamebooster.core;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.modedewa.gamebooster.model.OptimizationStep;
import com.modedewa.gamebooster.util.SimProtector;

import java.util.ArrayList;
import java.util.List;

/**
 * GameModeExecutor — Main orchestrator that runs all optimization modules
 * in sequence with progress reporting. This is the heart of the app.
 * 
 * Translates the full flow of modeGameON.sh and modeGameOFF.sh into Java.
 */
public class GameModeExecutor {

    private static final String TAG = "GameModeExecutor";

    /**
     * Listener interface for receiving progress updates during game mode
     * activation or deactivation. Callbacks are dispatched on the main thread.
     */
    public interface ProgressListener {
        /**
         * Called when an optimization step begins execution.
         *
         * @param stepIndex zero-based index of the step in the sequence
         * @param step      the {@link OptimizationStep} that is starting
         */
        void onStepStarted(int stepIndex, OptimizationStep step);

        /**
         * Called when an optimization step finishes (success or failure).
         *
         * @param stepIndex zero-based index of the completed step
         * @param step      the {@link OptimizationStep} with updated status and message
         */
        void onStepCompleted(int stepIndex, OptimizationStep step);

        /**
         * Called after all steps have been executed.
         *
         * @param success {@code true} if every step succeeded, {@code false} if any step failed
         * @param message human-readable summary of the result (Indonesian)
         */
        void onAllCompleted(boolean success, String message);
    }

    private final String targetGame;
    private ProgressListener listener;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final android.content.Context context;

    /**
     * Creates an executor bound to the given game package.
     *
     * @param context the application context for resource access
     * @param targetGamePackage fully-qualified package name of the game to optimise for
     *                          (e.g. {@code "com.mobile.legends"})
     */
    public GameModeExecutor(android.content.Context context, String targetGamePackage) {
        this.context = context.getApplicationContext();
        this.targetGame = targetGamePackage;
    }

    /**
     * Registers a listener that will receive step-by-step progress callbacks
     * on the main thread. Pass {@code null} to remove the current listener.
     *
     * @param listener the {@link ProgressListener} to register, or {@code null}
     */
    public void setProgressListener(ProgressListener listener) {
        this.listener = listener;
    }

    // ═══════════════════════════════════════════════
    // ACTIVATE GAME MODE (modeGameON.sh equivalent)
    // ═══════════════════════════════════════════════

    /**
     * Activates game mode by running all optimisation steps on a background thread.
     * Equivalent to {@code modeGameON.sh}. Steps include: disable animations,
     * enable performance mode, kill background apps, optimise network/display/audio,
     * block notifications, disable sync, and disable non-essential apps.
     * Progress is reported via the registered {@link ProgressListener}.
     */
    public void activateGameMode() {
        new Thread(() -> {
            Log.d(TAG, "═══ ACTIVATING GAME MODE ═══");
            List<OptimizationStep> steps = buildActivateSteps();
            boolean allOk = true;

            for (int i = 0; i < steps.size(); i++) {
                OptimizationStep step = steps.get(i);
                step.status = OptimizationStep.Status.RUNNING;
                notifyStepStarted(i, step);

                try {
                    boolean result = executeActivateStep(i, step);
                    step.status = result ? OptimizationStep.Status.SUCCESS
                            : OptimizationStep.Status.FAILED;
                    if (!result) allOk = false;
                } catch (Exception e) {
                    step.status = OptimizationStep.Status.FAILED;
                    step.message = e.getMessage();
                    allOk = false;
                    Log.e(TAG, "Step failed: " + step.name, e);
                }

                notifyStepCompleted(i, step);

                // Small delay between steps for stability
                try { Thread.sleep(200); } catch (InterruptedException ignored) {}
            }

            String msg = allOk ? "Mode Dewa aktif! 🔥" : "Beberapa optimasi gagal, tapi mode aktif.";
            notifyAllCompleted(allOk, msg);
            Log.d(TAG, "═══ GAME MODE ACTIVATED ═══");
        }).start();
    }

    // ═══════════════════════════════════════════════
    // DEACTIVATE GAME MODE (modeGameOFF.sh equivalent)
    // ═══════════════════════════════════════════════

    /**
     * Deactivates game mode and restores all settings to their defaults on a
     * background thread. Equivalent to {@code modeGameOFF.sh}. Steps include:
     * restore animations, reset performance mode, restore network/display/audio,
     * re-enable notifications, sync, and previously-disabled apps.
     * Progress is reported via the registered {@link ProgressListener}.
     */
            default: return false;
        }
    }

    private boolean executeDeactivateStep(int index, OptimizationStep step) {
        switch (index) {
            case 0:  return SettingsTweaker.restoreAnimations();
            case 1:  return SettingsTweaker.restorePerformanceMode();
            case 2:  return SettingsTweaker.restoreRefreshRate();
            case 3:  return GameModeController.restoreStandardMode(targetGame);
            case 4:  return NetworkOptimizer.restoreDefaults();
            case 5:  return DisplayController.restoreDefaults();
            case 6:
                NotificationController.disableDND();
                NotificationController.restoreNotifications();
                return true;
            case 7:  return AudioController.restoreDefaults();
            case 8:  return SyncController.restoreSync();
            case 9:  return FeatureController.restoreFeatures();
            case 10:
                int enabled = AppKiller.reEnableApps();
                step.message = enabled + " app diaktifkan kembali";
                return true;
            case 11:
                SimProtector.HealthReport report = SimProtector.checkHealth();
                step.message = report.message;
                return true;
            default: return false;
        }
    }

    // ═══════════════════════════════════════════════
    // Progress Notification Helpers
    // ═══════════════════════════════════════════════

    private void notifyStepStarted(int index, OptimizationStep step) {
        if (listener != null) {
            mainHandler.post(() -> listener.onStepStarted(index, step));
        }
    }

    private void notifyStepCompleted(int index, OptimizationStep step) {
        if (listener != null) {
            mainHandler.post(() -> listener.onStepCompleted(index, step));
        }
    }

    private void notifyAllCompleted(boolean success, String message) {
        if (listener != null) {
            mainHandler.post(() -> listener.onAllCompleted(success, message));
        }
    }
}
