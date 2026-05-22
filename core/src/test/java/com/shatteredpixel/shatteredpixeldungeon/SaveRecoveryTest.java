/*
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.watabou.utils.FileUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class SaveRecoveryTest {

	private static HeadlessApplication app;

	@Rule
	public TemporaryFolder temp = new TemporaryFolder();

	@BeforeClass
	public static void startGdx() {
		if (app == null) {
			HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
			app = new HeadlessApplication(new ApplicationAdapter() {}, config);
		}
	}

	@Before
	public void setUp() throws IOException {
		File saveRoot = temp.newFolder("saves");
		FileUtils.setDefaultFileProperties(Files.FileType.Absolute, saveRoot.getAbsolutePath() + "/");
	}

	@After
	public void tearDown() {
		FileUtils.setDefaultFileProperties(Files.FileType.Absolute, "");
	}

	@Test
	public void backupBeforeDeleteCopiesSaveFolder() throws IOException {
		makeFakeSave(1);

		assertTrue(SaveRecovery.backupBeforeDelete(1));

		File backup = findBackupForSlot(1);
		assertNotNull("Backup folder should be created", backup);
		assertTrue("Backup should contain game.dat", new File(backup, "game.dat").exists());
		assertTrue("Backup should contain depth file", new File(backup, "depth1.dat").exists());
	}

	@Test
	public void backupBeforeDeleteCreatesRecoveryFolder() throws IOException {
		makeFakeSave(1);

		assertTrue(SaveRecovery.backupBeforeDelete(1));

		assertTrue("Recovery folder should be created", FileUtils.dirExists("recovery"));
	}

	@Test
	public void backupBeforeDeleteKeepsOnlyFiveRecentBackups() throws IOException {
		for (int i = 1; i <= 6; i++) {
			makeFakeSave(i);
			assertTrue(SaveRecovery.backupBeforeDelete(i));
			try {
				Thread.sleep(2);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

		assertEquals("Only five recovery backups should remain", 5, FileUtils.filesInDir("recovery").size());
	}

	@Test
	public void backupBeforeDeleteReturnsFalseForMissingSave() {
		assertFalse("Missing save should not crash and should return false", SaveRecovery.backupBeforeDelete(99));
	}

	private void makeFakeSave( int slot ) throws IOException {
		File folder = new File(currentSaveRoot(), "game" + slot);
		assertTrue(folder.mkdirs());
		assertTrue(new File(folder, "game.dat").createNewFile());
		assertTrue(new File(folder, "depth1.dat").createNewFile());
	}

	private File findBackupForSlot( int slot ) {
		File recovery = new File(currentSaveRoot(), "recovery");
		File[] files = recovery.listFiles();
		if (files == null) {
			return null;
		}

		for (File file : files) {
			if (file.isDirectory() && file.getName().endsWith("-slot-" + slot)) {
				return file;
			}
		}
		return null;
	}

	private File currentSaveRoot() {
		return temp.getRoot().listFiles()[0];
	}
}
