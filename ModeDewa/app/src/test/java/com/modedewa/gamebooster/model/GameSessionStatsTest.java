package com.modedewa.gamebooster.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link GameSessionStats}.
 * Tests cover: factory creation, sample recording, session lifecycle,
 * battery calculation, and duration formatting.
 */
public class GameSessionStatsTest {

    private GameSessionStats stats;

    @Before
    public void setUp() {
        stats = new GameSessionStats();
    }

    // ═══════════════════════════════════════
    // Default constructor
    // ═══════════════════════════════════════

    @Test
    public void defaultConstructor_allFieldsZeroOrNull() {
        assertNull(stats.gamePackage);
        assertNull(stats.gameName);
        assertNull(stats.profileUsed);
        assertEquals(0, stats.startTime);
        assertEquals(0, stats.endTime);
        assertEquals(0, stats.durationMs);
        assertEquals(0f, stats.maxTemp, 0.001f);
        assertEquals(0f, stats.avgTemp, 0.001f);
        assertEquals(0, stats.avgFps);
        assertEquals(0, stats.minFps);
        assertEquals(0, stats.avgRamUsedMb);
        assertEquals(0, stats.batteryStart);
        assertEquals(0, stats.batteryEnd);
        assertEquals(0, stats.appsDisabledCount);
    }

    // ═══════════════════════════════════════
    // startSession factory
    // ═══════════════════════════════════════

    @Test
    public void startSession_setsFieldsCorrectly() {
        GameSessionStats s = GameSessionStats.startSession(
                "com.mobile.legends", "Mobile Legends", "ultra", 85);

        assertEquals("com.mobile.legends", s.gamePackage);
        assertEquals("Mobile Legends", s.gameName);
        assertEquals("ultra", s.profileUsed);
        assertEquals(85, s.batteryStart);
        assertEquals(0f, s.maxTemp, 0.001f);
        assertTrue(s.startTime > 0);
    }

    @Test
    public void startSession_timestampIsCurrentTime() {
        long before = System.currentTimeMillis();
        GameSessionStats s = GameSessionStats.startSession(
                "com.test", "Test", "balanced", 50);
        long after = System.currentTimeMillis();

        assertTrue(s.startTime >= before);
        assertTrue(s.startTime <= after);
    }

    // ═══════════════════════════════════════
    // recordSample
    // ═══════════════════════════════════════

    @Test
    public void recordSample_singleSample_setsAllValues() {
        stats.recordSample(42.5f, 30, 1200);

        assertEquals(42.5f, stats.maxTemp, 0.001f);
        assertEquals(42.5f, stats.avgTemp, 0.001f);
        assertEquals(30, stats.avgFps);
        assertEquals(30, stats.minFps);
        assertEquals(1200, stats.avgRamUsedMb);
    }

    @Test
    public void recordSample_multipleSamples_calculatesAverages() {
        stats.recordSample(40.0f, 30, 1000);
        stats.recordSample(44.0f, 26, 1400);
        stats.recordSample(46.0f, 34, 1200);

        // Max temp = 46.0
        assertEquals(46.0f, stats.maxTemp, 0.001f);
        // Avg temp = (40 + 44 + 46) / 3 = 43.333...
        assertEquals(43.333f, stats.avgTemp, 0.01f);
        // Avg FPS = (30 + 26 + 34) / 3 = 30
        assertEquals(30, stats.avgFps);
        // Min FPS = 26
        assertEquals(26, stats.minFps);
        // Avg RAM = (1000 + 1400 + 1200) / 3 = 1200
        assertEquals(1200, stats.avgRamUsedMb);
    }

    @Test
    public void recordSample_zeroFps_isIgnored() {
        stats.recordSample(40.0f, 30, 1000);
        stats.recordSample(42.0f, 0, 1200);

        // FPS should still reflect only the non-zero sample
        assertEquals(30, stats.avgFps);
        assertEquals(30, stats.minFps);
        // But temp and RAM should include both samples
        assertEquals(42.0f, stats.maxTemp, 0.001f);
        assertEquals(41.0f, stats.avgTemp, 0.001f);
        assertEquals(1100, stats.avgRamUsedMb);
    }

    @Test
    public void recordSample_maxTemp_tracksHighest() {
        stats.recordSample(50.0f, 30, 1000);
        stats.recordSample(45.0f, 30, 1000);
        stats.recordSample(48.0f, 30, 1000);

        assertEquals(50.0f, stats.maxTemp, 0.001f);
    }

