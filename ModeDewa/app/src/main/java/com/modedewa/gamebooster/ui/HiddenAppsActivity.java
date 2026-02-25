package com.modedewa.gamebooster.ui;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.modedewa.gamebooster.R;
import com.modedewa.gamebooster.util.HiddenAppsManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HiddenAppsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private SearchView searchView;
    private AppListAdapter adapter;

    private HiddenAppsManager hiddenAppsManager;
    private final List<AppItem> allApps = new ArrayList<>();
    private final List<AppItem> filteredApps = new ArrayList<>();
    
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hidden_apps);

        hiddenAppsManager = new HiddenAppsManager(this);

        Toolbar toolbar = findViewById(R.id.toolbarHiddenApps);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerHiddenApps);
        progressBar = findViewById(R.id.progressLoadingApps);
        searchView = findViewById(R.id.searchViewApps);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppListAdapter();
        recyclerView.setAdapter(adapter);

        setupSearchView();
        loadApps();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterApps(newText);
                return true;
            }
        });
    }

    private void loadApps() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        executor.execute(() -> {
            PackageManager pm = getPackageManager();
            List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
            List<AppItem> tempApps = new ArrayList<>();
            Set<String> hiddenSet = hiddenAppsManager.getHiddenApps();

            for (ApplicationInfo packageInfo : packages) {
                // Skip system wrapper and our own app
                if (packageInfo.packageName.equals(getPackageName())) continue;

                // Only include apps that have launch intent (user-facing apps)
                // EXCEPT if it is already hidden (user checked it before)
                boolean isSystem = (packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
                boolean isHidden = hiddenSet.contains(packageInfo.packageName);
                
                if (pm.getLaunchIntentForPackage(packageInfo.packageName) != null || isHidden || !isSystem) {
                    AppItem item = new AppItem();
                    item.packageName = packageInfo.packageName;
                    item.appName = packageInfo.loadLabel(pm).toString();
                    try {
                        item.icon = packageInfo.loadIcon(pm);
                    } catch (Exception e) {
                        item.icon = null;
                    }
                    item.isHidden = isHidden;
                    tempApps.add(item);
                }
            }

            // Sort alphabetically by name
            Collections.sort(tempApps, (a, b) -> a.appName.compareToIgnoreCase(b.appName));

            mainHandler.post(() -> {
                allApps.clear();
                allApps.addAll(tempApps);
                filterApps(searchView.getQuery().toString());
                
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            });
        });
    }

    private void filterApps(String query) {
        filteredApps.clear();
        if (query.isEmpty()) {
            filteredApps.addAll(allApps);
        } else {
            String lowerQuery = query.toLowerCase();
            for (AppItem app : allApps) {
                if (app.appName.toLowerCase().contains(lowerQuery) ||
                    app.packageName.toLowerCase().contains(lowerQuery)) {
                    filteredApps.add(app);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    // --- Model Class ---
    private static class AppItem {
        String appName;
        String packageName;
        Drawable icon;
        boolean isHidden;
    }

    // --- Recycler Adapter ---
    private class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_hidden_app, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AppItem item = filteredApps.get(position);
            holder.bind(item);
        }

        @Override
        public int getItemCount() {
            return filteredApps.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imgIcon;
            TextView textName;
            TextView textPackage;
            CheckBox checkBox;

            ViewHolder(View view) {
                super(view);
                imgIcon = view.findViewById(R.id.imgAppIcon);
                textName = view.findViewById(R.id.textAppName);
                textPackage = view.findViewById(R.id.textPackageName);
                checkBox = view.findViewById(R.id.checkHiddenApp);

                // Setup click on entire row to toggle checkbox
                view.setOnClickListener(v -> {
                    checkBox.setChecked(!checkBox.isChecked());
                });

                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        AppItem app = filteredApps.get(pos);
                        app.isHidden = isChecked;
                        
                        // Update SharedPreferences immediately
                        Set<String> hiddenSet = hiddenAppsManager.getHiddenApps();
                        if (isChecked) {
                            hiddenSet.add(app.packageName);
                        } else {
                            hiddenSet.remove(app.packageName);
                        }
                        hiddenAppsManager.setHiddenApps(hiddenSet);
                    }
                });
            }

            void bind(AppItem item) {
                textName.setText(item.appName);
                textPackage.setText(item.packageName);
                
                // Remove listener before setting state to avoid recursive loops
                checkBox.setOnCheckedChangeListener(null);
                checkBox.setChecked(item.isHidden);
                
                // Re-add listener
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        AppItem app = filteredApps.get(pos);
                        app.isHidden = isChecked;
                        Set<String> hiddenSet = hiddenAppsManager.getHiddenApps();
                        if (isChecked) {
                            hiddenSet.add(app.packageName);
                        } else {
                            hiddenSet.remove(app.packageName);
                        }
                        hiddenAppsManager.setHiddenApps(hiddenSet);
                    }
                });

                if (item.icon != null) {
                    imgIcon.setImageDrawable(item.icon);
                } else {
                    imgIcon.setImageResource(android.R.mipmap.sym_def_app_icon);
                }
            }
        }
    }
}
