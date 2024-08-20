# Original Application Analysis

## 1. What the app does overall
The application is a Java Swing-based desktop cricket scoring tool. It allows a user to score a complete two-innings cricket match ball-by-ball. The user can configure match details (team names, overs, toss), enter player rosters (11 players per team), and then use a control panel to record runs, extras (wides, no balls, byes, leg byes), and wickets. The app tracks all individual player statistics (batting and bowling) in real-time, manages strike rotation and over changes, calculates match results, and ultimately persists the entire match data (summaries and detailed scorecards) to a MySQL database. Users can also view historical matches and their detailed scorecards.

## 2. File by file breakdown

### `src/AppMain.java`
- **What it is:** The entry point of the application.
- **What it does:** Tests the database connection and launches the main UI.
- **How it does it:** Calls `DatabaseManager.testConnection()`. If successful, calls `MainWindow.launch()`.
- **Connections:** Talks to `dao.DatabaseManager` and `ui.MainWindow`.

### `src/dao/DatabaseManager.java`
- **What it is:** A utility class for database connectivity.
- **What it does:** Provides a JDBC connection to the MySQL database.
- **How it does it:** Uses `DriverManager.getConnection` with hardcoded credentials (`jdbc:mysql://localhost:3307/db`, root/harsh@24).

### `src/dao/MatchDAO.java`
- **What it is:** The Data Access Object for matches.
- **What it does:** Saves full match data to the database and retrieves summaries and full data for past matches.
- **How it does it:** 
  - `saveMatch`: Uses a SQL transaction (`conn.setAutoCommit(false)`) to insert the match summary into the `Matches` table, then batch-inserts all `BattingStats` and `BowlingStats` for both innings.
  - `getPastMatchSummaries`: Queries the `Matches` table to return a list of `MatchSummary` objects.
  - `getMatchDataById`: Fetches the `Matches` row and all related `BattingStats` and `BowlingStats` to construct a `FullMatchData` object.
- **Connections:** Connects to `DatabaseManager` for connections and returns data structures used by `PastMatchesScreen`, `PastScorecardScreen`, and `ScoringScreen`.

### `src/dao/MatchSummary.java` & `src/dao/FullMatchData.java`
- **What they are:** Data Transfer Objects (DTOs).
- **What they do:** `MatchSummary` holds basic info (teams, date, result) for list views. `FullMatchData` holds comprehensive stats (lists of batting and bowling stats for both innings) for the detailed scorecard view.

### `src/model/Match.java`, `Player.java`, `Team.java`
- **What they are:** Empty placeholder model classes. Currently unused, as domain logic is embedded within UI classes and DTOs.

### `src/ui/BattingStats.java`
- **What it is:** A state holder for a single batsman's performance.
- **What it does:** Tracks runs, balls faced, boundaries (4s, 6s), and dismissal status.
- **How it does it:** Simple integer fields. Calculates strike rate dynamically via `getStrikeRate()`.

### `src/ui/BowlingStats.java`
- **What it is:** A state holder for a single bowler's performance.
- **What it does:** Tracks balls bowled, runs conceded, wickets, maidens, extras.
- **How it does it:** Calculates economy rate dynamically. Contains a `checkMaiden()` method called at the end of an over. *Note:* In the current logic, runs conceded by byes/leg-byes incorrectly add to `runsThisOver`, preventing a maiden.

### `src/ui/MainWindow.java`
- **What it is:** The primary launcher screen.
- **What it does:** Displays "Start New Match" and "View Past Matches" buttons.
- **Connections:** Launches `MatchSetupScreen` and `PastMatchesScreen`.

### `src/ui/MatchSetupScreen.java`
- **What it is:** The match configuration form.
- **What it does:** Captures Team 1 and 2 names, total overs, toss winner, and toss decision (Bat/Bowl).
- **Connections:** Passes data to `PlayerEntryScreen`.

### `src/ui/PlayerEntryScreen.java`
- **What it is:** The roster management screen.
- **What it does:** Allows the user to input exactly 11 players for each team. It uses `JList` components to track rosters, validates against duplicates, and determines which team bats first based on the toss decision.
- **Connections:** Passes the final rosters and match configuration to `ScoringScreen`.

### `src/ui/ScoringScreen.java`
- **What it is:** The core operational screen and engine of the app.
- **What it does:** Manages the entire match state, ball-by-ball scoring, strike rotation, innings transitions, and match saving.
- **How it does it:** 
  - Maintains state variables (`totalRuns`, `wickets`, `ballsDeliveredThisOver`) and HashMaps of `BattingStats` and `BowlingStats`.
  - Action buttons call `recordEvent`, `recordWicket`, `recordNoBall`, etc.
  - Updates striker based on odd runs (`rotateStrike()`).
  - Checks over completion (`ballsDeliveredThisOver == 6`) and innings completion (`isInningsOver()`).
  - Upon innings 1 completion, it copies stats into holding variables and resets state for innings 2.
  - Upon innings 2 completion, it triggers `MatchDAO.saveMatch()` and declares a winner.
  - Uses an internal `WicketDialog` class to capture detailed dismissal data (who caught it, who bowled it, run out runs).
- **Connections:** heavily relies on `BattingStats`, `BowlingStats`, and `MatchDAO`.

