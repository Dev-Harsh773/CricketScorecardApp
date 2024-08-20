# Cricket Scorecard App — Complete Full-Stack Plan

> This is the complete instruction file for the AI agent to build the Cricket Scorecard Application from scratch. Read every single section before writing a single line of code.

---

## What We Are Building

A full-stack web-based cricket scoring platform where:

- A scorer creates a match, sets up teams and players, and scores ball by ball
- Each match gets a unique private key — only people with that key can view that match
- Viewers login first, then enter the match key to watch the live scorecard
- Multiple matches can run simultaneously (4 colleges, 4 matches, all private)
- Past matches can be viewed with full scorecards and PDF download using the same key
- Live scores of ongoing matches created in this app are visible inside the app (NOT external cricket API)

---

## Tech Stack

| Purpose          | Technology                         |
| ---------------- | ---------------------------------- |
| Backend server   | Spring Boot 3.x (Maven, Java 17)   |
| Database         | SQLite (cricket.db — auto created) |
| ORM              | Spring Data JPA + Hibernate        |
| Authentication   | JWT + Spring Security              |
| Live score push  | WebSocket + STOMP                  |
| PDF generation   | iText 7                            |
| Frontend         | React (Create React App)           |
| API calls        | Axios                              |
| Page navigation  | React Router                       |
| Analytics charts | Recharts                           |
| Styling          | Plain vanilla CSS only             |

No Tailwind. No Docker. No Kafka. No external cricket API. No MySQL.

---

## Project Structure

```
CricketScorecardApp/
├── backend/
│   ├── src/
│   └── pom.xml
├── frontend/
│   ├── src/
│   └── package.json
├── INTERVIEW_NOTES.md
└── README.md
```

No src/ folder at root. No Swing code anywhere.

---

## CRITICAL CONCEPT — Match Access Key

Every match created in this system gets a unique `matchKey` — a random alphanumeric string (e.g. `IND-AUS-X7K2`).

Rules:

- The matchKey is generated automatically when a match is created
- The scorer (creator) sees the key and can share it
- Any logged-in user who enters this key can view that match's live scorecard
- Without the key, no one can find or view the match
- The key works for both live matches AND past completed matches
- Different matches have different keys — 4 colleges = 4 keys = completely isolated

This is implemented as:

- A `match_key` column in the matches table (unique, indexed)
- All public scorecard endpoints require the matchKey in the URL
- The scorer uses their JWT token to score — viewers only need the key after login

---

## Database Schema

Create all tables in schema.sql:

