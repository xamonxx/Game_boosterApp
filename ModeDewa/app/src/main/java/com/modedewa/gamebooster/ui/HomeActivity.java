package com.modedewa.gamebooster.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.google.android.material.card.MaterialCardView;
import com.modedewa.gamebooster.R;
import com.modedewa.gamebooster.core.GameModeController;
import com.modedewa.gamebooster.core.GameModeExecutor;
import com.modedewa.gamebooster.model.GameInfo;
import com.modedewa.gamebooster.model.OptimizationStep;
import com.modedewa.gamebooster.model.SystemInfo;
import com.modedewa.gamebooster.util.DeviceDetector;
import com.modedewa.gamebooster.util.GameDetector;
import com.modedewa.gamebooster.util.ShizukuShell;

import java.util.List;

/**
 * HomeActivity — Main dashboard with big toggle, status cards, and system info.
 */
public class HomeActivity extends AppCompatActivity {

    private boolean isGameModeActive = false;
    private String targetGamePackage = "com.mobile.legends"; // Default

    // UI Elements
    private FrameLayout toggleFrame;
    private TextView toggleIcon;
    private TextView textModeStatus;
    private TextView textModeSub;
    private MaterialCardView cardProgress;
    private ProgressBar progressBar;
    private TextView textProgressTitle;
    private TextView textProgressStep;
    private TextView valueCpu, valueRam, valueTemp, valueBattery;
    private TextView labelCpu, labelRam;
    private TextView textDeviceInfo;

    private DeviceDetector deviceDetector;
    private GameDetector gameDetector;
    private Handler refreshHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        deviceDetector = new DeviceDetector(this);
        gameDetector = new GameDetector(this);

