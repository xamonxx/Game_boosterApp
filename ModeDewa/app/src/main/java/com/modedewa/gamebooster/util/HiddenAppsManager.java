package com.modedewa.gamebooster.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/**
 * Manager class to handle the list of "Hidden Apps" (whitelisted apps)
 * that should not be killed or disabled during Game Mode.
 */
public class HiddenAppsManager {

    private static final String PREF_NAME = "HiddenAppsPrefs";
    private static final String KEY_HIDDEN_APPS = "hidden_apps_set";

    private final SharedPreferences prefs;

    public HiddenAppsManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Get the current set of hidden (whitelisted) package names.
     */
    public Set<String> getHiddenApps() {
        // Return a new HashSet to avoid modifying the original preference instance
        return new HashSet<>(prefs.getStringSet(KEY_HIDDEN_APPS, new HashSet<>()));
    }

    /**
     * Set the complete list of hidden (whitelisted) package names.
     */
    public void setHiddenApps(Set<String> hiddenApps) {
        prefs.edit().putStringSet(KEY_HIDDEN_APPS, hiddenApps).apply();
    }

    /**
     * Check if a specific package is currently hidden (whitelisted).
     */
    public boolean isHidden(String packageName) {
        return getHiddenApps().contains(packageName);
    }
}
