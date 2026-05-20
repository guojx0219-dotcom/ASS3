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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Logs key runtime events related to mob behaviour.
 * Events are printed to the terminal and persisted to mob_behavior.log.
 * All public methods are thread-safe.
 */
public class MobLogger {

    public static final String LOG_FILE = "mob_behavior.log";

    private static final SimpleDateFormat FMT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);

    private static BufferedWriter writer;
    private static boolean initialized = false;

    // -----------------------------------------------------------------------
    // Lifecycle

    public static synchronized void init() {
        if (initialized) return;
        initialized = true;
        try {
            writer = new BufferedWriter(new FileWriter(LOG_FILE, true));
            writeHeader("Mob Behavior Log Started");
        } catch (IOException e) {
            System.err.println("[MobLogger] Cannot open log file: " + e.getMessage());
        }
    }

    public static synchronized void close() {
        if (!initialized) return;
        try {
            if (writer != null) {
                writeHeader("Mob Behavior Log Closed");
                writer.flush();
                writer.close();
            }
        } catch (IOException e) {
            System.err.println("[MobLogger] Cannot close log file: " + e.getMessage());
        } finally {
            writer = null;
            initialized = false;
        }
    }

    // -----------------------------------------------------------------------
    // Public logging API

    public static void logSpawn(int mobId, String className, int pos) {
        write(String.format("[%s] [SPAWN ] id=%-5d class=%-25s pos=%d",
                now(), mobId, className, pos));
    }

    public static void logStateChange(int mobId, String className,
                                      String oldState, String newState) {
        write(String.format("[%s] [STATE ] id=%-5d class=%-25s %s -> %s",
                now(), mobId, className, oldState, newState));
    }

    public static void logAlert(int mobId, String className) {
        write(String.format("[%s] [ALERT ] id=%-5d class=%-25s mob became alerted",
                now(), mobId, className));
    }

    public static void logTargetChange(int mobId, String className,
                                       int targetId, String targetClass) {
        write(String.format("[%s] [TARGET] id=%-5d class=%-25s new target: id=%d class=%s",
                now(), mobId, className, targetId, targetClass));
    }

    // -----------------------------------------------------------------------
    // Internal helpers

    private static synchronized void write(String line) {
        System.out.println(line);
        if (!initialized) init();
        if (writer != null) {
            try {
                writer.write(line);
                writer.newLine();
                writer.flush();
            } catch (IOException e) {
                System.err.println("[MobLogger] Write error: " + e.getMessage());
            }
        }
    }

    private static void writeHeader(String msg) throws IOException {
        String line = "=== " + msg + ": " + now() + " ===";
        System.out.println(line);
        if (writer != null) {
            writer.newLine();
            writer.write(line);
            writer.newLine();
            writer.flush();
        }
    }

    private static String now() {
        return FMT.format(new Date());
    }
}
