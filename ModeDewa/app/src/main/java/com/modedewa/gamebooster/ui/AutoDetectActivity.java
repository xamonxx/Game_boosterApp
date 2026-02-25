package com.modedewa.gamebooster.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.materialswitch.MaterialSwitch;

import com.modedewa.gamebooster.R;
import com.modedewa.gamebooster.model.AppDetectionConfig;
import com.modedewa.gamebooster.model.GameInfo;
import com.modedewa.gamebooster.model.GameSessionStats;
import com.modedewa.gamebooster.service.GameDetectionService;
import com.modedewa.gamebooster.util.DeviceDetector;
import com.modedewa.gamebooster.util.GameDetector;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * AutoDetectActivity — HUD Controller untuk fitur Auto-Detect Game.
 *
 * Menampilkan:
 * - Master toggle ON/OFF auto-detect engine
 * - Daftar app terdeteksi dengan per-app toggle
 * - Smart Profile mapping per game
 * - Konfigurasi (interval scan, battery-aware, notif controller, auto-disable)
 * - Session stats dari sesi gaming terakhir
 * - Detection log real-time
 * - Quick actions: Scan Ulang, Boost Now, Cool Down, Reset
 */
public class AutoDetectActivity extends AppCompatActivity {

    // === UI References ===
    private MaterialSwitch switchMasterToggle;
    private TextView textDetectedCount, textActiveCount, textInactiveCount;
    private RecyclerView recyclerDetectedApps;
    private MaterialSwitch switchSmartProfile;
    private SeekBar seekBarInterval;
    private TextView textIntervalValue;
    private MaterialSwitch switchBatteryAware;
    private MaterialSwitch switchNotifController;
    private MaterialSwitch switchAutoDisable;
    private MaterialSwitch switchAutoReEnable;
    private TextView textSessionDuration, textSessionGame, textSessionDate;
    private TextView textMaxTemp, textAvgFps, textRamUsed, textBatteryUsed;
    private ViewGroup containerLog;
    private TextView textStatusIndicator;

    // === Data ===
    private AppDetectionConfig config;
    private GameDetector gameDetector;
    private DeviceDetector deviceDetector;
    private List<GameInfo> detectedGames = new ArrayList<>();
    private DetectedAppsAdapter adapter;
    private List<String> logEntries = new ArrayList<>();
    private Handler handler = new Handler(Looper.getMainLooper());