    @Test
    public void recordSample_minFps_tracksLowest() {
        stats.recordSample(40.0f, 60, 1000);
        stats.recordSample(40.0f, 25, 1000);
        stats.recordSample(40.0f, 45, 1000);

        assertEquals(25, stats.minFps);
    }

    // ═══════════════════════════════════════
    // endSession
    // ═══════════════════════════════════════

    @Test
    public void endSession_setsDurationAndEndTime() {
        stats.startTime = System.currentTimeMillis() - 5000; // 5 seconds ago
        stats.endSession(70);

        assertTrue(stats.endTime > 0);
        assertTrue(stats.durationMs >= 4900); // allow ~100ms tolerance
        assertTrue(stats.durationMs <= 6000);
        assertEquals(70, stats.batteryEnd);
    }

    // ═══════════════════════════════════════
    // getBatteryUsed
    // ═══════════════════════════════════════

    @Test
    public void getBatteryUsed_normalUsage() {
        stats.batteryStart = 85;
        stats.batteryEnd = 60;
        assertEquals(25, stats.getBatteryUsed());
    }

    @Test
    public void getBatteryUsed_noUsage() {
        stats.batteryStart = 50;
        stats.batteryEnd = 50;
        assertEquals(0, stats.getBatteryUsed());
    }

    @Test
    public void getBatteryUsed_chargingDuringSession_clampsToZero() {
        stats.batteryStart = 30;
        stats.batteryEnd = 55;
        assertEquals(0, stats.getBatteryUsed());
    }

    // ═══════════════════════════════════════
    // getFormattedDuration
    // ═══════════════════════════════════════

    @Test
    public void getFormattedDuration_zeroDuration() {
        stats.durationMs = 0;
        assertEquals("00:00:00", stats.getFormattedDuration());
    }

    @Test
    public void getFormattedDuration_secondsOnly() {
        stats.durationMs = 45_000; // 45 seconds
        assertEquals("00:00:45", stats.getFormattedDuration());
    }

    @Test
    public void getFormattedDuration_minutesAndSeconds() {
        stats.durationMs = 125_000; // 2 min 5 sec
        assertEquals("00:02:05", stats.getFormattedDuration());
    }

    @Test
    public void getFormattedDuration_hoursMinutesSeconds() {
        stats.durationMs = 5_045_000; // 1h 24m 5s
        assertEquals("01:24:05", stats.getFormattedDuration());
    }

    // ═══════════════════════════════════════
    // getShortDuration
    // ═══════════════════════════════════════

    @Test
    public void getShortDuration_lessThanOneHour() {
        stats.durationMs = 45 * 60_000; // 45 minutes
        assertEquals("45m", stats.getShortDuration());
    }

    @Test
    public void getShortDuration_exactlyOneHour() {
        stats.durationMs = 60 * 60_000; // 60 minutes
        assertEquals("1j 0m", stats.getShortDuration());
    }

    @Test
    public void getShortDuration_hoursAndMinutes() {
        stats.durationMs = 84 * 60_000; // 1h 24m
        assertEquals("1j 24m", stats.getShortDuration());
    }

    @Test
    public void getShortDuration_zeroDuration() {
        stats.durationMs = 0;
        assertEquals("0m", stats.getShortDuration());
    }

    // ═══════════════════════════════════════
    // Full lifecycle integration
    // ═══════════════════════════════════════

    @Test
    public void fullLifecycle_startRecordEnd() {
        GameSessionStats s = GameSessionStats.startSession(
                "com.test.game", "TestGame", "balanced", 90);

        s.recordSample(38.0f, 60, 800);
        s.recordSample(42.0f, 55, 900);
        s.recordSample(41.0f, 58, 850);

        s.appsDisabledCount = 12;
        s.endSession(82);

        assertEquals("com.test.game", s.gamePackage);
        assertEquals("TestGame", s.gameName);
        assertEquals("balanced", s.profileUsed);
        assertEquals(42.0f, s.maxTemp, 0.001f);
        assertEquals(55, s.minFps);
        assertEquals(57, s.avgFps); // (60+55+58)/3 = 57.67 -> 57
        assertEquals(850, s.avgRamUsedMb); // (800+900+850)/3 = 850
        assertEquals(8, s.getBatteryUsed());
        assertEquals(12, s.appsDisabledCount);
        assertTrue(s.durationMs >= 0);
    }
}
