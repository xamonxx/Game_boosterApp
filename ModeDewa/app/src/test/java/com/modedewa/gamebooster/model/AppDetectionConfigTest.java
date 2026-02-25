package com.modedewa.gamebooster.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link AppDetectionConfig}.
 * Tests cover: default values, custom package management (add/remove/array),
 * and edge cases.
 */
public class AppDetectionConfigTest {

    private AppDetectionConfig config;

    @Before
    public void setUp() {
        config = new AppDetectionConfig();
    }

    // ═══════════════════════════════════════
    // Default values
    // ═══════════════════════════════════════

    @Test
    public void defaultConstructor_hasCorrectDefaults() {
        assertTrue(config.isEnabled);
        assertEquals(3, config.scanIntervalSec);
        assertTrue(config.batteryAwareEnabled);
        assertEquals(20, config.batteryThreshold);
        assertTrue(config.notificationControllerEnabled);
        assertFalse(config.autoDisableBloatware);
        assertTrue(config.autoReEnableOnExit);
        assertTrue(config.smartProfileEnabled);
        assertEquals("", config.customPackages);
    }

    // ═══════════════════════════════════════
    // addCustomPackage
    // ═══════════════════════════════════════

    @Test
    public void addCustomPackage_toEmptyList() {
        config.addCustomPackage("com.game.one");
        assertEquals("com.game.one", config.customPackages);
    }

    @Test
    public void addCustomPackage_appendsWithComma() {
        config.addCustomPackage("com.game.one");
        config.addCustomPackage("com.game.two");
        assertEquals("com.game.one,com.game.two", config.customPackages);
    }

    @Test
    public void addCustomPackage_multiplePackages() {
        config.addCustomPackage("com.game.a");
        config.addCustomPackage("com.game.b");
        config.addCustomPackage("com.game.c");
        assertEquals("com.game.a,com.game.b,com.game.c", config.customPackages);
    }

    @Test
    public void addCustomPackage_duplicateIgnored() {
        config.addCustomPackage("com.game.one");
        config.addCustomPackage("com.game.two");
        config.addCustomPackage("com.game.one");
        assertEquals("com.game.one,com.game.two", config.customPackages);
    }

    // ═══════════════════════════════════════
    // removeCustomPackage
    // ═══════════════════════════════════════

    @Test
    public void removeCustomPackage_fromMiddle() {
        config.customPackages = "com.a,com.b,com.c";
        config.removeCustomPackage("com.b");
        assertEquals("com.a,com.c", config.customPackages);
    }

    @Test
    public void removeCustomPackage_fromStart() {
        config.customPackages = "com.a,com.b,com.c";
        config.removeCustomPackage("com.a");
        assertEquals("com.b,com.c", config.customPackages);
    }

    @Test
    public void removeCustomPackage_fromEnd() {
        config.customPackages = "com.a,com.b,com.c";
        config.removeCustomPackage("com.c");
        assertEquals("com.a,com.b", config.customPackages);
    }

    @Test
    public void removeCustomPackage_onlyPackage() {
        config.customPackages = "com.only";
        config.removeCustomPackage("com.only");
        assertEquals("", config.customPackages);
    }

    @Test
    public void removeCustomPackage_notPresent_noChange() {
        config.customPackages = "com.a,com.b";
        config.removeCustomPackage("com.missing");
        assertEquals("com.a,com.b", config.customPackages);
    }

    // ═══════════════════════════════════════
    // getCustomPackageArray
    // ═══════════════════════════════════════

    @Test
    public void getCustomPackageArray_emptyString_returnsEmptyArray() {
        config.customPackages = "";
        String[] result = config.getCustomPackageArray();
        assertEquals(0, result.length);
    }

    @Test
    public void getCustomPackageArray_nullString_returnsEmptyArray() {
        config.customPackages = null;
        String[] result = config.getCustomPackageArray();
        assertEquals(0, result.length);
    }

    @Test
    public void getCustomPackageArray_singlePackage() {
        config.customPackages = "com.game.one";
        String[] result = config.getCustomPackageArray();
        assertEquals(1, result.length);
        assertEquals("com.game.one", result[0]);
    }

    @Test
    public void getCustomPackageArray_multiplePackages() {
        config.customPackages = "com.a,com.b,com.c";
        String[] result = config.getCustomPackageArray();
        assertEquals(3, result.length);
        assertEquals("com.a", result[0]);
        assertEquals("com.b", result[1]);
        assertEquals("com.c", result[2]);
    }

    @Test
    public void getCustomPackageArray_whitespaceOnly_returnsEmptyArray() {
        config.customPackages = "   ";
        String[] result = config.getCustomPackageArray();
        assertEquals(0, result.length);
    }

    // ═══════════════════════════════════════
    // Integration: add then get array
    // ═══════════════════════════════════════

    @Test
    public void addThenGetArray_roundTrip() {
        config.addCustomPackage("com.x");
        config.addCustomPackage("com.y");
        config.addCustomPackage("com.z");

        String[] arr = config.getCustomPackageArray();
        assertEquals(3, arr.length);
        assertEquals("com.x", arr[0]);
        assertEquals("com.y", arr[1]);
        assertEquals("com.z", arr[2]);
    }

    @Test
    public void addRemoveThenGetArray_roundTrip() {
        config.addCustomPackage("com.a");
        config.addCustomPackage("com.b");
        config.addCustomPackage("com.c");
        config.removeCustomPackage("com.b");

        String[] arr = config.getCustomPackageArray();
        assertEquals(2, arr.length);
        assertEquals("com.a", arr[0]);
        assertEquals("com.c", arr[1]);
    }

    // ═══════════════════════════════════════
    // Field mutability
    // ═══════════════════════════════════════

    @Test
    public void fieldsAreMutable() {
        config.isEnabled = false;
        config.scanIntervalSec = 5;
        config.batteryAwareEnabled = false;
        config.batteryThreshold = 10;
        config.notificationControllerEnabled = false;
        config.autoDisableBloatware = true;
        config.autoReEnableOnExit = false;
        config.smartProfileEnabled = false;

        assertFalse(config.isEnabled);
        assertEquals(5, config.scanIntervalSec);
        assertFalse(config.batteryAwareEnabled);
        assertEquals(10, config.batteryThreshold);
        assertFalse(config.notificationControllerEnabled);
        assertTrue(config.autoDisableBloatware);
        assertFalse(config.autoReEnableOnExit);
        assertFalse(config.smartProfileEnabled);
    }
}