```sql
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    role TEXT NOT NULL DEFAULT 'VIEWER'
);

CREATE TABLE IF NOT EXISTS matches (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    match_key TEXT NOT NULL UNIQUE,
    team1_name TEXT NOT NULL,
    team2_name TEXT NOT NULL,
    total_overs INTEGER NOT NULL,
    venue TEXT,
    match_date TEXT,
    toss_winner TEXT,
    toss_decision TEXT,
    batting_first_team TEXT,
    bowling_first_team TEXT,
    status TEXT DEFAULT 'SETUP',
    result TEXT,
    created_by_user_id INTEGER,
    FOREIGN KEY (created_by_user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS players (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    match_id INTEGER NOT NULL,
    team_name TEXT NOT NULL,
    player_name TEXT NOT NULL,
    batting_style TEXT,
    bowling_style TEXT,
    player_role TEXT,
    batting_position INTEGER,
    FOREIGN KEY (match_id) REFERENCES matches(id)
);

CREATE TABLE IF NOT EXISTS innings (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    match_id INTEGER NOT NULL,
    innings_number INTEGER NOT NULL,
    batting_team TEXT NOT NULL,
    bowling_team TEXT NOT NULL,
    total_runs INTEGER DEFAULT 0,
    total_wickets INTEGER DEFAULT 0,
    legal_balls_bowled INTEGER DEFAULT 0,
    extras_total INTEGER DEFAULT 0,
    wides INTEGER DEFAULT 0,
    no_balls INTEGER DEFAULT 0,
    byes INTEGER DEFAULT 0,
    leg_byes INTEGER DEFAULT 0,
    status TEXT DEFAULT 'IN_PROGRESS',
    target INTEGER,
    striker_player_id INTEGER,
    non_striker_player_id INTEGER,
    current_bowler_id INTEGER,
    balls_this_over INTEGER DEFAULT 0,
    innings_state TEXT DEFAULT 'NEEDS_OPENERS',
    FOREIGN KEY (match_id) REFERENCES matches(id)
);

CREATE TABLE IF NOT EXISTS balls (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    innings_id INTEGER NOT NULL,
    over_number INTEGER NOT NULL,
    ball_number INTEGER NOT NULL,
    batsman_id INTEGER NOT NULL,
    bowler_id INTEGER NOT NULL,
    runs_scored INTEGER DEFAULT 0,
    is_wicket INTEGER DEFAULT 0,
    wicket_type TEXT,
    fielder_name TEXT,
    is_wide INTEGER DEFAULT 0,
    is_no_ball INTEGER DEFAULT 0,
    is_free_hit INTEGER DEFAULT 0,
    is_bye INTEGER DEFAULT 0,
    is_leg_bye INTEGER DEFAULT 0,
    extra_runs INTEGER DEFAULT 0,
    is_legal_delivery INTEGER DEFAULT 1,
    bowling_side TEXT,
    FOREIGN KEY (innings_id) REFERENCES innings(id),
    FOREIGN KEY (batsman_id) REFERENCES players(id),
    FOREIGN KEY (bowler_id) REFERENCES players(id)
);

CREATE TABLE IF NOT EXISTS batting_performances (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    innings_id INTEGER NOT NULL,
    player_id INTEGER NOT NULL,
    runs_scored INTEGER DEFAULT 0,
    balls_faced INTEGER DEFAULT 0,
    fours INTEGER DEFAULT 0,
    sixes INTEGER DEFAULT 0,
    dismissal_info TEXT DEFAULT 'not out',
    batting_position INTEGER,
    is_active INTEGER DEFAULT 1,
    FOREIGN KEY (innings_id) REFERENCES innings(id),
    FOREIGN KEY (player_id) REFERENCES players(id)
);

CREATE TABLE IF NOT EXISTS bowling_performances (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    innings_id INTEGER NOT NULL,
    player_id INTEGER NOT NULL,
    balls_bowled INTEGER DEFAULT 0,
    maidens INTEGER DEFAULT 0,
    runs_conceded INTEGER DEFAULT 0,
    wickets_taken INTEGER DEFAULT 0,
    dots INTEGER DEFAULT 0,
    fours_conceded INTEGER DEFAULT 0,
    sixes_conceded INTEGER DEFAULT 0,
    wides INTEGER DEFAULT 0,
    no_balls INTEGER DEFAULT 0,
    runs_this_over INTEGER DEFAULT 0,
    FOREIGN KEY (innings_id) REFERENCES innings(id),
    FOREIGN KEY (player_id) REFERENCES players(id)
);
```

Auto seed on first run using CommandLineRunner:

- Username: HarshShringi, Password: BCrypt(Harsh1234), Role: ADMIN

---

## Backend — Full Folder Structure

```
backend/src/main/java/com/cricket/scorecard/
├── AppMain.java
├── config/
│   ├── SecurityConfig.java
│   ├── WebSocketConfig.java
│   └── CorsConfig.java
├── model/
│   ├── User.java
│   ├── Match.java
│   ├── Player.java
│   ├── Innings.java
│   ├── Ball.java
│   ├── BattingPerformance.java
│   └── BowlingPerformance.java
├── repository/
│   ├── UserRepository.java
│   ├── MatchRepository.java
│   ├── PlayerRepository.java
│   ├── InningsRepository.java
│   ├── BallRepository.java
│   ├── BattingPerformanceRepository.java
│   └── BowlingPerformanceRepository.java
├── service/
│   ├── AuthService.java
│   ├── MatchService.java
│   ├── ScoringService.java
│   ├── AnalyticsService.java
│   └── PdfService.java
├── controller/
│   ├── AuthController.java
│   ├── MatchController.java
│   ├── ScoringController.java
│   ├── AnalyticsController.java
│   ├── PdfController.java
│   └── CricketApiController.java  (STUB ONLY)
├── dto/
│   ├── LoginRequest.java
│   ├── LoginResponse.java
│   ├── MatchCreateRequest.java
│   ├── PlayerRequest.java
│   ├── BallRequest.java
│   ├── WicketRequest.java
│   ├── InningsStartRequest.java
│   ├── NextBowlerRequest.java
│   ├── NextBatsmanRequest.java
│   ├── ScorecardResponse.java
│   ├── BatsmanCardDTO.java
│   ├── BowlerCardDTO.java
│   └── MatchSummaryDTO.java
└── security/
    ├── JwtUtil.java
    ├── JwtFilter.java
    └── UserDetailsServiceImpl.java
```

