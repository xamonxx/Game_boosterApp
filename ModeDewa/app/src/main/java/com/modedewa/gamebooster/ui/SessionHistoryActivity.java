package com.modedewa.gamebooster.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.modedewa.gamebooster.R;
import com.modedewa.gamebooster.model.GameSessionStats;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * SessionHistoryActivity — Layar riwayat semua sesi gaming.
 *
 * Menampilkan:
 * - Ringkasan total (total sesi, total durasi bermain, suhu maks all-time)
 * - List chronological semua sesi gaming (terbaru di atas)
 * - Per sesi: nama game, profil, durasi, suhu maks, FPS avg, baterai terpakai
 * - Tombol hapus riwayat
 */
public class SessionHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerSessions;
    private TextView textTotalSessions, textTotalPlayTime, textAllTimeMaxTemp;
    private TextView textEmptyState;
    private List<GameSessionStats> sessions;
    private SessionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_history);

        initViews();
        loadSessions();
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        textTotalSessions = findViewById(R.id.textTotalSessions);
        textTotalPlayTime = findViewById(R.id.textTotalPlayTime);
        textAllTimeMaxTemp = findViewById(R.id.textAllTimeMaxTemp);
        textEmptyState = findViewById(R.id.textEmptyState);

        recyclerSessions = findViewById(R.id.recyclerSessions);
        recyclerSessions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SessionAdapter();
        recyclerSessions.setAdapter(adapter);

        findViewById(R.id.btnClearHistory).setOnClickListener(v -> {
            GameSessionStats.clearHistory(this);
            loadSessions();
        });
    }

    private void loadSessions() {
        sessions = GameSessionStats.loadHistory(this);
        adapter.notifyDataSetChanged();

        if (sessions.isEmpty()) {
            textEmptyState.setVisibility(View.VISIBLE);
            recyclerSessions.setVisibility(View.GONE);
        } else {
            textEmptyState.setVisibility(View.GONE);
            recyclerSessions.setVisibility(View.VISIBLE);
        }

        // Hitung ringkasan
        textTotalSessions.setText(String.valueOf(sessions.size()));

        long totalDurationMs = 0;
        float maxTempAllTime = 0;
        for (GameSessionStats s : sessions) {
            totalDurationMs += s.durationMs;
            if (s.maxTemp > maxTempAllTime) maxTempAllTime = s.maxTemp;
        }

        // Format total durasi
        long totalHours = totalDurationMs / 3600000;
        long totalMins = (totalDurationMs % 3600000) / 60000;
        textTotalPlayTime.setText(totalHours + "j " + totalMins + "m");
        textAllTimeMaxTemp.setText(maxTempAllTime > 0 ?
                String.format("%.0f°C", maxTempAllTime) : "--");
    }

    // ============================================================
    // ADAPTER
    // ============================================================

    private class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.VH> {

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_session_history, parent, false);
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            holder.bind(sessions.get(position), position);
        }

        @Override
        public int getItemCount() {
            return sessions != null ? sessions.size() : 0;
        }

        class VH extends RecyclerView.ViewHolder {
            TextView textGameName, textDate, textDuration, textProfile;
            TextView textTemp, textFps, textBattery;
            View profileIndicator;

            VH(View v) {
                super(v);
                textGameName = v.findViewById(R.id.textSessionGameName);
                textDate = v.findViewById(R.id.textSessionDate);
                textDuration = v.findViewById(R.id.textSessionDuration);
                textProfile = v.findViewById(R.id.textSessionProfile);
                textTemp = v.findViewById(R.id.textSessionTemp);
                textFps = v.findViewById(R.id.textSessionFps);
                textBattery = v.findViewById(R.id.textSessionBattery);
                profileIndicator = v.findViewById(R.id.viewProfileIndicator);
            }

            void bind(GameSessionStats session, int position) {
                textGameName.setText(session.gameName != null ? session.gameName : "Unknown");

                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault());
                textDate.setText(sdf.format(new Date(session.startTime)));

                textDuration.setText(session.getFormattedDuration());

                String profile = session.profileUsed != null ?
                        session.profileUsed.toUpperCase() : "BALANCED";
                textProfile.setText(profile);

                // Warna profil
                int profileColor;
                switch (profile) {
                    case "ULTRA":
                        profileColor = getColor(R.color.status_danger);
                        break;
                    case "LIGHT":
                        profileColor = getColor(R.color.neon_green);
                        break;
                    default:
                        profileColor = getColor(R.color.neon_orange);
                        break;
                }
                textProfile.setTextColor(profileColor);
                profileIndicator.setBackgroundColor(profileColor);

                // Stats
                textTemp.setText(session.maxTemp > 0 ?
                        String.format("%.0f°C", session.maxTemp) : "--");
                textFps.setText(session.avgFps > 0 ?
                        session.avgFps + " FPS" : "--");
                textBattery.setText(session.getBatteryUsed() > 0 ?
                        "-" + session.getBatteryUsed() + "%" : "--");

                // Warna suhu
                if (session.maxTemp >= 70) {
                    textTemp.setTextColor(getColor(R.color.status_danger));
                } else if (session.maxTemp >= 55) {
                    textTemp.setTextColor(getColor(R.color.neon_orange));
                } else {
                    textTemp.setTextColor(getColor(R.color.neon_green));
                }
            }
        }
    }
}