### `src/ui/ScorecardScreen.java`
- **What it is:** A live scorecard viewer.
- **What it does:** Displays tabular data for the active innings' batting and bowling performances.
- **How it does it:** Reads the `LinkedHashMap` data from `ScoringScreen` and populates uneditable `JTable`s.

### `src/ui/PastMatchesScreen.java` & `src/ui/PastScorecardScreen.java`
- **What they are:** Historical viewing screens.
- **What they do:** `PastMatchesScreen` displays a dropdown of `MatchSummary` objects. Selecting one fetches `FullMatchData` via `MatchDAO` and passes it to `PastScorecardScreen` to display the historical scorecard tables.

## 3. Complete application flow
1. **Startup:** User runs `AppMain`. App checks DB connection and opens `MainWindow`.
2. **Setup:** User clicks "Start New Match". `MatchSetupScreen` appears. User types team names, selects total overs, and defines the toss.
3. **Roster Entry:** User progresses to `PlayerEntryScreen`. They add 11 unique names to Team 1 and Team 2 lists. Clicking "Start Match" resolves the toss logic to assign the Batting and Bowling teams.
4. **First Innings Scoring:** `ScoringScreen` opens.
   - User selects the opening two batsmen from a dialog.
   - User selects the opening bowler from a dialog.
   - User clicks scoring buttons (runs, extras, wickets).
   - The app auto-rotates strike on odd runs.
   - At the end of 6 legal deliveries, the app logs the over, auto-rotates strike, and prompts the user to select the next bowler.
   - If a wicket falls, a dialog prompts for the dismissal type, involved fielder, and run out details, then prompts the user to select the next incoming batsman.
5. **Innings Transition:** When 10 wickets fall or overs run out, an alert pops up. Target is set. Team 1's stats are saved in memory. The UI resets for Team 2's innings.
6. **Second Innings:** User selects new openers and bowler. Scoring continues as before, but the app also checks if the target score is reached.
7. **Match Conclusion:** When Team 2 reaches the target, loses 10 wickets, or runs out of overs, the match ends. The result is calculated, an alert is shown, and `MatchDAO` writes the entire match dataset to the MySQL database.
8. **Review:** From `MainWindow`, user clicks "View Past Matches" to select a historical match and view its generated scorecard.

## 4. Database structure
The app uses three tables:
- **`Matches`**: Stores the match metadata.
  - `match_id` (PK), `team1_name`, `team2_name`, `total_overs`, `toss_winner`, `toss_decision`, `result_summary`, `match_date`
  - `innings1_batting_team`, `innings1_runs`, `innings1_wickets`, `innings1_overs`
  - `innings2_batting_team`, `innings2_runs`, `innings2_wickets`, `innings2_overs`
- **`BattingStats`**: Stores individual batting performances.
  - `batting_stat_id` (PK), `match_id` (FK), `innings_number` (1 or 2), `player_name`, `dismissal_info` (e.g. "not out", "b. Bowler"), `runs_scored`, `balls_faced`, `fours`, `sixes`.
- **`BowlingStats`**: Stores individual bowling performances.
  - `bowling_stat_id` (PK), `match_id` (FK), `innings_number`, `player_name`, `balls_bowled`, `maidens`, `runs_conceded`, `wickets_taken`, `dots`, `fours_conceded`, `sixes_conceded`, `wides`, `no_balls`.

## 5. Cricket scoring logic
- **Runs:** Standard runs add to team score, batsman score, and bowler conceded.
- **Balls:** Legal deliveries increment `ballsDeliveredThisOver`, bowler's balls, and batsman's balls faced. 6 legal deliveries trigger `endOfOver()`.
- **Wickets:** Increments team wickets. Updates `BattingStats.dismissalInfo`. If run out, allocates runs. Prompts for new batsman. Strike rotates based on runs completed (odd/even).
- **Strike Rotation:** `striker` points to either `currentBatsman1` or `currentBatsman2`. If runs scored (or completed during a run out) are 1, 3, or 5, the pointer swaps. It also swaps automatically at the end of an over.
- **Innings State:** The app constantly evaluates `isInningsOver()`. If true, it either triggers `startSecondInnings()` or finalizes the match.

## 6. All business rules
- **Wides:** 1 penalty run added to team total and extras. Adds 1 run to bowler's conceded runs. Does **not** count as a legal delivery.
- **No Balls:** Prompts user for "Runs off bat". Total added = 1 penalty + runs off bat. Bowler conceded = penalty + bat runs. If runs off bat > 0, they are credited to the batsman (including boundaries). Does **not** count as a legal delivery.
- **Byes & Leg Byes:** Prompts user for 0-6 runs. Adds entirely to team total and extras. Does **not** add to batsman's score. Does count as a legal delivery (batsman balls faced++, bowler balls bowled++). *Implemented Bug:* Byes/Leg byes add to `bowlerStats.runsThisOver` and `runsConceded` which violates standard cricket rules (they shouldn't count against the bowler).
- **Maiden Overs:** Checked at `endOfOver()`. If `bowler.runsThisOver == 0`, maiden is awarded. (Due to the bug above, extras can ruin maidens).
- **Player Limits:** Exactly 11 unique players required per team.
- **Validation Blocks:** The app refuses to record events if no bowler is selected, or if the innings has already concluded.
