/*
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for MobLogger.
 * Verifies correct formatting, file persistence, and all four event types.
 */
public class MobLoggerTest {

    @Before
    public void setUp() {
        MobLogger.close();
        new File(MobLogger.LOG_FILE).delete();
        MobLogger.init();
    }

    @After
    public void tearDown() {
        MobLogger.close();
        new File(MobLogger.LOG_FILE).delete();
    }

    // ------------------------------------------------------------------
    // Helpers

    private List<String> readLogLines() throws IOException {
        List<String> lines = new ArrayList<>();
        File f = new File(MobLogger.LOG_FILE);
        if (!f.exists()) return lines;
        try (BufferedReader r = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = r.readLine()) != null) {
                if (!line.isEmpty()) lines.add(line);
            }
        }
        return lines;
    }

    private List<String> filterByTag(List<String> lines, String tag) {
        List<String> result = new ArrayList<>();
        for (String l : lines) {
            if (l.contains(tag)) result.add(l);
        }
        return result;
    }

    // ------------------------------------------------------------------
    // Tests

    @Test
    public void testInitCreatesLogFile() {
        assertTrue("Log file should exist after init", new File(MobLogger.LOG_FILE).exists());
    }

    @Test
    public void testLogSpawnWritesToFile() throws IOException {
        MobLogger.logSpawn(42, "Rat", 150);

        List<String> lines = filterByTag(readLogLines(), "[SPAWN ]");
        assertFalse("SPAWN event should be written to file", lines.isEmpty());
        String entry = lines.get(0);
        assertTrue("Should contain mob id=42",    entry.contains("id=42"));
        assertTrue("Should contain class Rat",     entry.contains("Rat"));
        assertTrue("Should contain pos=150",       entry.contains("pos=150"));
    }

    @Test
    public void testLogStateChangeWritesToFile() throws IOException {
        MobLogger.logStateChange(7, "Brute", "SLEEPING", "HUNTING");

        List<String> lines = filterByTag(readLogLines(), "[STATE ]");
        assertFalse("STATE event should be written to file", lines.isEmpty());
        String entry = lines.get(0);
        assertTrue("Should contain mob id=7",     entry.contains("id=7"));
        assertTrue("Should contain old state",    entry.contains("SLEEPING"));
        assertTrue("Should contain new state",    entry.contains("HUNTING"));
        assertTrue("Should contain arrow ->",     entry.contains("->"));
    }

    @Test
    public void testLogAlertWritesToFile() throws IOException {
        MobLogger.logAlert(99, "Skeleton");

        List<String> lines = filterByTag(readLogLines(), "[ALERT ]");
        assertFalse("ALERT event should be written to file", lines.isEmpty());
        String entry = lines.get(0);
        assertTrue("Should contain mob id=99",       entry.contains("id=99"));
        assertTrue("Should contain class Skeleton",  entry.contains("Skeleton"));
        assertTrue("Should describe alert status",   entry.contains("alerted"));
    }

    @Test
    public void testLogTargetChangeWritesToFile() throws IOException {
        MobLogger.logTargetChange(3, "Bat", 1, "Hero");

        List<String> lines = filterByTag(readLogLines(), "[TARGET]");
        assertFalse("TARGET event should be written to file", lines.isEmpty());
        String entry = lines.get(0);
        assertTrue("Should contain source mob id=3", entry.contains("id=3"));
        assertTrue("Should contain source class Bat", entry.contains("Bat"));
        assertTrue("Should contain target class Hero", entry.contains("Hero"));
    }

    @Test
    public void testTimestampFormat() throws IOException {
        MobLogger.logSpawn(1, "Rat", 0);

        List<String> lines = filterByTag(readLogLines(), "[SPAWN ]");
        assertFalse(lines.isEmpty());
        // Format: [yyyy-MM-dd HH:mm:ss.SSS]
        assertTrue("Entry must start with a properly formatted timestamp",
                lines.get(0).matches("\\[\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}\\].*"));
    }

    @Test
    public void testMultipleEventsPreserveOrder() throws IOException {
        MobLogger.logSpawn(1, "Rat", 10);
        MobLogger.logStateChange(1, "Rat", "SLEEPING", "WANDERING");
        MobLogger.logAlert(1, "Rat");
        MobLogger.logTargetChange(1, "Rat", 2, "Hero");

        List<String> all     = readLogLines();
        List<String> spawns  = filterByTag(all, "[SPAWN ]");
        List<String> states  = filterByTag(all, "[STATE ]");
        List<String> alerts  = filterByTag(all, "[ALERT ]");
        List<String> targets = filterByTag(all, "[TARGET]");

        assertEquals("Exactly one SPAWN event",  1, spawns.size());
        assertEquals("Exactly one STATE event",  1, states.size());
        assertEquals("Exactly one ALERT event",  1, alerts.size());
        assertEquals("Exactly one TARGET event", 1, targets.size());

        assertTrue("SPAWN comes before STATE",
                all.indexOf(spawns.get(0)) < all.indexOf(states.get(0)));
        assertTrue("STATE comes before ALERT",
                all.indexOf(states.get(0)) < all.indexOf(alerts.get(0)));
        assertTrue("ALERT comes before TARGET",
                all.indexOf(alerts.get(0)) < all.indexOf(targets.get(0)));
    }

    @Test
    public void testCloseAndReopenAppendsEntries() throws IOException {
        MobLogger.logSpawn(1, "Rat", 0);
        MobLogger.close();

        MobLogger.init();
        MobLogger.logSpawn(2, "Bat", 5);

        List<String> spawns = filterByTag(readLogLines(), "[SPAWN ]");
        assertEquals("Both SPAWN entries should survive a close/reopen cycle", 2, spawns.size());
    }

    @Test
    public void testNoExceptionOnDoubleClose() {
        MobLogger.close();
        MobLogger.close(); // second close must not throw
    }

    @Test
    public void testLazyInitOnWrite() {
        MobLogger.close(); // start with logger closed
        // write() should call init() internally — must not throw
        MobLogger.logSpawn(5, "Gnoll", 20);
        assertTrue("File should be created by lazy init", new File(MobLogger.LOG_FILE).exists());
    }

    @Test
    public void testStateChangeLogsAllFourEventTypes() throws IOException {
        MobLogger.logStateChange(1, "Mob", "SLEEPING",  "WANDERING");
        MobLogger.logStateChange(1, "Mob", "WANDERING", "HUNTING");
        MobLogger.logStateChange(1, "Mob", "HUNTING",   "FLEEING");
        MobLogger.logStateChange(1, "Mob", "FLEEING",   "PASSIVE");

        List<String> lines = filterByTag(readLogLines(), "[STATE ]");
        assertEquals("All four state transitions should be logged", 4, lines.size());
    }
}