---

## REST API Endpoints

### Auth (public — no JWT needed)

```
POST   /api/auth/register
POST   /api/auth/login
```

### Match Management (JWT required)

```
POST   /api/matches                        Create match — returns matchKey
POST   /api/matches/{matchKey}/players     Add 11 players for both teams
POST   /api/matches/{matchKey}/start       Set openers + opening bowler, begin innings
GET    /api/matches/my                     Get all matches created by logged in user
GET    /api/matches/{matchKey}             Get match details (any logged in user with key)
DELETE /api/matches/{matchKey}             Delete match (creator or ADMIN only)
```

### Scoring (JWT required, only match creator can score)

```
POST   /api/matches/{matchKey}/ball              Record a delivery
POST   /api/matches/{matchKey}/wicket            Record a wicket
POST   /api/matches/{matchKey}/next-batsman      Select next batsman after wicket
POST   /api/matches/{matchKey}/next-bowler       Select next bowler after over
POST   /api/matches/{matchKey}/start-second-innings   Begin second innings with new openers
GET    /api/matches/{matchKey}/innings/current   Get current innings live state
```

### Scorecard (JWT required, any user with matchKey)

```
GET    /api/matches/{matchKey}/scorecard         Full live scorecard
GET    /api/matches/{matchKey}/scorecard/innings/{num}   Specific innings scorecard
```

### Analytics (JWT required)

```
GET    /api/analytics/{matchKey}/run-rate        Over by over run rate data
GET    /api/analytics/{matchKey}/summary         Match summary stats
GET    /api/analytics/top-scorers                All time top run scorers
GET    /api/analytics/top-bowlers                All time top wicket takers
```

### PDF (JWT required)

```
GET    /api/matches/{matchKey}/report/pdf        Download full match PDF (completed matches only)
```

### CricAPI Stub

```
GET    /api/cricket/live-matches                 Returns empty array with TODO comment
GET    /api/cricket/icc-rankings/{type}          Returns empty array with TODO comment
```

---

## Player Setup — Detailed Fields

When adding players, capture these fields for each player:

```json
{
  "playerName": "Virat Kohli",
  "teamName": "India",
  "playerRole": "BATSMAN",
  "battingStyle": "RIGHT_HAND",
  "bowlingStyle": "RIGHT_ARM_MEDIUM"
}
```

playerRole options: BATSMAN, BOWLER, ALL_ROUNDER, WICKET_KEEPER

battingStyle options: RIGHT_HAND, LEFT_HAND

bowlingStyle options:

- RIGHT_ARM_FAST
- LEFT_ARM_FAST
- RIGHT_ARM_OFF_SPIN
- RIGHT_ARM_LEG_SPIN
- LEFT_ARM_OFF_SPIN (chinaman)
- LEFT_ARM_LEG_SPIN
- RIGHT_ARM_MEDIUM
- LEFT_ARM_MEDIUM
- NOT_A_BOWLER

---

## ScoringService — Complete Business Rules

### recordBall(matchKey, BallRequest, userId):

1. Verify userId is the match creator — if not, throw 403 Forbidden
2. Load current innings
3. Check inningsState — must be IN_PROGRESS, else throw error
4. Determine if legal delivery:
   - Wide = not legal
   - No Ball = not legal
   - Everything else = legal
