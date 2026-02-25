package com.modedewa.gamebooster.model;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link GameInfo}.
 * Tests cover: constructors, factory methods, profile/status labels,
 * equals/hashCode contract, and toString.
 */
public class GameInfoTest {

    // ═══════════════════════════════════════
    // Default constructor
    // ═══════════════════════════════════════

    @Test
    public void defaultConstructor_hasCorrectDefaults() {
        GameInfo info = new GameInfo();

        assertEquals("", info.packageName);
        assertEquals("", info.displayName);
        assertNull(info.icon);
        assertFalse(info.isInstalled);
        assertFalse(info.isSelected);
        assertTrue(info.isAutoDetectEnabled);
        assertFalse(info.isCustomAdded);
        assertEquals("balanced", info.selectedProfile);
        assertFalse(info.isRunning);
        assertEquals(0, info.lastDetectedTimestamp);
        assertEquals(0, info.totalPlayTimeMs);
    }

    // ═══════════════════════════════════════
    // Two-arg constructor
    // ═══════════════════════════════════════

    @Test
    public void twoArgConstructor_setsFieldsCorrectly() {
        GameInfo info = new GameInfo("com.mobile.legends", "Mobile Legends");

        assertEquals("com.mobile.legends", info.packageName);
        assertEquals("Mobile Legends", info.displayName);
        assertTrue(info.isInstalled);
        // defaults preserved
        assertFalse(info.isSelected);
        assertTrue(info.isAutoDetectEnabled);
        assertFalse(info.isCustomAdded);
        assertEquals("balanced", info.selectedProfile);
    }

    // ═══════════════════════════════════════
    // createCustom factory
    // ═══════════════════════════════════════

    @Test
    public void createCustom_marksAsCustomAndLight() {
        GameInfo info = GameInfo.createCustom("com.custom.app", "Custom App");

        assertEquals("com.custom.app", info.packageName);
        assertEquals("Custom App", info.displayName);
        assertTrue(info.isInstalled);
        assertTrue(info.isCustomAdded);
        assertEquals("light", info.selectedProfile);
    }

    // ═══════════════════════════════════════
    // getProfileLabel
    // ═══════════════════════════════════════

    @Test
    public void getProfileLabel_ultra() {
        GameInfo info = new GameInfo();
        info.selectedProfile = "ultra";
        assertEquals("ULTRA", info.getProfileLabel());
    }

    @Test
    public void getProfileLabel_balanced() {
        GameInfo info = new GameInfo();
        info.selectedProfile = "balanced";
        assertEquals("BALANCED", info.getProfileLabel());
    }

    @Test
    public void getProfileLabel_light() {
        GameInfo info = new GameInfo();
        info.selectedProfile = "light";
        assertEquals("LIGHT", info.getProfileLabel());
    }

    @Test
    public void getProfileLabel_unknown_defaultsToBalanced() {
        GameInfo info = new GameInfo();
        info.selectedProfile = "extreme";
        assertEquals("BALANCED", info.getProfileLabel());
    }

    // ═══════════════════════════════════════
    // getStatusLabel
    // ═══════════════════════════════════════

    @Test
    public void getStatusLabel_autoDetectEnabled_notCustom_returnsAuto() {
        GameInfo info = new GameInfo("com.test", "Test");
        info.isAutoDetectEnabled = true;
        info.isCustomAdded = false;
        assertEquals("AUTO", info.getStatusLabel());
    }

    @Test
    public void getStatusLabel_autoDetectEnabled_customAdded_returnsManual() {
        GameInfo info = GameInfo.createCustom("com.custom", "Custom");
        info.isAutoDetectEnabled = true;
        assertEquals("MANUAL", info.getStatusLabel());
    }

    @Test
    public void getStatusLabel_autoDetectDisabled_returnsMati() {
        GameInfo info = new GameInfo("com.test", "Test");
        info.isAutoDetectEnabled = false;
        assertEquals("MATI", info.getStatusLabel());
    }

    @Test
    public void getStatusLabel_autoDetectDisabled_customAdded_stillReturnsMati() {
        GameInfo info = GameInfo.createCustom("com.custom", "Custom");
        info.isAutoDetectEnabled = false;
        assertEquals("MATI", info.getStatusLabel());
    }

    // ═══════════════════════════════════════
    // toString
    // ═══════════════════════════════════════

    @Test
    public void toString_formatsCorrectly() {
        GameInfo info = new GameInfo("com.mobile.legends", "Mobile Legends");
        assertEquals("Mobile Legends (com.mobile.legends)", info.toString());
    }

    @Test
    public void toString_emptyDefaults() {
        GameInfo info = new GameInfo();
        assertEquals(" ()", info.toString());
    }

    // ═══════════════════════════════════════
    // equals and hashCode
    // ═══════════════════════════════════════

    @Test
    public void equals_samePackageName_isEqual() {
        GameInfo a = new GameInfo("com.game", "Game A");
        GameInfo b = new GameInfo("com.game", "Game B");
        assertEquals(a, b);
    }

    @Test
    public void equals_differentPackageName_isNotEqual() {
        GameInfo a = new GameInfo("com.game.one", "Game");
        GameInfo b = new GameInfo("com.game.two", "Game");
        assertNotEquals(a, b);
    }

    @Test
    public void equals_sameInstance_isEqual() {
        GameInfo a = new GameInfo("com.game", "Game");
        assertEquals(a, a);
    }

    @Test
    public void equals_null_isNotEqual() {
        GameInfo a = new GameInfo("com.game", "Game");
        assertNotEquals(a, null);
    }

    @Test
    public void equals_differentType_isNotEqual() {
        GameInfo a = new GameInfo("com.game", "Game");
        assertNotEquals(a, "com.game");
    }

    @Test
    public void hashCode_samePackageName_sameHash() {
        GameInfo a = new GameInfo("com.game", "Game A");
        GameInfo b = new GameInfo("com.game", "Game B");
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void hashCode_nullPackageName_returnsZero() {
        GameInfo a = new GameInfo();
        a.packageName = null;
        assertEquals(0, a.hashCode());
    }

    // ═══════════════════════════════════════
    // Field mutability
    // ═══════════════════════════════════════

    @Test
    public void fieldsAreMutable() {
        GameInfo info = new GameInfo();
        info.isRunning = true;
        info.lastDetectedTimestamp = 123456789L;
        info.totalPlayTimeMs = 3600000L;
        info.isSelected = true;

        assertTrue(info.isRunning);
        assertEquals(123456789L, info.lastDetectedTimestamp);
        assertEquals(3600000L, info.totalPlayTimeMs);
        assertTrue(info.isSelected);
    }
}
