/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon;

import com.watabou.utils.FileUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SaveRecovery {

	private static final String RECOVERY_DIR = "recovery";
	private static final int MAX_RECOVERED_SAVES = 5;

	public static boolean backupBeforeDelete( int slot ) {
		String source = GamesInProgress.gameFolder(slot);
		String target = RECOVERY_DIR + "/deleted-" + timestamp() + "-slot-" + slot;

		if (!FileUtils.dirExists(source)) {
			logFail("Save folder not found: " + source);
			return false;
		}

		try {
			FileUtils.makeDir(RECOVERY_DIR);

			// Copy whole game folder before old delete logic runs.
			boolean copied = FileUtils.copyDir(source, target);
			if (!copied) {
				logFail("Cannot copy save from " + source + " to " + target);
				return false;
			}

			pruneOldRecoveries();
			return true;
		} catch (Exception e) {
			logFail("Backup failed for slot " + slot + ": " + e.getMessage());
			return false;
		}
	}

	private static long timestamp() {
		return System.currentTimeMillis();
	}

	private static void pruneOldRecoveries() {
		ArrayList<String> recoverList = FileUtils.filesInDir(RECOVERY_DIR);

		Collections.sort(recoverList, new Comparator<String>() {
			@Override
			public int compare(String left, String right) {
				return (int)Math.signum(getTime(right) - getTime(left));
			}
		});

		// Keep only latest 5 backups to save disk space.
		for (int i = MAX_RECOVERED_SAVES; i < recoverList.size(); i++) {
			FileUtils.deleteDir(RECOVERY_DIR + "/" + recoverList.get(i));
		}
	}

	private static long getTime( String folderName ) {
		int start = folderName.indexOf("deleted-");
		int end = folderName.indexOf("-slot-");
		if (start == -1 || end == -1) {
			return 0;
		}

		try {
			return Long.parseLong(folderName.substring(start + 8, end));
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	private static void logFail( String text ) {
		// Print error only, backup failure should not crash the game.
		System.err.println("[SaveRecovery] " + text);
	}
}