5. If legal: ballsThisOver++, batsman ballsFaced++, bowler ballsBowled++
6. Add runs to innings total
7. If wide: +1 penalty to total and bowler conceded, do NOT add to batsman
8. If no ball: +1 penalty, add bat runs to batsman and bowler, set nextBallIsFreehit = true
9. If free hit: record is_free_hit = true on this ball, batsman cannot be out bowled/lbw/hit wicket on free hit
10. If bye or leg bye: add runs to extras only, NOT to batsman, count as legal ball
11. Update BattingPerformance: runs, balls, fours (if runs==4), sixes (if runs==6)
12. Update BowlingPerformance: runs conceded, dots (if runs==0 and no extras), fours conceded, sixes conceded
13. Track runs_this_over on bowler
14. Call rotateStrike(runsScored)
15. If ballsThisOver == 6: call endOfOver()
16. Call checkInningsComplete()
17. Broadcast via WebSocket

### endOfOver():

1. Increment over count
2. Check maiden: if bowler.runsThisOver == 0 → maidens++
3. Reset bowler.runsThisOver = 0
4. Reset ballsThisOver = 0
5. Rotate strike (end of over always swaps)
6. Save bowling performance
7. Set inningsState = NEEDS_BOWLER

### recordWicket(matchKey, WicketRequest, userId):

1. Verify userId is match creator
2. Check if free hit — if yes, only run out is valid dismissal
3. Increment innings wickets
4. Build dismissal string:
   - BOWLED → "b. BowlerName"
   - CAUGHT → "c. FielderName b. BowlerName"
   - LBW → "lbw b. BowlerName"
   - RUN OUT → "run out (FielderName)"
   - STUMPED → "st. FielderName b. BowlerName"
   - HIT WICKET → "hit wicket b. BowlerName"
   - RETIRED HURT → "retired hurt"
5. Update batsman BattingPerformance dismissalInfo
6. Increment bowler wickets (NOT for run out and retired hurt)
7. For run out: if runsBeforeDismissal is odd, rotate strike
8. Count as legal delivery UNLESS it was also a no ball
9. Check if innings complete (10 wickets)
10. If not complete: set inningsState = NEEDS_BATSMAN
11. Save and broadcast

### rotateStrike(runs):

- If runs % 2 != 0: swap striker and nonStriker in innings record

### checkInningsComplete():

- If wickets == 10 OR (legal_balls_bowled / 6) >= totalOvers:
  - If innings_number == 1: call startSecondInnings()
  - If innings_number == 2: call concludeMatch()

### startSecondInnings():

1. Set innings 1 status = COMPLETED
2. Calculate target = innings1Runs + 1
3. Create innings 2 record with target stored
4. Set match status = SECOND_INNINGS
5. Set inningsState = NEEDS_OPENERS

### concludeMatch():

1. If innings2Runs >= target: batting team wins by (10 - wickets) wickets
2. If innings2Runs < target: bowling team wins by (target - innings2Runs - 1) runs
3. If equal: Tie
4. Save result to match
5. Set match status = COMPLETED
6. Broadcast final result

### Projected Score calculation:

- currentRunRate = (totalRuns / legalBallsBowled) \* 6
- projectedScore = currentRunRate \* totalOvers
- requiredRunRate = (target - currentRuns) / remainingOvers (second innings only)

### Bowling side tracking:

- Record whether bowler is bowling OVER_THE_WICKET or ROUND_THE_WICKET on each ball
- Store in bowling_side column in balls table

---

## WebSocket Broadcasting

Config:

- STOMP endpoint: /ws
- App prefix: /app
- Topic prefix: /topic

Broadcast after EVERY ball and wicket to:
`/topic/match/{matchKey}/score`

Payload:

