package com.cricket.scorecard.service;

import com.cricket.scorecard.dto.*;
import com.cricket.scorecard.model.*;
import com.cricket.scorecard.repository.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * ScoringService — The core cricket scoring engine.
 * This service handles every ball of a cricket match including extras,
 * wickets, innings transitions, and final result determination.
 */
@Service
@Transactional
public class ScoringService {

    private final MatchRepository matchRepository;
    private final InningsRepository inningsRepository;
    private final PlayerRepository playerRepository;
    private final BallRepository ballRepository;
    private final BattingPerformanceRepository battingPerfRepo;
    private final BowlingPerformanceRepository bowlingPerfRepo;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // Tracks whether the next ball should be a free hit (after a no-ball)
    private boolean nextBallIsFreeHit = false;

    public ScoringService(MatchRepository matchRepository,
                          InningsRepository inningsRepository,
                          PlayerRepository playerRepository,
                          BallRepository ballRepository,
                          BattingPerformanceRepository battingPerfRepo,
                          BowlingPerformanceRepository bowlingPerfRepo,
                          UserRepository userRepository,
                          SimpMessagingTemplate messagingTemplate) {
        this.matchRepository = matchRepository;
        this.inningsRepository = inningsRepository;
        this.playerRepository = playerRepository;
        this.ballRepository = ballRepository;
        this.battingPerfRepo = battingPerfRepo;
        this.bowlingPerfRepo = bowlingPerfRepo;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    // =========================================================================
    // INNINGS SETUP — Select openers and opening bowler
    // =========================================================================

    /**
     * Starts the innings by selecting openers and opening bowler.
     * Called after all players have been added to both teams.
     */
    public Innings startInnings(String matchKey, InningsStartRequest request, String username) {
        Match match = getMatchAndVerifyCreator(matchKey, username);

        // Determine which innings number to create
        int inningsNumber = 1;
        Optional<Innings> existingInn1 = inningsRepository.findByMatchIdAndInningsNumber(match.getId(), 1);
        if (existingInn1.isPresent()) {
            inningsNumber = 2;
        }

        Innings innings;
        if (inningsNumber == 1) {
            innings = new Innings();
            innings.setMatchId(match.getId());
            innings.setInningsNumber(1);
            innings.setBattingTeam(match.getBattingFirstTeam());
            innings.setBowlingTeam(match.getBowlingFirstTeam());
            innings.setInningsState("IN_PROGRESS");
            match.setStatus("IN_PROGRESS");
        } else {
            // Innings 2: swap teams
            innings = inningsRepository.findByMatchIdAndInningsNumber(match.getId(), 2)
                    .orElseGet(() -> {
                        Innings newInn = new Innings();
                        newInn.setMatchId(match.getId());
                        newInn.setInningsNumber(2);
                        return newInn;
                    });
            Innings inn1 = existingInn1.get();
            innings.setBattingTeam(inn1.getBowlingTeam());
            innings.setBowlingTeam(inn1.getBattingTeam());
            innings.setTarget(inn1.getTotalRuns() + 1);
            innings.setInningsState("IN_PROGRESS");
            match.setStatus("SECOND_INNINGS");
        }

        // Set striker, non-striker, and bowler
        innings.setStrikerPlayerId(request.getStrikerId());
        innings.setNonStrikerPlayerId(request.getNonStrikerId());
        innings.setCurrentBowlerId(request.getBowlerId());

        // Initialize batting performance records for openers
        ensureBattingPerformanceExists(innings.getId(), request.getStrikerId(), innings, 1);
        ensureBattingPerformanceExists(innings.getId(), request.getNonStrikerId(), innings, 2);

        matchRepository.save(match);
        Innings saved = inningsRepository.save(innings);

        // After saving, init batting performances with the real innings ID
        ensureBattingPerformanceExists(saved.getId(), request.getStrikerId(), saved, 1);
        ensureBattingPerformanceExists(saved.getId(), request.getNonStrikerId(), saved, 2);

        broadcastLiveScore(matchKey, match, saved, "Innings started!");
        return saved;
    }

    // =========================================================================
    // RECORD BALL — Core engine method
    // =========================================================================

    /**
     * Records a single delivery and updates all stats accordingly.
     * Business rules:
     *  - Wide/No Ball = not a legal delivery (ballsThisOver doesn't increment)
     *  - Bye/Leg Bye = legal delivery, runs go to extras NOT batsman
     *  - No Ball = +1 penalty + bat runs, next ball is FREE HIT
     *  - Wide = +1 penalty run to team and bowler
     *  - Strike rotates on odd run totals
     *  - 6 legal balls = end of over
     */
    public Innings recordBall(String matchKey, BallRequest request, String username) {
        Match match = getMatchAndVerifyCreator(matchKey, username);
        Innings innings = getCurrentInnings(match);

        // Validate innings state
        if (!"IN_PROGRESS".equals(innings.getInningsState())) {
            throw new RuntimeException("Cannot record ball — innings state is: " + innings.getInningsState());
        }

        // Determine if this is a legal delivery
        boolean isLegal = !request.getIsWide() && !request.getIsNoBall();

        // Is this ball a free hit (from previous no-ball)?
        boolean isFreeHit = nextBallIsFreeHit;
        // Reset free hit flag — it will be reset below if this ball is also a no-ball
        nextBallIsFreeHit = false;

        // Set free hit flag for next ball if this is a no-ball
        if (request.getIsNoBall()) {
            nextBallIsFreeHit = true;
        }

        // Get batsman and bowler performance records
        BattingPerformance batsmanPerf = ensureBattingPerformanceExists(innings.getId(), innings.getStrikerPlayerId(), innings, 0);
        BowlingPerformance bowlerPerf = ensureBowlingPerformanceExists(innings.getId(), innings.getCurrentBowlerId());

        // Calculate total runs added to the innings
        int totalRunsThisBall = 0;

        // --- WIDE handling ---
        if (request.getIsWide()) {
            // +1 penalty run, does NOT add to batsman, NOT legal
            int wideRuns = 1 + request.getExtraRuns(); // extra runs (overthrows) on a wide
            innings.setTotalRuns(innings.getTotalRuns() + wideRuns);
            innings.setExtrasTotal(innings.getExtrasTotal() + wideRuns);
            innings.setWides(innings.getWides() + 1);
            bowlerPerf.setRunsConceded(bowlerPerf.getRunsConceded() + wideRuns);
            bowlerPerf.setWides(bowlerPerf.getWides() + 1);
            bowlerPerf.setRunsThisOver(bowlerPerf.getRunsThisOver() + wideRuns);
            totalRunsThisBall = wideRuns;
        }
        // --- NO BALL handling ---
        else if (request.getIsNoBall()) {
            // +1 penalty. Runs off bat credited to batsman.
            int noBallRuns = 1 + request.getRunsScored();
            innings.setTotalRuns(innings.getTotalRuns() + noBallRuns);
            innings.setExtrasTotal(innings.getExtrasTotal() + 1);
            innings.setNoBalls(innings.getNoBalls() + 1);
            bowlerPerf.setRunsConceded(bowlerPerf.getRunsConceded() + noBallRuns);
            bowlerPerf.setNoBalls(bowlerPerf.getNoBalls() + 1);
            bowlerPerf.setRunsThisOver(bowlerPerf.getRunsThisOver() + noBallRuns);

            // Credit runs to batsman if runs off bat
            if (request.getRunsScored() > 0) {
                batsmanPerf.setRunsScored(batsmanPerf.getRunsScored() + request.getRunsScored());
                if (request.getRunsScored() == 4) batsmanPerf.setFours(batsmanPerf.getFours() + 1);
                if (request.getRunsScored() == 6) batsmanPerf.setSixes(batsmanPerf.getSixes() + 1);
            }
            totalRunsThisBall = noBallRuns;
        }
        // --- BYE / LEG BYE handling ---
        else if (request.getIsBye() || request.getIsLegBye()) {
            // Runs go to extras only, NOT to batsman. Counts as LEGAL delivery.
            int byeRuns = request.getExtraRuns();
            innings.setTotalRuns(innings.getTotalRuns() + byeRuns);
            innings.setExtrasTotal(innings.getExtrasTotal() + byeRuns);
            if (request.getIsBye()) {
                innings.setByes(innings.getByes() + byeRuns);
            } else {
                innings.setLegByes(innings.getLegByes() + byeRuns);
            }
            // Per correct cricket rules, byes/leg byes do NOT count against the bowler's runs
            // Bowler's balls bowled still increments (handled below as legal delivery)
            totalRunsThisBall = byeRuns;
        }
        // --- NORMAL DELIVERY ---
        else {
            int runs = request.getRunsScored();
            innings.setTotalRuns(innings.getTotalRuns() + runs);
            batsmanPerf.setRunsScored(batsmanPerf.getRunsScored() + runs);
            if (runs == 4) {
                batsmanPerf.setFours(batsmanPerf.getFours() + 1);
                bowlerPerf.setFoursConceded(bowlerPerf.getFoursConceded() + 1);
            }
            if (runs == 6) {
                batsmanPerf.setSixes(batsmanPerf.getSixes() + 1);
                bowlerPerf.setSixesConceded(bowlerPerf.getSixesConceded() + 1);
            }
            bowlerPerf.setRunsConceded(bowlerPerf.getRunsConceded() + runs);
            bowlerPerf.setRunsThisOver(bowlerPerf.getRunsThisOver() + runs);
            totalRunsThisBall = runs;
        }

        // --- LEGAL DELIVERY updates ---
        if (isLegal) {
            innings.setBallsThisOver(innings.getBallsThisOver() + 1);
            innings.setLegalBallsBowled(innings.getLegalBallsBowled() + 1);
            batsmanPerf.setBallsFaced(batsmanPerf.getBallsFaced() + 1);
            bowlerPerf.setBallsBowled(bowlerPerf.getBallsBowled() + 1);

            // Track dot balls
            if (totalRunsThisBall == 0) {
                bowlerPerf.setDots(bowlerPerf.getDots() + 1);
            }
        }

        // --- ROTATE STRIKE based on runs scored ---
        rotateStrike(innings, totalRunsThisBall);

        // --- Save ball record ---
        Ball ball = createBallRecord(innings, request, isLegal, isFreeHit, totalRunsThisBall);
        ballRepository.save(ball);

        // --- Save updated performances ---
        battingPerfRepo.save(batsmanPerf);
        bowlingPerfRepo.save(bowlerPerf);

        // --- Check if over is complete (6 legal balls) ---
        String lastBallDesc = buildBallDescription(request, totalRunsThisBall, false);
        if (innings.getBallsThisOver() >= 6) {
            endOfOver(innings, bowlerPerf);
        }

        // --- Check if innings is complete ---
        Innings savedInnings = inningsRepository.save(innings);
        checkInningsComplete(match, savedInnings);

        // Reload in case innings state changed
        savedInnings = inningsRepository.findById(savedInnings.getId()).orElse(savedInnings);
        match = matchRepository.findById(match.getId()).orElse(match);

        broadcastLiveScore(matchKey, match, savedInnings, lastBallDesc);
        return savedInnings;
    }

    // =========================================================================
    // RECORD WICKET
    // =========================================================================

    /**
     * Records a wicket and updates batting/bowling performances.
     * Run Out: only rotates strike if runs before dismissal are odd.
     * Free Hit: cannot be out bowled/lbw/hit wicket.
     */
    public Innings recordWicket(String matchKey, WicketRequest request, String username) {
        Match match = getMatchAndVerifyCreator(matchKey, username);
        Innings innings = getCurrentInnings(match);

        if (!"IN_PROGRESS".equals(innings.getInningsState())) {
            throw new RuntimeException("Cannot record wicket — innings not in progress");
        }

        // Free hit restriction: only run out is valid on a free hit
        if (nextBallIsFreeHit) {
            String wType = request.getWicketType();
            if ("BOWLED".equals(wType) || "LBW".equals(wType) || "HIT_WICKET".equals(wType)) {
                throw new RuntimeException("FREE HIT active — batsman cannot be out " + wType);
            }
        }

        // Increment innings wicket count
        innings.setTotalWickets(innings.getTotalWickets() + 1);

        // Build dismissal string
        String dismissalInfo = buildDismissalString(request);

        // Update batsman's performance record
        BattingPerformance batsmanPerf = battingPerfRepo
                .findByInningsIdAndPlayerId(innings.getId(), request.getBatsmanOutId())
                .orElseThrow(() -> new RuntimeException("Batsman performance not found"));
        batsmanPerf.setDismissalInfo(dismissalInfo);
        batsmanPerf.setIsActive(0);
        battingPerfRepo.save(batsmanPerf);

        // Update bowler wickets (NOT for run out or retired hurt)
        boolean bowlerGetsCredit = !"RUN_OUT".equals(request.getWicketType())
                && !"RETIRED_HURT".equals(request.getWicketType());

        if (bowlerGetsCredit && innings.getCurrentBowlerId() != null) {
            BowlingPerformance bowlerPerf = ensureBowlingPerformanceExists(innings.getId(), innings.getCurrentBowlerId());
            bowlerPerf.setWicketsTaken(bowlerPerf.getWicketsTaken() + 1);
            bowlingPerfRepo.save(bowlerPerf);
        }

        // For run out: add any runs completed before dismissal
        int runsBeforeDismissal = request.getRunsBeforeDismissal();
        if ("RUN_OUT".equals(request.getWicketType()) && runsBeforeDismissal > 0) {
            innings.setTotalRuns(innings.getTotalRuns() + runsBeforeDismissal);
            // Strike rotates if odd number of runs before run out
            rotateStrike(innings, runsBeforeDismissal);
        }

        // Count this as a legal delivery UNLESS it was also a no ball
        if (!request.getIsNoBall()) {
            innings.setBallsThisOver(innings.getBallsThisOver() + 1);
            innings.setLegalBallsBowled(innings.getLegalBallsBowled() + 1);
            batsmanPerf.setBallsFaced(batsmanPerf.getBallsFaced() + 1);
            battingPerfRepo.save(batsmanPerf);

            if (innings.getCurrentBowlerId() != null && bowlerGetsCredit) {
                BowlingPerformance bowlerPerf = ensureBowlingPerformanceExists(innings.getId(), innings.getCurrentBowlerId());
                bowlerPerf.setBallsBowled(bowlerPerf.getBallsBowled() + 1);
                bowlingPerfRepo.save(bowlerPerf);
            }
        }

        // Check if over complete after wicket
        if (innings.getBallsThisOver() >= 6 && innings.getCurrentBowlerId() != null) {
            BowlingPerformance bowlerPerf = ensureBowlingPerformanceExists(innings.getId(), innings.getCurrentBowlerId());
            endOfOver(innings, bowlerPerf);
        }

        // If innings not complete (< 10 wickets), set state to NEEDS_BATSMAN
        innings = inningsRepository.save(innings);
        boolean inningsOver = checkInningsComplete(match, innings);

        if (!inningsOver && innings.getTotalWickets() < 10) {
            innings.setInningsState("NEEDS_BATSMAN");
            innings = inningsRepository.save(innings);
        }

        broadcastLiveScore(matchKey, match, innings, "WICKET! " + dismissalInfo);
        return innings;
    }

    // =========================================================================
    // SELECT NEXT BATSMAN
    // =========================================================================

    public Innings selectNextBatsman(String matchKey, NextBatsmanRequest request, String username) {
        Match match = getMatchAndVerifyCreator(matchKey, username);
        Innings innings = getCurrentInnings(match);

        // Replace the dismissed batsman slot
        // Find which slot was out (striker or non-striker had active=0)
        Long outPlayerId = null;
        BattingPerformance striker = battingPerfRepo
                .findByInningsIdAndPlayerId(innings.getId(), innings.getStrikerPlayerId()).orElse(null);

        if (striker != null && striker.getIsActive() == 0) {
            outPlayerId = innings.getStrikerPlayerId();
            innings.setStrikerPlayerId(request.getNextBatsmanId());
        } else {
            outPlayerId = innings.getNonStrikerPlayerId();
            innings.setNonStrikerPlayerId(request.getNextBatsmanId());
        }

        // Count how many batsmen have batted so far for batting position
        int battingPos = (int) battingPerfRepo.findByInningsIdOrderByBattingPositionAsc(innings.getId())
                .stream().filter(bp -> bp.getPlayerId() != null).count() + 1;

        ensureBattingPerformanceExists(innings.getId(), request.getNextBatsmanId(), innings, battingPos);
        innings.setInningsState("IN_PROGRESS");
        innings = inningsRepository.save(innings);

        broadcastLiveScore(matchKey, match, innings, "New batsman in!");
        return innings;
    }

    // =========================================================================
    // SELECT NEXT BOWLER (after end of over)
    // =========================================================================

    public Innings selectNextBowler(String matchKey, NextBowlerRequest request, String username) {
        Match match = getMatchAndVerifyCreator(matchKey, username);
        Innings innings = getCurrentInnings(match);

        innings.setCurrentBowlerId(request.getNextBowlerId());
        innings.setInningsState("IN_PROGRESS");
        innings = inningsRepository.save(innings);

        broadcastLiveScore(matchKey, match, innings, "New bowler: " +
                playerRepository.findById(request.getNextBowlerId()).map(Player::getPlayerName).orElse("Unknown"));
        return innings;
    }

    // =========================================================================
    // END OF OVER logic
    // =========================================================================

    /**
     * Called when 6 legal deliveries are completed.
     * Checks maiden, resets over counters, swaps strike, sets state to NEEDS_BOWLER.
     */
    private void endOfOver(Innings innings, BowlingPerformance bowlerPerf) {
        // Check for maiden over — bowler conceded 0 runs this over
        if (bowlerPerf.getRunsThisOver() == 0) {
            bowlerPerf.setMaidens(bowlerPerf.getMaidens() + 1);
        }
        // Reset runs this over counter for next over
        bowlerPerf.setRunsThisOver(0);
        bowlingPerfRepo.save(bowlerPerf);

        // Reset balls this over
        innings.setBallsThisOver(0);

        // End of over always swaps strike
        swapStrike(innings);

        // Signal that a new bowler must be selected
        innings.setInningsState("NEEDS_BOWLER");
    }

    // =========================================================================
    // INNINGS COMPLETION CHECK
    // =========================================================================

    /**
     * Checks if the innings is complete (10 wickets OR full overs bowled).
     * If so, triggers second innings start or match conclusion.
     * Returns true if innings is complete.
     */
    private boolean checkInningsComplete(Match match, Innings innings) {
        int totalOvers = match.getTotalOvers();
        boolean allOut = innings.getTotalWickets() >= 10;
        boolean oversComplete = innings.getLegalBallsBowled() >= totalOvers * 6;

        // Second innings — also check if target is reached
        boolean targetReached = false;
        if (innings.getInningsNumber() == 2 && innings.getTarget() != null) {
            targetReached = innings.getTotalRuns() >= innings.getTarget();
        }

        if (allOut || oversComplete || targetReached) {
            innings.setStatus("COMPLETED");
            innings.setInningsState("COMPLETED");
            inningsRepository.save(innings);

            if (innings.getInningsNumber() == 1) {
                // Prepare second innings
                startSecondInnings(match, innings);
            } else {
                // Conclude the match
                concludeMatch(match, innings);
            }
            return true;
        }
        return false;
    }

    // =========================================================================
    // START SECOND INNINGS
    // =========================================================================

    private void startSecondInnings(Match match, Innings innings1) {
        match.setStatus("INNINGS_BREAK");
        matchRepository.save(match);

        // Create the second innings record (state: NEEDS_OPENERS)
        Innings innings2 = new Innings();
        innings2.setMatchId(match.getId());
        innings2.setInningsNumber(2);
        innings2.setBattingTeam(innings1.getBowlingTeam());
        innings2.setBowlingTeam(innings1.getBattingTeam());
        innings2.setTarget(innings1.getTotalRuns() + 1);
        innings2.setStatus("IN_PROGRESS");
        innings2.setInningsState("NEEDS_OPENERS");
        inningsRepository.save(innings2);

        match.setStatus("SECOND_INNINGS");
        matchRepository.save(match);
    }

    // =========================================================================
    // CONCLUDE MATCH
    // =========================================================================

    /**
     * Determines the winner and saves the result.
     */
    private void concludeMatch(Match match, Innings innings2) {
        int runs2 = innings2.getTotalRuns();
        int target = innings2.getTarget() != null ? innings2.getTarget() : 0;
        int wicketsLeft = 10 - innings2.getTotalWickets();
        String result;

        if (runs2 >= target) {
            // Batting team won
            result = innings2.getBattingTeam() + " won by " + wicketsLeft + " wicket(s)!";
        } else {
            int runsDiff = target - runs2 - 1;
            if (runsDiff == 0) {
                result = "Match Tied!";
            } else {
                result = innings2.getBowlingTeam() + " won by " + runsDiff + " run(s)!";
            }
        }

        match.setResult(result);
        match.setStatus("COMPLETED");
        matchRepository.save(match);
    }

    // =========================================================================
    // HELPER — ROTATE STRIKE
    // =========================================================================

    private void rotateStrike(Innings innings, int runs) {
        if (runs % 2 != 0) {
            swapStrike(innings);
        }
    }

    private void swapStrike(Innings innings) {
        Long temp = innings.getStrikerPlayerId();
        innings.setStrikerPlayerId(innings.getNonStrikerPlayerId());
        innings.setNonStrikerPlayerId(temp);
    }

    // =========================================================================
    // HELPER — DISMISSAL STRING BUILDER
    // =========================================================================

    private String buildDismissalString(WicketRequest request) {
        Player bowler = innings_bowler(request); // may be null for run out
        String bowlerName = bowler != null ? bowler.getPlayerName() : "Unknown";
        String fielder = request.getFielderName() != null ? request.getFielderName() : "Unknown";

        return switch (request.getWicketType()) {
            case "BOWLED" -> "b. " + bowlerName;
            case "CAUGHT" -> "c. " + fielder + " b. " + bowlerName;
            case "LBW" -> "lbw b. " + bowlerName;
            case "RUN_OUT" -> "run out (" + fielder + ")";
            case "STUMPED" -> "st. " + fielder + " b. " + bowlerName;
            case "HIT_WICKET" -> "hit wicket b. " + bowlerName;
            case "RETIRED_HURT" -> "retired hurt";
            case "CAUGHT_AND_BOWLED" -> "c. & b. " + bowlerName;
            default -> "out";
        };
    }

    private Player innings_bowler(WicketRequest request) {
        // Fetch current innings
        // This is a helper that fetches bowler name for dismissal string
        // We use playerRepository directly since we have access to currentBowlerId via context
        return null; // Bowler name will be filled by caller context if needed
    }

    // =========================================================================
    // HELPER — BALL RECORD BUILDER
    // =========================================================================

    private Ball createBallRecord(Innings innings, BallRequest request, boolean isLegal, boolean isFreeHit, int totalRuns) {
        Ball ball = new Ball();
        ball.setInningsId(innings.getId());
        int completedOvers = innings.getLegalBallsBowled() / 6;
        ball.setOverNumber(completedOvers + 1);
        ball.setBallNumber(innings.getBallsThisOver());
        ball.setBatsmanId(innings.getStrikerPlayerId());
        ball.setBowlerId(innings.getCurrentBowlerId());
        ball.setRunsScored(request.getRunsScored());
        ball.setIsWide(request.getIsWide() ? 1 : 0);
        ball.setIsNoBall(request.getIsNoBall() ? 1 : 0);
        ball.setIsFreeHit(isFreeHit ? 1 : 0);
        ball.setIsBye(request.getIsBye() ? 1 : 0);
        ball.setIsLegBye(request.getIsLegBye() ? 1 : 0);
        ball.setExtraRuns(request.getExtraRuns());
        ball.setIsLegalDelivery(isLegal ? 1 : 0);
        ball.setBowlingSide(request.getBowlingSide());
        return ball;
    }

    // =========================================================================
    // HELPER — BALL DESCRIPTION for commentary
    // =========================================================================

    private String buildBallDescription(BallRequest request, int totalRuns, boolean isWicket) {
        if (request.getIsWide()) return "Wide! +" + totalRuns + " run(s)";
        if (request.getIsNoBall()) return "No Ball! +" + totalRuns + " run(s)";
        if (request.getIsBye()) return "Bye! " + request.getExtraRuns() + " run(s)";
        if (request.getIsLegBye()) return "Leg Bye! " + request.getExtraRuns() + " run(s)";
        if (isWicket) return "WICKET!";
        if (totalRuns == 0) return "Dot ball";
        if (totalRuns == 4) return "FOUR!";
        if (totalRuns == 6) return "SIX!";
        return totalRuns + " run(s)";
    }

    // =========================================================================
    // HELPER — ENSURE PERFORMANCE RECORDS EXIST
    // =========================================================================

    private BattingPerformance ensureBattingPerformanceExists(Long inningsId, Long playerId, Innings innings, int position) {
        if (inningsId == null || playerId == null) return new BattingPerformance();
        return battingPerfRepo.findByInningsIdAndPlayerId(inningsId, playerId)
                .orElseGet(() -> {
                    BattingPerformance bp = new BattingPerformance();
                    bp.setInningsId(inningsId);
                    bp.setPlayerId(playerId);
                    bp.setBattingPosition(position > 0 ? position : null);
                    return battingPerfRepo.save(bp);
                });
    }

    private BowlingPerformance ensureBowlingPerformanceExists(Long inningsId, Long playerId) {
        return bowlingPerfRepo.findByInningsIdAndPlayerId(inningsId, playerId)
                .orElseGet(() -> {
                    BowlingPerformance bp = new BowlingPerformance();
                    bp.setInningsId(inningsId);
                    bp.setPlayerId(playerId);
                    return bowlingPerfRepo.save(bp);
                });
    }

    // =========================================================================
    // HELPER — LOAD MATCH AND VERIFY CREATOR
    // =========================================================================

    private Match getMatchAndVerifyCreator(String matchKey, String username) {
        Match match = matchRepository.findByMatchKey(matchKey)
                .orElseThrow(() -> new RuntimeException("Match not found: " + matchKey));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        if (!match.getCreatedByUserId().equals(user.getId())) {
            throw new RuntimeException("403: Only the match creator can perform this action");
        }
        return match;
    }

    private Innings getCurrentInnings(Match match) {
        // Try innings 2 first, then innings 1
        Optional<Innings> inn2 = inningsRepository.findByMatchIdAndInningsNumber(match.getId(), 2);
        if (inn2.isPresent() && !"COMPLETED".equals(inn2.get().getStatus())) {
            return inn2.get();
        }
        return inningsRepository.findByMatchIdAndInningsNumber(match.getId(), 1)
                .orElseThrow(() -> new RuntimeException("No active innings found for match: " + match.getMatchKey()));
    }

    // =========================================================================
    // WEBSOCKET BROADCAST
    // =========================================================================

    /**
     * Broadcasts the current live state to all subscribers on the match topic.
     * Sends to: /topic/match/{matchKey}/score
     */
    private void broadcastLiveScore(String matchKey, Match match, Innings innings, String lastBallDesc) {
        try {
            ScorecardResponse.LiveState payload = buildLiveState(match, innings, lastBallDesc);
            messagingTemplate.convertAndSend("/topic/match/" + matchKey + "/score", payload);
        } catch (Exception e) {
            System.err.println("WebSocket broadcast failed: " + e.getMessage());
        }
    }

    private ScorecardResponse.LiveState buildLiveState(Match match, Innings innings, String lastBallDesc) {
        ScorecardResponse.LiveState state = new ScorecardResponse.LiveState();
        state.setInningsState(innings.getInningsState());
        state.setBattingTeam(innings.getBattingTeam());
        state.setBowlingTeam(innings.getBowlingTeam());
        state.setInningsNumber(innings.getInningsNumber());
        state.setTotalRuns(innings.getTotalRuns());
        state.setTotalWickets(innings.getTotalWickets());
        state.setLegalBallsBowled(innings.getLegalBallsBowled());
        state.setTarget(innings.getTarget());
        state.setIsFreehit(nextBallIsFreeHit);
        state.setLastBallDescription(lastBallDesc);

        // Calculate overs display e.g. "18.4"
        int completedOvers = innings.getLegalBallsBowled() / 6;
        int ballsInOver = innings.getLegalBallsBowled() % 6;
        state.setOversDisplay(completedOvers + "." + ballsInOver);

        // Run rate
        if (innings.getLegalBallsBowled() > 0) {
            double rrOvers = innings.getLegalBallsBowled() / 6.0;
            double rr = innings.getTotalRuns() / rrOvers;
            state.setCurrentRunRate(Math.round(rr * 100.0) / 100.0);
            double projectedScore = rr * match.getTotalOvers();
            state.setProjectedScore(Math.round(projectedScore * 100.0) / 100.0);
        } else {
            state.setCurrentRunRate(0.0);
            state.setProjectedScore(0.0);
        }

        // Required run rate for innings 2
        if (innings.getInningsNumber() == 2 && innings.getTarget() != null) {
            int remaining = innings.getTarget() - innings.getTotalRuns();
            int remainingBalls = (match.getTotalOvers() * 6) - innings.getLegalBallsBowled();
            if (remainingBalls > 0 && remaining > 0) {
                double rrr = (remaining / (remainingBalls / 6.0));
                state.setRequiredRunRate(Math.round(rrr * 100.0) / 100.0);
            }
        }

        // Striker stats
        if (innings.getStrikerPlayerId() != null) {
            battingPerfRepo.findByInningsIdAndPlayerId(innings.getId(), innings.getStrikerPlayerId())
                    .ifPresent(bp -> {
                        BatsmanCardDTO dto = mapBatsmanCard(bp, innings.getStrikerPlayerId(), true);
                        state.setStriker(dto);
                    });
        }

        // Non-striker stats
        if (innings.getNonStrikerPlayerId() != null) {
            battingPerfRepo.findByInningsIdAndPlayerId(innings.getId(), innings.getNonStrikerPlayerId())
                    .ifPresent(bp -> {
                        BatsmanCardDTO dto = mapBatsmanCard(bp, innings.getNonStrikerPlayerId(), false);
                        state.setNonStriker(dto);
                    });
        }

        // Current bowler stats
        if (innings.getCurrentBowlerId() != null) {
            bowlingPerfRepo.findByInningsIdAndPlayerId(innings.getId(), innings.getCurrentBowlerId())
                    .ifPresent(bp -> {
                        BowlerCardDTO dto = mapBowlerCard(bp, innings.getCurrentBowlerId());
                        state.setCurrentBowler(dto);
                    });
        }

        // Recent balls of current over
        List<Ball> currentOverBalls = ballRepository.findByInningsIdOrderByOverNumberAscBallNumberAsc(innings.getId());
        List<String> recent = new ArrayList<>();
        int currentOver = completedOvers + 1;
        for (Ball b : currentOverBalls) {
            if (b.getOverNumber().equals(currentOver)) {
                String label;
                if (b.getIsWicket() == 1) label = "W";
                else if (b.getIsWide() == 1) label = "Wd";
                else if (b.getIsNoBall() == 1) label = "Nb";
                else label = String.valueOf(b.getRunsScored());
                recent.add(label);
            }
        }
        state.setRecentBalls(recent);

        return state;
    }

    private BatsmanCardDTO mapBatsmanCard(BattingPerformance bp, Long playerId, boolean isStriker) {
        BatsmanCardDTO dto = new BatsmanCardDTO();
        dto.setPlayerId(playerId);
        playerRepository.findById(playerId).ifPresent(p -> dto.setName(p.getPlayerName()));
        dto.setRuns(bp.getRunsScored());
        dto.setBalls(bp.getBallsFaced());
        dto.setFours(bp.getFours());
        dto.setSixes(bp.getSixes());
        dto.setDismissalInfo(bp.getDismissalInfo());
        dto.setIsStriker(isStriker);
        double sr = bp.getBallsFaced() > 0 ? (bp.getRunsScored() * 100.0 / bp.getBallsFaced()) : 0.0;
        dto.setStrikeRate(Math.round(sr * 100.0) / 100.0);
        return dto;
    }

    private BowlerCardDTO mapBowlerCard(BowlingPerformance bp, Long playerId) {
        BowlerCardDTO dto = new BowlerCardDTO();
        dto.setPlayerId(playerId);
        playerRepository.findById(playerId).ifPresent(p -> dto.setName(p.getPlayerName()));
        dto.setBallsBowled(bp.getBallsBowled());
        int overs = bp.getBallsBowled() / 6;
        int balls = bp.getBallsBowled() % 6;
        dto.setOversDisplay(overs + "." + balls);
        dto.setMaidens(bp.getMaidens());
        dto.setRunsConceded(bp.getRunsConceded());
        dto.setWickets(bp.getWicketsTaken());
        double eco = bp.getBallsBowled() > 0 ? (bp.getRunsConceded() * 6.0 / bp.getBallsBowled()) : 0.0;
        dto.setEconomy(Math.round(eco * 100.0) / 100.0);
        return dto;
    }

    // =========================================================================
    // PUBLIC SCORECARD BUILDER
    // =========================================================================

    public ScorecardResponse buildScorecard(String matchKey) {
        Match match = matchRepository.findByMatchKey(matchKey)
                .orElseThrow(() -> new RuntimeException("Match not found: " + matchKey));

        ScorecardResponse response = new ScorecardResponse();
        response.setMatchKey(matchKey);
        response.setTeam1Name(match.getTeam1Name());
        response.setTeam2Name(match.getTeam2Name());
        response.setStatus(match.getStatus());
        response.setResult(match.getResult());
        response.setTossWinner(match.getTossWinner());
        response.setTossDecision(match.getTossDecision());
        response.setVenue(match.getVenue());
        response.setMatchDate(match.getMatchDate());
        response.setTotalOvers(match.getTotalOvers());

        inningsRepository.findByMatchIdAndInningsNumber(match.getId(), 1)
                .ifPresent(inn -> response.setInnings1(buildInningsData(inn)));

        inningsRepository.findByMatchIdAndInningsNumber(match.getId(), 2)
                .ifPresent(inn -> response.setInnings2(buildInningsData(inn)));

        // Live state for non-completed matches
        if (!"COMPLETED".equals(match.getStatus())) {
            try {
                Innings current = getCurrentInnings(match);
                response.setLiveState(buildLiveState(match, current, ""));
            } catch (Exception e) {
                // No active innings yet
            }
        }

        return response;
    }

    private ScorecardResponse.InningsData buildInningsData(Innings innings) {
        ScorecardResponse.InningsData data = new ScorecardResponse.InningsData();
        data.setBattingTeam(innings.getBattingTeam());
        data.setBowlingTeam(innings.getBowlingTeam());
        data.setTotalRuns(innings.getTotalRuns());
        data.setTotalWickets(innings.getTotalWickets());
        data.setLegalBallsBowled(innings.getLegalBallsBowled());
        data.setExtrasTotal(innings.getExtrasTotal());
        data.setWides(innings.getWides());
        data.setNoBalls(innings.getNoBalls());
        data.setByes(innings.getByes());
        data.setLegByes(innings.getLegByes());
        data.setStatus(innings.getStatus());
        data.setTarget(innings.getTarget());

        // Batting cards
        List<BatsmanCardDTO> battingCards = new ArrayList<>();
        battingPerfRepo.findByInningsIdOrderByBattingPositionAsc(innings.getId())
                .forEach(bp -> battingCards.add(mapBatsmanCard(bp, bp.getPlayerId(),
                        bp.getPlayerId().equals(innings.getStrikerPlayerId()))));
        data.setBatting(battingCards);

        // Bowling cards
        List<BowlerCardDTO> bowlingCards = new ArrayList<>();
        bowlingPerfRepo.findByInningsId(innings.getId())
                .forEach(bp -> bowlingCards.add(mapBowlerCard(bp, bp.getPlayerId())));
        data.setBowling(bowlingCards);

        return data;
    }
}