    // === Broadcast Receiver ===
    private BroadcastReceiver logReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_detect);

        config = AppDetectionConfig.load(this);
        gameDetector = new GameDetector(this);
        deviceDetector = new DeviceDetector(this);

        initViews();
        loadDetectedGames();
        loadConfig();
        loadLastSession();
        registerLogReceiver();
    }

    @Override
    protected void onDestroy() {
        if (logReceiver != null) {
            unregisterReceiver(logReceiver);
        }
        super.onDestroy();
    }

    // ============================================================
    // INIT VIEWS
    // ============================================================

    private void initViews() {
        // Header back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Status indicator
        textStatusIndicator = findViewById(R.id.textStatusIndicator);

        // === Section 1: Master Toggle ===
        switchMasterToggle = findViewById(R.id.switchMasterToggle);
        textDetectedCount = findViewById(R.id.textDetectedCount);
        textActiveCount = findViewById(R.id.textActiveCount);
        textInactiveCount = findViewById(R.id.textInactiveCount);

        switchMasterToggle.setOnCheckedChangeListener((btn, isChecked) -> {
            config.isEnabled = isChecked;
            config.save(this);
            updateStatusIndicator(isChecked);
            if (isChecked) {
                startDetectionService();
            } else {
                stopDetectionService();
            }
        });

        // === Section 2: Detected Apps ===
        recyclerDetectedApps = findViewById(R.id.recyclerDetectedApps);
        recyclerDetectedApps.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DetectedAppsAdapter();
        recyclerDetectedApps.setAdapter(adapter);

        // Tombol tambah custom app
        findViewById(R.id.btnAddCustomApp).setOnClickListener(v -> showAddCustomAppDialog());

        // === Section 3: Smart Profile ===
        switchSmartProfile = findViewById(R.id.switchSmartProfile);
        switchSmartProfile.setOnCheckedChangeListener((btn, isChecked) -> {
            config.smartProfileEnabled = isChecked;
            config.save(this);
        });

        // === Section 4: Konfigurasi ===
        seekBarInterval = findViewById(R.id.seekBarInterval);
        textIntervalValue = findViewById(R.id.textIntervalValue);
        switchBatteryAware = findViewById(R.id.switchBatteryAware);
        switchNotifController = findViewById(R.id.switchNotifController);
        switchAutoDisable = findViewById(R.id.switchAutoDisable);
        switchAutoReEnable = findViewById(R.id.switchAutoReEnable);

        seekBarInterval.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int seconds = progress + 1; // range 1-10
                textIntervalValue.setText(seconds + " detik");
                if (fromUser) {
                    config.scanIntervalSec = seconds;
                    config.save(AutoDetectActivity.this);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        switchBatteryAware.setOnCheckedChangeListener((btn, isChecked) -> {
            config.batteryAwareEnabled = isChecked;
            config.save(this);
        });
        switchNotifController.setOnCheckedChangeListener((btn, isChecked) -> {
            config.notificationControllerEnabled = isChecked;
            config.save(this);
        });
        switchAutoDisable.setOnCheckedChangeListener((btn, isChecked) -> {
            config.autoDisableBloatware = isChecked;
            config.save(this);
        });
        switchAutoReEnable.setOnCheckedChangeListener((btn, isChecked) -> {
            config.autoReEnableOnExit = isChecked;
            config.save(this);
        });

        // === Section 5: Session Stats (loaded separately) ===
        textSessionDuration = findViewById(R.id.textSessionDuration);
        textSessionGame = findViewById(R.id.textSessionGame);
        textSessionDate = findViewById(R.id.textSessionDate);
        textMaxTemp = findViewById(R.id.textMaxTemp);
        textAvgFps = findViewById(R.id.textAvgFps);
        textRamUsed = findViewById(R.id.textRamUsed);
        textBatteryUsed = findViewById(R.id.textBatteryUsed);

        // === Section 6: Log container ===
        containerLog = findViewById(R.id.containerLog);
        findViewById(R.id.btnClearLog).setOnClickListener(v -> {
            logEntries.clear();
            containerLog.removeAllViews();
        });

        // === Section 7: Quick Actions ===
        findViewById(R.id.btnScanUlang).setOnClickListener(v -> {
            sendServiceAction(GameDetectionService.ACTION_FORCE_SCAN);
            loadDetectedGames();
        });
        findViewById(R.id.btnBoostNow).setOnClickListener(v ->
                sendServiceAction(GameDetectionService.ACTION_FORCE_BOOST));
        findViewById(R.id.btnCoolDown).setOnClickListener(v ->
                sendServiceAction(GameDetectionService.ACTION_COOL_DOWN));
        findViewById(R.id.btnReset).setOnClickListener(v -> {
            config = new AppDetectionConfig();
            config.save(this);
            loadConfig();
            stopDetectionService();
        });
    }

    // ============================================================
    // LOAD DATA
    // ============================================================

    private void loadDetectedGames() {
        detectedGames.clear();
        // Dari database bawaan
        detectedGames.addAll(gameDetector.detectInstalledGames());
        // Dari custom packages
        String[] customPkgs = config.getCustomPackageArray();
        for (String pkg : customPkgs) {
            if (!pkg.trim().isEmpty()) {
                boolean exists = false;
                for (GameInfo g : detectedGames) {
                    if (g.packageName.equals(pkg.trim())) { exists = true; break; }
                }
                if (!exists) {
                    detectedGames.add(GameInfo.createCustom(pkg.trim(), pkg.trim()));
                }
            }
        }
        adapter.notifyDataSetChanged();
        updateCounts();
    }

    private void loadConfig() {
        config = AppDetectionConfig.load(this);
        switchMasterToggle.setChecked(config.isEnabled);
        switchSmartProfile.setChecked(config.smartProfileEnabled);
        seekBarInterval.setProgress(config.scanIntervalSec - 1);
        textIntervalValue.setText(config.scanIntervalSec + " detik");
        switchBatteryAware.setChecked(config.batteryAwareEnabled);
        switchNotifController.setChecked(config.notificationControllerEnabled);
        switchAutoDisable.setChecked(config.autoDisableBloatware);
        switchAutoReEnable.setChecked(config.autoReEnableOnExit);
        updateStatusIndicator(config.isEnabled);
    }

    private void loadLastSession() {
        GameSessionStats last = GameSessionStats.getLastSession(this);
        if (last != null) {
            textSessionDuration.setText(last.getFormattedDuration());
            textSessionGame.setText(last.gameName);
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            textSessionDate.setText(sdf.format(new Date(last.startTime)));
            textMaxTemp.setText(String.format("%.0f°C", last.maxTemp));
            textAvgFps.setText(last.avgFps > 0 ? last.avgFps + " FPS" : "N/A");
            textRamUsed.setText(last.avgRamUsedMb > 0 ?
                    String.format("%.1f GB", last.avgRamUsedMb / 1024.0) : "N/A");
            textBatteryUsed.setText(last.getBatteryUsed() + "%");
        } else {
            textSessionDuration.setText("--:--:--");
            textSessionGame.setText("Belum ada sesi");
            textSessionDate.setText("-");
            textMaxTemp.setText("--");
            textAvgFps.setText("--");
            textRamUsed.setText("--");
            textBatteryUsed.setText("--");
        }
    }

    // ============================================================
    // UI UPDATES
    // ============================================================

    private void updateCounts() {
        int total = detectedGames.size();
        int active = 0, inactive = 0;
        for (GameInfo g : detectedGames) {
            if (g.isAutoDetectEnabled) active++; else inactive++;
        }
        textDetectedCount.setText(String.valueOf(total));
        textActiveCount.setText(String.valueOf(active));
        textInactiveCount.setText(String.valueOf(inactive));
    }

    private void updateStatusIndicator(boolean isActive) {
        if (isActive) {
            textStatusIndicator.setText("AKTIF");
            textStatusIndicator.setTextColor(getColor(R.color.neon_green));
        } else {
            textStatusIndicator.setText("MATI");
            textStatusIndicator.setTextColor(getColor(R.color.status_inactive));
        }
    }

    private void addLogEntry(String message, String level) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String time = sdf.format(new Date());
        String fullMsg = time + "  " + message;
        logEntries.add(0, fullMsg);

        // Keep max 50 entries
        if (logEntries.size() > 50) logEntries.remove(logEntries.size() - 1);

        // Add to UI
        TextView tv = new TextView(this);
        tv.setText(fullMsg);
        tv.setTextSize(10);
        tv.setFontFeatureSettings("tnum");
        tv.setPadding(0, 4, 0, 4);

        int color;
        switch (level) {
            case "success": color = getColor(R.color.neon_green); break;
            case "warn":    color = getColor(R.color.neon_orange); break;
            case "error":   color = getColor(R.color.status_danger); break;
            case "info":    color = getColor(R.color.neon_cyan); break;
            default:        color = getColor(R.color.text_tertiary); break;
        }
        tv.setTextColor(color);

        containerLog.addView(tv, 0);
        // Keep max 20 visible
        if (containerLog.getChildCount() > 20) {
            containerLog.removeViewAt(containerLog.getChildCount() - 1);
        }
    }

    // ============================================================
    // SERVICE CONTROL
    // ============================================================

    private void startDetectionService() {
        Intent intent = new Intent(this, GameDetectionService.class);
        intent.setAction(GameDetectionService.ACTION_START_DETECTION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    private void stopDetectionService() {
        Intent intent = new Intent(this, GameDetectionService.class);
        intent.setAction(GameDetectionService.ACTION_STOP_DETECTION);
        startService(intent);
    }

    private void sendServiceAction(String action) {
        Intent intent = new Intent(this, GameDetectionService.class);
        intent.setAction(action);
        startService(intent);
    }

    // ============================================================
    // BROADCAST RECEIVER (Log dari Service)
    // ============================================================

    private void registerLogReceiver() {
        logReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (GameDetectionService.ACTION_LOG.equals(intent.getAction())) {
                    String msg = intent.getStringExtra(GameDetectionService.EXTRA_LOG_MSG);
                    String level = intent.getStringExtra(GameDetectionService.EXTRA_LOG_LEVEL);
                    if (msg != null) {
                        handler.post(() -> addLogEntry(msg, level != null ? level : "info"));
                    }
                } else if (GameDetectionService.ACTION_GAME_DETECTED.equals(intent.getAction())) {
                    handler.post(() -> loadLastSession());
                } else if (GameDetectionService.ACTION_GAME_CLOSED.equals(intent.getAction())) {
                    handler.post(() -> loadLastSession());
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(GameDetectionService.ACTION_LOG);
        filter.addAction(GameDetectionService.ACTION_GAME_DETECTED);
        filter.addAction(GameDetectionService.ACTION_GAME_CLOSED);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(logReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(logReceiver, filter);
        }
    }

    // ============================================================
    // DIALOGS
    // ============================================================

    private void showAddCustomAppDialog() {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("com.example.game");
        input.setTextColor(getColor(R.color.text_primary));
        input.setHintTextColor(getColor(R.color.text_tertiary));
        input.setPadding(48, 32, 48, 32);

        new AlertDialog.Builder(this, R.style.Theme_ModeDewa)
                .setTitle("Tambah Aplikasi Custom")
                .setMessage("Masukkan package name aplikasi:")
                .setView(input)
                .setPositiveButton("Tambah", (dialog, which) -> {
                    String pkg = input.getText().toString().trim();
                    if (!pkg.isEmpty()) {
                        config.addCustomPackage(pkg);
                        config.save(this);
                        loadDetectedGames();
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void showProfileDialog(GameInfo game) {
        String[] profiles = {"LIGHT", "BALANCED", "ULTRA"};
        String[] values = {"light", "balanced", "ultra"};
        int selected = 1;
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(game.selectedProfile)) { selected = i; break; }
        }

        new AlertDialog.Builder(this, R.style.Theme_ModeDewa)
                .setTitle("Profil untuk " + game.displayName)
                .setSingleChoiceItems(profiles, selected, (dialog, which) -> {
                    game.selectedProfile = values[which];
                    adapter.notifyDataSetChanged();
                    dialog.dismiss();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    // ============================================================
    // RECYCLER VIEW ADAPTER
    // ============================================================

    private class DetectedAppsAdapter extends RecyclerView.Adapter<DetectedAppsAdapter.VH> {

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_detected_app, parent, false);
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            GameInfo game = detectedGames.get(position);
            holder.bind(game);
        }

        @Override
        public int getItemCount() {
            return detectedGames.size();
        }

        class VH extends RecyclerView.ViewHolder {
            ImageView imgIcon;
            TextView textName, textStatusLabel, textProfile;
            MaterialSwitch switchToggle;
            View indicator;

            VH(View v) {
                super(v);
                imgIcon = v.findViewById(R.id.imgAppIcon);
                textName = v.findViewById(R.id.textAppName);
                textStatusLabel = v.findViewById(R.id.textStatusLabel);
                textProfile = v.findViewById(R.id.textProfileLabel);
                switchToggle = v.findViewById(R.id.switchAppToggle);
                indicator = v.findViewById(R.id.viewStatusIndicator);
            }

            void bind(GameInfo game) {
                textName.setText(game.displayName);
                textStatusLabel.setText(game.getStatusLabel());
                textProfile.setText("Profil: " + game.getProfileLabel());

                if (game.icon != null) {
                    imgIcon.setImageDrawable(game.icon);
                }

                // Status indicator color
                if (game.isAutoDetectEnabled) {
                    indicator.setBackgroundResource(R.drawable.circle_indicator_active);
                    textName.setAlpha(1f);
                } else {
                    indicator.setBackgroundResource(R.drawable.circle_indicator_inactive);
                    textName.setAlpha(0.4f);
                }

                // Status label color
                switch (game.getStatusLabel()) {
                    case "AUTO":
                        textStatusLabel.setTextColor(getColor(R.color.neon_green));
                        textStatusLabel.setBackgroundResource(R.drawable.bg_label_auto);
                        break;
                    case "MANUAL":
                        textStatusLabel.setTextColor(getColor(R.color.neon_cyan));
                        textStatusLabel.setBackgroundResource(R.drawable.bg_label_manual);
                        break;
                    default:
                        textStatusLabel.setTextColor(getColor(R.color.text_tertiary));
                        textStatusLabel.setBackgroundResource(R.drawable.bg_label_off);
                        break;
                }

                switchToggle.setChecked(game.isAutoDetectEnabled);
                switchToggle.setOnCheckedChangeListener((btn, isChecked) -> {
                    game.isAutoDetectEnabled = isChecked;
                    notifyItemChanged(getAdapterPosition());
                    updateCounts();
                });

                // Tap on item to change profile
                itemView.setOnClickListener(v -> showProfileDialog(game));
            }
        }
    }
}