```json
{
  "matchKey": "IND-AUS-X7K2",
  "matchStatus": "IN_PROGRESS",
  "inningsNumber": 1,
  "battingTeam": "India",
  "bowlingTeam": "Australia",
  "totalRuns": 145,
  "totalWickets": 3,
  "legalBallsBowled": 112,
  "oversDisplay": "18.4",
  "currentRunRate": 7.83,
  "projectedScore": 235,
  "target": null,
  "requiredRunRate": null,
  "striker": {
    "name": "Virat Kohli",
    "runs": 67,
    "balls": 45,
    "fours": 6,
    "sixes": 2,
    "strikeRate": 148.8
  },
  "nonStriker": {
    "name": "Rohit Sharma",
    "runs": 34,
    "balls": 28,
    "fours": 3,
    "sixes": 1,
    "strikeRate": 121.4
  },
  "currentBowler": {
    "name": "Mitchell Starc",
    "ballsBowled": 22,
    "oversDisplay": "3.4",
    "runsConceded": 28,
    "wickets": 1,
    "economy": 7.63,
    "bowlingSide": "OVER_THE_WICKET"
  },
  "lastBallDescription": "4 runs - boundary",
  "inningsState": "IN_PROGRESS",
  "isFreehit": false,
  "recentBalls": ["1", "W", "4", "0", "2", "6"]
}
```

---

## JWT Authentication

### Flow:

1. POST /api/auth/login → returns JWT token
2. Store token in localStorage on frontend
3. Every Axios request includes: Authorization: Bearer {token}
4. JwtFilter validates token on every protected request

### Roles:

- VIEWER — can view scorecards of matches they have the key for
- SCORER — can score matches they created
- ADMIN — can do everything

### Match creator check:

- When scoring a ball, backend checks if the JWT user ID matches the created_by_user_id on the match
- If not, return 403 Forbidden

---

## React Frontend — Full Structure

```
frontend/src/
├── components/
│   ├── Navbar.jsx + Navbar.css
│   ├── ProtectedRoute.jsx
│   ├── ScorecardTable.jsx + ScorecardTable.css
│   ├── BallTracker.jsx + BallTracker.css
│   └── charts/
│       ├── RunRateChart.jsx
│       ├── TopScorersChart.jsx
│       ├── TopBowlersChart.jsx
│       └── RunBreakdownPie.jsx
├── pages/
│   ├── LoginPage.jsx + LoginPage.css
│   ├── Dashboard.jsx + Dashboard.css
│   ├── MatchSetup.jsx + MatchSetup.css
│   ├── PlayerSetup.jsx + PlayerSetup.css
│   ├── LiveScoring.jsx + LiveScoring.css
│   ├── Scorecard.jsx + Scorecard.css
│   ├── WatchLive.jsx + WatchLive.css
│   ├── EnterMatchKey.jsx + EnterMatchKey.css
│   ├── PastMatches.jsx + PastMatches.css
│   ├── PastScorecard.jsx + PastScorecard.css
│   └── Analytics.jsx + Analytics.css
├── services/
│   ├── api.js
│   └── websocket.js
├── context/
│   └── AuthContext.jsx
└── App.jsx
```

---

## React Pages — Detailed Requirements

### LoginPage.jsx:

- Username + password fields
- Login button → POST /api/auth/login → store JWT
- Register option for new users
- Redirect to Dashboard after login

### Dashboard.jsx:

