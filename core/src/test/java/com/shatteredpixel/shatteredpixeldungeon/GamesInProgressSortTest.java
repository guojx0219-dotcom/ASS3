package com.shatteredpixel.shatteredpixeldungeon;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class GamesInProgressSortTest {

	private GamesInProgress.Info info( int slot, int level, int depth, int maxDepth, long lastPlayed ){
		GamesInProgress.Info info = new GamesInProgress.Info();
		info.slot = slot;
		info.level = level;
		info.depth = depth;
		info.maxDepth = maxDepth;
		info.lastPlayed = lastPlayed;
		return info;
	}

	private ArrayList<GamesInProgress.Info> infos( GamesInProgress.Info... values ){
		ArrayList<GamesInProgress.Info> result = new ArrayList<>();
		for (GamesInProgress.Info value : values){
			result.add(value);
		}
		return result;
	}

	@Test
	public void validateSortFallsBackToLevelForInvalidValues() {
		assertEquals(GamesInProgress.SORT_LEVEL, GamesInProgress.validateSort(null));
		assertEquals(GamesInProgress.SORT_LEVEL, GamesInProgress.validateSort(""));
		assertEquals(GamesInProgress.SORT_LEVEL, GamesInProgress.validateSort("not_a_real_sort"));
	}

	@Test
	public void validateSortKeepsKnownValues() {
		assertEquals(GamesInProgress.SORT_LEVEL, GamesInProgress.validateSort(GamesInProgress.SORT_LEVEL));
		assertEquals(GamesInProgress.SORT_LAST_PLAYED, GamesInProgress.validateSort(GamesInProgress.SORT_LAST_PLAYED));
		assertEquals(GamesInProgress.SORT_DEEPEST_FLOOR, GamesInProgress.validateSort(GamesInProgress.SORT_DEEPEST_FLOOR));
	}

	@Test
	public void nextSortCyclesThroughAllMenuStates() {
		assertEquals(GamesInProgress.SORT_LAST_PLAYED, GamesInProgress.nextSort(GamesInProgress.SORT_LEVEL));
		assertEquals(GamesInProgress.SORT_DEEPEST_FLOOR, GamesInProgress.nextSort(GamesInProgress.SORT_LAST_PLAYED));
		assertEquals(GamesInProgress.SORT_LEVEL, GamesInProgress.nextSort(GamesInProgress.SORT_DEEPEST_FLOOR));
		assertEquals(GamesInProgress.SORT_LAST_PLAYED, GamesInProgress.nextSort("broken_setting"));
	}

	@Test
	public void deepestFloorSortUsesDepthThenRecentTieBreakers() {
		ArrayList<GamesInProgress.Info> games = infos(
				info(1, 5, 12, 12, 100L),
				info(2, 8, 6, 18, 10L),
				info(3, 3, 11, 18, 30L),
				info(4, 9, 11, 18, 40L)
		);

		GamesInProgress.sort(games, GamesInProgress.SORT_DEEPEST_FLOOR);

		assertEquals(4, games.get(0).slot);
		assertEquals(3, games.get(1).slot);
		assertEquals(2, games.get(2).slot);
		assertEquals(1, games.get(3).slot);
	}

	@Test
	public void invalidSortFallsBackToLevelOrdering() {
		ArrayList<GamesInProgress.Info> games = infos(
				info(1, 3, 10, 20, 300L),
				info(2, 8, 1, 1, 100L),
				info(3, 8, 4, 4, 200L)
		);

		GamesInProgress.sort(games, "broken_setting");

		assertEquals(3, games.get(0).slot);
		assertEquals(2, games.get(1).slot);
		assertEquals(1, games.get(2).slot);
	}
}
