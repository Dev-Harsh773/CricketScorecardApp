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