- Welcome message with username
- Three main options as big cards:
  1. "Create New Match" → MatchSetup
  2. "My Matches" → list of matches this user created
  3. "Enter Match Key" → EnterMatchKey page (to watch someone else's match)
- Show in-progress matches created by this user with "Continue Scoring" button

### EnterMatchKey.jsx:

- Simple input field: "Enter Match Key"
- Submit → GET /api/matches/{matchKey}
- If match is IN_PROGRESS → redirect to WatchLive
- If match is COMPLETED → redirect to PastScorecard
- If key not found → show error

### MatchSetup.jsx:

- Fields: Team 1 name, Team 2 name, Total overs, Venue (optional), Match date
- Toss winner dropdown (Team 1 / Team 2)
- Toss decision dropdown (Bat / Bowl)
- Submit → POST /api/matches
- Response includes matchKey — show it prominently: "Your match key is: IND-AUS-X7K2 — Share this with viewers"
- Copy to clipboard button
- Redirect to PlayerSetup

### PlayerSetup.jsx:

- Two sections side by side — Team 1 and Team 2
- For each player, capture:
  - Player name
  - Role (Batsman / Bowler / All-Rounder / Wicket Keeper)
  - Batting style (Right Hand / Left Hand)
  - Bowling style (dropdown with all options)
- Add player button, shows list below
- Validate: exactly 11 players, no duplicates
- Submit both teams → POST /api/matches/{matchKey}/players
- Then show opener selection dialog:
  - Select Striker (batsman 1)
  - Select Non-striker (batsman 2)
  - Select Opening bowler
  - Select bowling side (Over the wicket / Round the wicket)
- Submit → POST /api/matches/{matchKey}/start
- Redirect to LiveScoring

### LiveScoring.jsx — MOST IMPORTANT PAGE:

Top section — match info bar:

- Team scores: "India 145/3 (18.4 Ov)"
- Current Run Rate
- Projected Score (1st innings) OR Target and Required Run Rate (2nd innings)
- Match key displayed for easy sharing

Middle section — current players:

- Striker batsman: name, runs, balls, 4s, 6s, SR (mark with \* for striker)
- Non-striker batsman: same stats
- Current bowler: name, overs, runs, wickets, economy
- Bowling side shown (Over/Round the wicket)

Bottom section — scoring controls:
Run buttons: 0 | 1 | 2 | 3 | 4 | 6
Extra buttons: Wide | No Ball | Bye | Leg Bye
Wicket button (red)

If FREE HIT is active — show "FREE HIT" banner, disable Bowled/LBW/Hit Wicket options

Recent balls display: show last 6 balls of current over as dots/numbers/W

Wicket Dialog (popup when Wicket clicked):

- Dismissal type dropdown: Bowled / Caught / LBW / Run Out / Stumped / Hit Wicket / Retired Hurt
- If Caught: fielder name input
- If Run Out: fielder name input + runs completed before dismissal input
- If Stumped: fielder name input
- Runs scored on this ball input (for run outs)
- Submit button

After wicket — Next Batsman Dialog:

- Dropdown of remaining players not yet out
- Select and confirm

After over ends — Next Bowler Dialog:

- Dropdown of bowlers (cannot be same bowler as previous over)
- Bowling side selection (Over the wicket / Round the wicket)
- Select and confirm

End of innings — show innings summary popup then transition to second innings setup

### WatchLive.jsx:

- Same layout as LiveScoring but NO scoring buttons
- WebSocket connected to /topic/match/{matchKey}/score
- Auto-updates every time scorer records a ball
- Shows: score, current batsmen stats, current bowler stats, recent balls
- Read only — viewers cannot interact

### Scorecard.jsx:

Batting table:
| Batsman | How Out | R | B | 4s | 6s | SR |

Bowling table:
| Bowler | O | M | R | W | Econ |

Extras row: Wides X, No Balls X, Byes X, Leg Byes X, Total X
Total row: TeamName X/Y (Z Ov)

Toggle between Innings 1 and Innings 2 tabs

### PastMatches.jsx:

- List of completed matches created by this user
- Each shows: Team1 vs Team2, Date, Result
- Click → PastScorecard

### PastScorecard.jsx:

- Full batting and bowling scorecards for both innings
- Match result header
- Download PDF button (calls GET /api/matches/{matchKey}/report/pdf)
- Analytics tab showing charts for this match

### Analytics.jsx:

- Line chart: over-by-over run rate for selected match
- Bar chart: top 5 run scorers all time
- Bar chart: top 5 wicket takers all time
- Pie chart: runs breakdown (4s, 6s, singles, extras) for selected match
- Match selector dropdown at top

---

## PDF Report — iText 7

Generate PDF with:

1. Header: "Team1 vs Team2 | Venue | Date"
2. Match Result: bold result string
3. Innings 1 heading
4. Batting table: Batsman | How Out | R | B | 4s | 6s | SR
5. Extras row
6. Total row
7. Bowling table: Bowler | O | M | R | W | Econ
8. Innings 2 heading (same format)
9. Footer: "Generated by Cricket Scorecard App | Match Key: {matchKey}"

---

## Application Flow — Complete User Journey

### Scorer (match creator):

1. Register / Login
2. Dashboard → Create New Match
3. Fill match details → get matchKey → share with viewers
4. Add 11 players per team with roles and styles
5. Select openers and opening bowler
6. Score ball by ball using buttons
7. Handle overs, wickets, innings transition
8. Match completes → result shown → download PDF

### Viewer:

1. Register / Login
2. Dashboard → Enter Match Key
3. Enter the matchKey shared by scorer
4. See live scorecard updating in real time (WebSocket)
5. After match: same key → full scorecard + PDF download

---

## INTERVIEW_NOTES.md — Mandatory

Create this file at project root. For every technology cover:

1. What it is (one sentence)
2. Why used in this cricket project specifically
3. How it works (3-5 simple bullets)
4. One likely interview question + strong answer

Technologies to cover:

1. Spring Boot — why it simplifies Java backend
2. REST API — HTTP methods, status codes, stateless nature
3. Spring Data JPA + Hibernate — ORM, why no raw SQL
4. SQLite — why over MySQL for this project
5. JWT — token structure, stateless auth, why not sessions
6. Spring Security — filter chain, role based access
7. WebSockets + STOMP — vs HTTP, why for live cricket scores
8. iText 7 — PDF generation in Java
9. Match Key concept — how privacy is implemented without complex auth
10. React — components, hooks, state, virtual DOM
11. React Router — SPA navigation, protected routes
12. Recharts — charting, what data format it needs
13. Axios — vs fetch, interceptors for auto JWT
14. CORS — why it exists, how Spring Boot handles it
15. BCrypt — why hash passwords, what salting is
16. MVC Pattern — how Spring Boot implements it
17. DTOs — why separate from JPA entities
18. WebSocket vs REST — when to use which

Also include section: "How to explain this project in interview" — 4-5 sentences ready to say out loud.

---

## README.md

Include:

1. Project title and description
2. Tech stack
3. Features list
4. How to run backend: cd backend && mvn spring-boot:run
5. How to run frontend: cd frontend && npm install && npm start
6. Default login: HarshShringi / Harsh1234
7. How match keys work (explain in 3 lines)
8. API endpoints quick reference

---

## Build Order

1. Create backend Maven project and pom.xml
2. Write application.properties
3. Write schema.sql
4. Create all JPA entities
5. Create all repositories
6. Implement JWT security (JwtUtil, JwtFilter, SecurityConfig, CorsConfig)
7. Implement AuthService + AuthController + user seeding
8. Implement MatchService + MatchController (create match, generate matchKey, add players)
9. Implement ScoringService (complete engine with ALL business rules)
10. Implement ScoringController
11. Implement WebSocketConfig + broadcasting in ScoringService
12. Implement AnalyticsService + AnalyticsController
13. Implement PdfService + PdfController
14. Add CricketApiController stub with TODO comments
15. Initialize React with CRA
16. Set up AuthContext, Axios interceptor, React Router
17. Build LoginPage
18. Build Dashboard
19. Build EnterMatchKey page
20. Build MatchSetup (show matchKey after creation)
21. Build PlayerSetup (with role and style fields)
22. Build LiveScoring (all buttons, all dialogs)
23. Build WatchLive (WebSocket read-only view)
24. Build Scorecard
25. Build PastMatches + PastScorecard
26. Build Analytics with Recharts
27. Write INTERVIEW_NOTES.md
28. Write README.md

---

## Constraints

- No MySQL — SQLite only
- No Docker
- No Tailwind or any CSS framework — plain CSS only, one .css file per component
- No external cricket API calls — CricketApiController is a stub only
- No Kafka or any message queue
- Only one person can score a match (the creator)
- Match access is controlled by matchKey — not by user permissions on every record
- Every endpoint returns correct HTTP status codes (200, 201, 400, 401, 403, 404)
- JWT secret in application.properties only
- CORS allows http://localhost:3000
- All responses are JSON
- Add comments in ScoringService explaining each block of logic
- Free hit must be tracked and enforced (batsman cannot be out bowled/lbw/hit wicket on free hit)