        initViews();
        setupClickListeners();
        loadSystemInfo();
        detectGame();
    }

    private void initViews() {
        toggleFrame = findViewById(R.id.toggleFrame);
        toggleIcon = findViewById(R.id.toggleIcon);
        textModeStatus = findViewById(R.id.textModeStatus);
        textModeSub = findViewById(R.id.textModeSub);
        cardProgress = findViewById(R.id.cardProgress);
        progressBar = findViewById(R.id.progressBar);
        textProgressTitle = findViewById(R.id.textProgressTitle);
        textProgressStep = findViewById(R.id.textProgressStep);
        valueCpu = findViewById(R.id.valueCpu);
        valueRam = findViewById(R.id.valueRam);
        valueTemp = findViewById(R.id.valueTemp);
        valueBattery = findViewById(R.id.valueBattery);
        labelCpu = findViewById(R.id.labelCpu);
        labelRam = findViewById(R.id.labelRam);
        textDeviceInfo = findViewById(R.id.textDeviceInfo);
    }

    private void setupClickListeners() {
        // Big Toggle
        MaterialCardView cardToggle = findViewById(R.id.cardToggle);
        cardToggle.setOnClickListener(v -> toggleGameMode());

        // Settings button
        findViewById(R.id.btnSettings).setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));

        // Bottom buttons
        findViewById(R.id.btnProfiles).setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        findViewById(R.id.btnSettingsBottom).setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));
    }

    private void loadSystemInfo() {
        new Thread(() -> {
            SystemInfo info = deviceDetector.getSystemInfo();
            runOnUiThread(() -> {
                valueCpu.setText(String.valueOf(deviceDetector.getCpuCoreCount()));
                labelCpu.setText(info.chipset);
                valueRam.setText(info.availableRam + " MB");
                labelRam.setText("/ " + info.totalRam + " MB");
                valueTemp.setText(info.getTempDisplay());
                valueBattery.setText(info.batteryPercent + "%");
                textDeviceInfo.setText(
                        info.manufacturer + " " + info.deviceModel + "\n" +
                        "Android " + info.androidVersion + " (API " + info.sdkLevel + ")\n" +
                        "Chipset: " + info.chipset + "\n" +
                        "GPU: " + deviceDetector.getGpuName() + "\n" +
                        "Shizuku: " + (ShizukuShell.isAvailable() ? "✓ Aktif" : "✗ Tidak aktif"));
            });
        }).start();
    }

    private void detectGame() {
        new Thread(() -> {
            List<GameInfo> games = gameDetector.detectInstalledGames();
            if (!games.isEmpty()) {
                targetGamePackage = games.get(0).packageName;
            }
        }).start();
    }

    // ═══════════════════════════════════════════════
    // TOGGLE GAME MODE
    // ═══════════════════════════════════════════════

    private void toggleGameMode() {
        if (isGameModeActive) {
            deactivateMode();
        } else {
            activateMode();
        }
    }

    private void activateMode() {
        // Show progress
        cardProgress.setVisibility(View.VISIBLE);
        textProgressTitle.setText(R.string.progress_title);
        progressBar.setProgress(0);

        GameModeExecutor executor = new GameModeExecutor(this, targetGamePackage);
        executor.setProgressListener(new GameModeExecutor.ProgressListener() {
            @Override
            public void onStepStarted(int stepIndex, OptimizationStep step) {
                textProgressStep.setText(step.emoji + " " + step.name + "...");
                progressBar.setProgress((stepIndex * 100) / 15);
            }

            @Override
            public void onStepCompleted(int stepIndex, OptimizationStep step) {
                progressBar.setProgress(((stepIndex + 1) * 100) / 15);
            }

            @Override
            public void onAllCompleted(boolean success, String message) {
                isGameModeActive = true;
                updateToggleUI(true);
                cardProgress.setVisibility(View.GONE);
                Toast.makeText(HomeActivity.this, message, Toast.LENGTH_LONG).show();
                // Refresh system info
                loadSystemInfo();
                // Ask to launch game
                launchGamePrompt();
            }
        });

        executor.activateGameMode();
    }

    private void deactivateMode() {
        cardProgress.setVisibility(View.VISIBLE);
        textProgressTitle.setText(R.string.progress_restore_title);
        progressBar.setProgress(0);

        GameModeExecutor executor = new GameModeExecutor(this, targetGamePackage);
        executor.setProgressListener(new GameModeExecutor.ProgressListener() {
            @Override
            public void onStepStarted(int stepIndex, OptimizationStep step) {
                textProgressStep.setText(step.emoji + " " + step.name + "...");
                progressBar.setProgress((stepIndex * 100) / 12);
            }

            @Override
            public void onStepCompleted(int stepIndex, OptimizationStep step) {
                progressBar.setProgress(((stepIndex + 1) * 100) / 12);
            }

            @Override
            public void onAllCompleted(boolean success, String message) {
                isGameModeActive = false;
                updateToggleUI(false);
                cardProgress.setVisibility(View.GONE);
                Toast.makeText(HomeActivity.this, message, Toast.LENGTH_LONG).show();
                loadSystemInfo();
            }
        });

        executor.deactivateGameMode();
    }

    private void updateToggleUI(boolean active) {
        if (active) {
            toggleFrame.setBackgroundResource(R.drawable.circle_toggle_on);
            toggleIcon.setText("🔥");
            textModeStatus.setText(R.string.mode_aktif);
            textModeStatus.setTextColor(getColor(R.color.neon_cyan));
            textModeSub.setText(R.string.tap_to_deactivate);
        } else {
            toggleFrame.setBackgroundResource(R.drawable.circle_toggle_off);
            toggleIcon.setText("⚡");
            textModeStatus.setText(R.string.mode_nonaktif);
            textModeStatus.setTextColor(getColor(R.color.status_inactive));
            textModeSub.setText(R.string.tap_to_activate);
        }
    }

    private void launchGamePrompt() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("🎮 Game Siap!")
                .setMessage("Mode Dewa aktif. Buka game sekarang?")
                .setPositiveButton("Buka Game", (d, w) -> {
                    GameModeController.launchGame(targetGamePackage);
                })
                .setNegativeButton("Nanti", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSystemInfo();
    }
}
