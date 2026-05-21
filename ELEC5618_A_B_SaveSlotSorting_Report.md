# ELEC5618 Assignment 3 - Member A/B Report

## Improvement

Improve Save Slot Sorting Usability and Robustness.

This change adds a third save-slot sorting mode, `Deepest Floor`, and validates stored sort settings so invalid or corrupted values safely fall back to `Level`.

## ISO/IEC 25010 Quality Attributes

- Usability / Operability: players can choose a save slot based on deepest dungeon progress, which is a natural decision point when several saves exist.
- Reliability / Fault tolerance: invalid saved configuration values no longer leave the start scene without a valid sort label or ordering.
- Maintainability / Testability: sort modes are centralized as constants, and sorting/validation logic is extracted into pure methods that can be unit tested.

## Code Files Changed

- `build.gradle`: changed `gdxControllersVersion` from `2.2.4-SNAPSHOT` to stable `2.2.4` as recommended in the assignment setup notes.
- `core/build.gradle`: added JUnit 4 test dependency.
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/GamesInProgress.java`: added sort constants, validation, next-state logic, `Deepest Floor` comparator, and a testable `sort(...)` method.
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/SPDSettings.java`: validates saved sort values on both read and write.
- `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/scenes/StartScene.java`: shows the new `Sort: Deepest Floor` label and cycles Level -> Recent -> Deepest Floor -> Level.
- `core/src/main/assets/messages/scenes/scenes.properties`: English UI label for the new sort mode.
- `core/src/main/assets/messages/scenes/scenes_zh.properties`: Chinese UI label for the new sort mode.
- `core/src/test/java/com/shatteredpixel/shatteredpixeldungeon/GamesInProgressSortTest.java`: unit tests for the improvement.

## Testing Methods Used

1. White-box testing
   - Target: `GamesInProgress.validateSort(...)` and invalid-setting fallback paths.
   - Purpose: verify each branch accepts known values and rejects invalid, empty, or null values.

2. Finite State Machine testing
   - Target: `GamesInProgress.nextSort(...)`.
   - States: `Level`, `Recent`, `Deepest Floor`.
   - Expected transitions: Level -> Recent -> Deepest Floor -> Level.
   - Invalid-state transition: invalid value is normalized to Level, then advances to Recent.

3. Object-oriented unit testing
   - Target: `GamesInProgress.Info` objects sorted through `GamesInProgress.sort(...)`.
   - Purpose: verify object fields `maxDepth`, `depth`, `level`, and `lastPlayed` produce the expected ordering.

## Test Cases

| Test case | Method | Expected result |
| --- | --- | --- |
| Invalid sort values | White-box | `null`, empty string, and unknown sort values return `level` |
| Known sort values | White-box | `level`, `last_played`, and `deepest_floor` remain unchanged |
| Sort button cycle | FSM | Level -> Recent -> Deepest Floor -> Level |
| Deepest floor order | Object-oriented | Higher `maxDepth` first; ties use current `depth`, then recent play time |
| Invalid setting order | White-box + object-oriented | Invalid sort mode falls back to level ordering |

## Test Results

Command:

```powershell
.\gradlew.bat :core:test
```

Result:

- Test suite: `GamesInProgressSortTest`
- Tests: 5
- Failures: 0
- Errors: 0
- Skipped: 0
- XML result: `core/build/test-results/test/TEST-com.shatteredpixel.shatteredpixeldungeon.GamesInProgressSortTest.xml`

Build command:

```powershell
.\gradlew.bat :desktop:build
```

Result:

- `BUILD SUCCESSFUL in 14s`
- Desktop artifacts created:
  - `desktop/build/distributions/desktop-3.0.2.zip`
  - `desktop/build/distributions/desktop-3.0.2.tar`
  - `desktop/build/libs/desktop-3.0.2.jar`

Notes:

- The build prints Java 8 source/target warnings under JDK 21. These are existing compatibility warnings and do not fail tests or build.
- The build prints Gradle deprecation warnings from existing project scripts. They do not affect this improvement.

## Basic Build / Run Instructions

From the project root:

```powershell
.\gradlew.bat :core:test
.\gradlew.bat :desktop:build
.\gradlew.bat :desktop:debug
```

The `:desktop:debug` task launches the desktop game. In the start scene with at least two saves, click the sort button repeatedly to observe:

```text
Sort: Level -> Sort: Recent -> Sort: Deepest Floor -> Sort: Level
```

If a stored preference is corrupted, for example `games_sort = broken_setting`, the game treats it as:

```text
Sort: Level
```

## Suggested Git Workflow

Suggested branch name:

```text
feature/save-slot-deepest-floor-sort
```

Suggested commits:

```text
Implement deepest-floor save slot sorting
Add save slot sorting validation tests
Fix Gradle controller dependency for stable build
```

## Video / Q&A Speaking Points

- Member A can show `GamesInProgress.java`, explain the new comparator, and connect it to ISO/IEC 25010 usability, reliability, and maintainability.
- Member B can show `GamesInProgressSortTest.java`, explain the selected SQA methods, and show the passing `:core:test` and `:desktop:build` results.
- The key reliability point is that old or corrupted settings are handled in both `SPDSettings.gamesInProgressSort()` and `SPDSettings.gamesInProgressSort(String value)`, so the UI and sorting logic always receive a valid mode.
