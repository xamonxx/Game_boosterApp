package com.modedewa.gamebooster.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.modedewa.gamebooster.R;
import com.modedewa.gamebooster.util.ShizukuShell;

import rikka.shizuku.Shizuku;

/**
 * SplashActivity — Entry point. Checks Shizuku availability and permissions.
 */
public class SplashActivity extends AppCompatActivity {

    private static final int SHIZUKU_PERMISSION_REQUEST = 100;

    private TextView statusText;
    private ProgressBar progressBar;
    private LinearLayout shizukuWarning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        statusText = findViewById(R.id.splashStatus);
        progressBar = findViewById(R.id.splashProgress);
        shizukuWarning = findViewById(R.id.shizukuWarning);

        // Continue anyway button
        findViewById(R.id.btnContinueAnyway).setOnClickListener(v -> goToHome());

        // Start checking after a short delay for visual effect
        new Handler().postDelayed(this::checkShizuku, 1500);
    }

    private void checkShizuku() {
        statusText.setText("Memeriksa Shizuku...");

        if (ShizukuShell.isAvailable()) {
            // Shizuku is running
            if (ShizukuShell.hasPermission()) {
                // All good, proceed
                statusText.setText(getString(R.string.splash_shizuku_ok));
                statusText.setTextColor(getColor(R.color.status_active));
                new Handler().postDelayed(this::goToHome, 1000);
            } else {
                // Need permission
                statusText.setText(getString(R.string.splash_permission_needed));
                statusText.setTextColor(getColor(R.color.status_warning));
                ShizukuShell.requestPermission(SHIZUKU_PERMISSION_REQUEST);

                // Add permission listener
                Shizuku.addRequestPermissionResultListener(this::onPermissionResult);
            }
        } else {
            // Shizuku not available
            statusText.setText(getString(R.string.splash_shizuku_fail));
            statusText.setTextColor(getColor(R.color.status_danger));
            progressBar.setVisibility(View.GONE);
            shizukuWarning.setVisibility(View.VISIBLE);
        }
    }

    private void onPermissionResult(int requestCode, int grantResult) {
        if (requestCode == SHIZUKU_PERMISSION_REQUEST) {
            if (ShizukuShell.hasPermission()) {
                runOnUiThread(() -> {
                    statusText.setText(getString(R.string.splash_shizuku_ok));
                    statusText.setTextColor(getColor(R.color.status_active));
                    new Handler().postDelayed(this::goToHome, 800);
                });
            } else {
                runOnUiThread(() -> {
                    statusText.setText("Izin Shizuku ditolak");
                    statusText.setTextColor(getColor(R.color.status_danger));
                    progressBar.setVisibility(View.GONE);
                    shizukuWarning.setVisibility(View.VISIBLE);
                });
            }
        }
    }

    private void goToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            Shizuku.removeRequestPermissionResultListener(this::onPermissionResult);
        } catch (Exception ignored) {}
    }
}
