package com.modedewa.gamebooster.ui;

import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.modedewa.gamebooster.R;

/**
 * SettingsActivity — App settings: temperature limit, SIM protection, auto-detect.
 */
public class SettingsActivity extends AppCompatActivity {

    private static final int TEMP_MIN = 35; // 35°C
    private static final int TEMP_MAX = 55; // 55°C

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setupBackButton();
        setupTempSlider();
        setupToggles();
    }

    private void setupBackButton() {
        findViewById(R.id.btnBackSettings).setOnClickListener(v -> finish());
    }

    private void setupTempSlider() {
        SeekBar seekTemp = findViewById(R.id.seekTempLimit);
        TextView textTemp = findViewById(R.id.textTempLimit);

        // Load saved value
        int savedTemp = getSharedPreferences("modedewa", MODE_PRIVATE)
                .getInt("temp_limit", 45);
        seekTemp.setProgress(savedTemp - TEMP_MIN);
        textTemp.setText(savedTemp + "°C");

        seekTemp.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int temp = TEMP_MIN + progress;
                textTemp.setText(temp + "°C");

                // Change color based on danger level
                if (temp <= 40) {
                    textTemp.setTextColor(getColor(R.color.temp_cool));
                } else if (temp <= 45) {
                    textTemp.setTextColor(getColor(R.color.temp_warm));
                } else if (temp <= 50) {
                    textTemp.setTextColor(getColor(R.color.temp_hot));
                } else {
                    textTemp.setTextColor(getColor(R.color.temp_critical));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int temp = TEMP_MIN + seekBar.getProgress();
                getSharedPreferences("modedewa", MODE_PRIVATE).edit()
                        .putInt("temp_limit", temp).apply();
            }
        });
    }

    private void setupToggles() {
        MaterialSwitch switchAutoDetect = findViewById(R.id.switchAutoDetect);

        // Load saved value
        boolean autoDetect = getSharedPreferences("modedewa", MODE_PRIVATE)
                .getBoolean("auto_detect", true);
        switchAutoDetect.setChecked(autoDetect);

        switchAutoDetect.setOnCheckedChangeListener((button, isChecked) -> {
            getSharedPreferences("modedewa", MODE_PRIVATE).edit()
                    .putBoolean("auto_detect", isChecked).apply();
        });
    }
}
