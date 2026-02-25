package com.modedewa.gamebooster.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.modedewa.gamebooster.R;
import com.modedewa.gamebooster.model.GameInfo;
import com.modedewa.gamebooster.util.GameDetector;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ProfileActivity — Game profiles with per-module toggles and preset selection.
 */
public class ProfileActivity extends AppCompatActivity {

    // Module toggle states
    private final Map<String, Boolean> moduleStates = new LinkedHashMap<>();

    // Module definitions: key -> [emoji, name, description]
    private static final String[][] MODULES = {
            {"animations", "🎭", "Matikan Animasi", "Nonaktifkan animasi sistem"},
            {"gpu_render", "🎨", "Force GPU Rendering", "Paksa GPU untuk rendering"},
            {"bg_limit", "🚫", "Background Limit", "Batasi proses background = 0"},
            {"kill_apps", "🔪", "Kill Background Apps", "Matikan semua app background"},
            {"disable_apps", "📦", "Disable App Non-Gaming", "Nonaktifkan app tidak terpakai"},
            {"game_mode", "🎮", "Game Mode API", "Aktifkan mode performa Android"},
            {"fps_unlock", "🔓", "FPS Unlock", "Hapus batasan FPS dari OEM"},
            {"network", "🌐", "Optimasi Jaringan", "WiFi sleep off, optimasi DNS"},
            {"display", "🖥️", "Optimasi Layar", "Brightness & timeout"},
            {"notif", "🔕", "Block Notifikasi", "Matikan notif yang mengganggu"},
            {"dnd", "🔇", "Mode Jangan Ganggu", "Aktifkan DND saat gaming"},
            {"sync", "🔄", "Matikan Sinkronisasi", "Nonaktifkan auto-sync"},
            {"clean_ram", "🧹", "Bersihkan RAM", "Trim cache & memory"},
    };

    private LinearLayout containerModules;
    private String selectedPreset = "ultra";
    private GameDetector gameDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        gameDetector = new GameDetector(this);
        containerModules = findViewById(R.id.containerModules);

        setupBackButton();
        setupPresetButtons();
        setupGameInfo();
        applyPreset("ultra"); // Default to ultra
        buildModuleToggles();
        setupApplyButton();
    }

    private void setupBackButton() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupGameInfo() {
        List<GameInfo> games = gameDetector.detectInstalledGames();
        if (!games.isEmpty()) {
            GameInfo game = games.get(0);
            TextView gameName = findViewById(R.id.textGameName);
            TextView gamePackage = findViewById(R.id.textGamePackage);
            ImageView gameIcon = findViewById(R.id.imgGameIcon);

            gameName.setText(game.displayName);
            gamePackage.setText(game.packageName);
            if (game.icon != null) {
                gameIcon.setImageDrawable(game.icon);
            }
        }
    }

    private void setupPresetButtons() {
        MaterialButton btnLight = findViewById(R.id.btnPresetLight);
        MaterialButton btnBalanced = findViewById(R.id.btnPresetBalanced);
        MaterialButton btnUltra = findViewById(R.id.btnPresetUltra);

        btnLight.setOnClickListener(v -> {
            applyPreset("light");
            rebuildToggles();
        });
        btnBalanced.setOnClickListener(v -> {
            applyPreset("balanced");
            rebuildToggles();
        });
        btnUltra.setOnClickListener(v -> {
            applyPreset("ultra");
            rebuildToggles();
        });
    }

    private void applyPreset(String preset) {
        selectedPreset = preset;
        moduleStates.clear();

        switch (preset) {
            case "light":
                // Only safe, minimal optimizations
                moduleStates.put("animations", true);
                moduleStates.put("gpu_render", true);
                moduleStates.put("bg_limit", true);
                moduleStates.put("kill_apps", false);  // off
                moduleStates.put("disable_apps", false); // off
                moduleStates.put("game_mode", true);
                moduleStates.put("fps_unlock", false);  // off
                moduleStates.put("network", false);     // off
                moduleStates.put("display", false);     // off
                moduleStates.put("notif", false);       // off
                moduleStates.put("dnd", false);         // off
                moduleStates.put("sync", false);        // off
                moduleStates.put("clean_ram", true);
                break;

            case "balanced":
                // Moderate optimizations
                moduleStates.put("animations", true);
                moduleStates.put("gpu_render", true);
                moduleStates.put("bg_limit", true);
                moduleStates.put("kill_apps", true);
                moduleStates.put("disable_apps", false);  // off
                moduleStates.put("game_mode", true);
                moduleStates.put("fps_unlock", true);
                moduleStates.put("network", true);
                moduleStates.put("display", false);
                moduleStates.put("notif", true);
                moduleStates.put("dnd", true);
                moduleStates.put("sync", true);
                moduleStates.put("clean_ram", true);
                break;

            case "ultra":
            default:
                // Everything on
                for (String[] module : MODULES) {
                    moduleStates.put(module[0], true);
                }
                break;
        }
    }

    private void buildModuleToggles() {
        containerModules.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (String[] module : MODULES) {
            View item = inflater.inflate(R.layout.item_module_toggle, containerModules, false);

            TextView emoji = item.findViewById(R.id.moduleEmoji);
            TextView name = item.findViewById(R.id.moduleName);
            TextView desc = item.findViewById(R.id.moduleDesc);
            MaterialSwitch toggle = item.findViewById(R.id.moduleToggle);

            emoji.setText(module[1]);
            name.setText(module[2]);
            desc.setText(module[3]);

            Boolean state = moduleStates.getOrDefault(module[0], true);
            toggle.setChecked(state != null && state);

            toggle.setOnCheckedChangeListener((button, isChecked) ->
                    moduleStates.put(module[0], isChecked));

            containerModules.addView(item);

            // Add divider
            View divider = new View(this);
            divider.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 1));
            divider.setBackgroundColor(getColor(R.color.card_border));
            containerModules.addView(divider);
        }
    }

    private void rebuildToggles() {
        buildModuleToggles();
    }

    private void setupApplyButton() {
        MaterialButton btnApply = findViewById(R.id.btnApply);
        btnApply.setOnClickListener(v -> {
            // Count enabled modules
            int enabledCount = 0;
            for (Boolean state : moduleStates.values()) {
                if (state != null && state) enabledCount++;
            }
            Toast.makeText(this,
                    "Profil " + selectedPreset.toUpperCase() + " tersimpan! (" +
                    enabledCount + " modul aktif)", Toast.LENGTH_SHORT).show();

            // Save preference and go back
            getSharedPreferences("modedewa", MODE_PRIVATE).edit()
                    .putString("preset", selectedPreset)
                    .apply();

            finish();
        });
    }
}
