package com.modedewa.gamebooster.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.modedewa.gamebooster.model.GameInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * GameDetector — Scans installed games from a known list of popular game packages.
 */
public class GameDetector {

    private static final String TAG = "GameDetector";

    // Known game packages (from modeGameON.sh)
    private static final String[][] KNOWN_GAMES = {
            {"com.mobile.legends", "Mobile Legends: Bang Bang"},
            {"com.tencent.ig", "PUBG Mobile"},
            {"com.pubg.krmobile", "PUBG Mobile KR"},
            {"com.garena.game.codm", "Call of Duty Mobile (Garena)"},
            {"com.activision.callofduty.shooter", "Call of Duty Mobile"},
            {"com.dts.freefireth", "Free Fire"},
            {"com.dts.freefiremax", "Free Fire MAX"},
            {"com.miHoYo.GenshinImpact", "Genshin Impact"},
            {"com.HoYoverse.hkrpgoversea", "Honkai: Star Rail"},
            {"com.riotgames.league.wildrift", "Wild Rift"},
            {"com.supercell.clashofclans", "Clash of Clans"},
            {"com.supercell.clashroyale", "Clash Royale"},
            {"com.nianticlabs.pokemongo", "Pokemon GO"},
            {"com.mojang.minecraftpe", "Minecraft"},
            {"com.tencent.lolm", "League of Legends Mobile"},
            {"com.YoStarEN.Arknights", "Arknights"},
            {"com.ea.game.pvzfree_row", "Plants vs Zombies"},
            {"com.igg.android.lordsmobile", "Lords Mobile"},
            {"com.epicgames.fortnite", "Fortnite"},
            {"com.innersloth.spacemafia", "Among Us"},
            {"com.axlebolt.standoff2", "Standoff 2"},
            {"com.proximabeta.mf.uamo", "Arena of Valor"},
    };

    private final PackageManager pm;

    /**
     * Constructor. Menyimpan referensi PackageManager untuk query app.
     *
     * @param context Android context untuk akses PackageManager
     */
    public GameDetector(Context context) {
        this.pm = context.getPackageManager();
    }

    /**
     * Scan for installed games and return the list.
     */
    public List<GameInfo> detectInstalledGames() {
        List<GameInfo> games = new ArrayList<>();

        for (String[] game : KNOWN_GAMES) {
            String packageName = game[0];
            String displayName = game[1];

            if (isPackageInstalled(packageName)) {
                GameInfo info = new GameInfo();
                info.packageName = packageName;
                info.displayName = getAppLabel(packageName, displayName);
                info.icon = getAppIcon(packageName);
                info.isInstalled = true;
                games.add(info);
                Log.d(TAG, "Game detected: " + info.displayName);
            }
        }

        return games;
    }

    /**
     * Check if Mobile Legends is installed (primary target game).
     */
    public GameInfo detectMLBB() {
        if (isPackageInstalled("com.mobile.legends")) {
            GameInfo info = new GameInfo();
            info.packageName = "com.mobile.legends";
            info.displayName = getAppLabel("com.mobile.legends", "Mobile Legends");
            info.icon = getAppIcon("com.mobile.legends");
            info.isInstalled = true;
            return info;
        }
        return null;
    }

    /**
     * Check if a package is installed.
     */
    public boolean isPackageInstalled(String packageName) {
        try {
            pm.getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * Get the app label (display name) for a package.
     */
    private String getAppLabel(String packageName, String fallback) {
        try {
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
            CharSequence label = pm.getApplicationLabel(appInfo);
            return label != null ? label.toString() : fallback;
        } catch (PackageManager.NameNotFoundException e) {
            return fallback;
        }
    }

    /**
     * Get the app icon drawable for a package.
     */
    private Drawable getAppIcon(String packageName) {
        try {
            return pm.getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    /**
     * Get the list of all known game package names for quick lookup.
     */
    public static List<String> getAllKnownPackages() {
        List<String> packages = new ArrayList<>();
        for (String[] game : KNOWN_GAMES) {
            packages.add(game[0]);
        }
        return packages;
    }
}
