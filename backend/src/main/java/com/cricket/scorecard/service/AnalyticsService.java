package com.cricket.scorecard.service;

import com.cricket.scorecard.model.Ball;
import com.cricket.scorecard.model.Innings;
import com.cricket.scorecard.model.Match;
import com.cricket.scorecard.repository.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AnalyticsService {

    private final MatchRepository matchRepository;
    private final InningsRepository inningsRepository;
    private final BallRepository ballRepository;
    private final BattingPerformanceRepository battingPerfRepo;
    private final BowlingPerformanceRepository bowlingPerfRepo;
    private final PlayerRepository playerRepository;

    public AnalyticsService(MatchRepository matchRepository, InningsRepository inningsRepository,
                             BallRepository ballRepository, BattingPerformanceRepository battingPerfRepo,
                             BowlingPerformanceRepository bowlingPerfRepo, PlayerRepository playerRepository) {
        this.matchRepository = matchRepository;
        this.inningsRepository = inningsRepository;
        this.ballRepository = ballRepository;
        this.battingPerfRepo = battingPerfRepo;
        this.bowlingPerfRepo = bowlingPerfRepo;
        this.playerRepository = playerRepository;
    }

    /** Over-by-over run rate data for a specific match */
    public List<Map<String, Object>> getRunRateData(String matchKey) {
        Match match = matchRepository.findByMatchKey(matchKey)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        List<Map<String, Object>> result = new ArrayList<>();

        for (int inningsNum = 1; inningsNum <= 2; inningsNum++) {
            int innNum = inningsNum;
            inningsRepository.findByMatchIdAndInningsNumber(match.getId(), inningsNum)
                    .ifPresent(innings -> {
                        List<Ball> balls = ballRepository.findByInningsIdOrderByOverNumberAscBallNumberAsc(innings.getId());
                        Map<Integer, List<Ball>> byOver = new LinkedHashMap<>();
                        for (Ball ball : balls) {
                            byOver.computeIfAbsent(ball.getOverNumber(), k -> new ArrayList<>()).add(ball);
                        }

                        int runningTotal = 0;
                        for (Map.Entry<Integer, List<Ball>> entry : byOver.entrySet()) {
                            int overRuns = entry.getValue().stream().mapToInt(b -> b.getRunsScored() + b.getExtraRuns()).sum();
                            runningTotal += overRuns;
                            double rr = runningTotal / (double) entry.getKey();

                            Map<String, Object> point = new HashMap<>();
                            point.put("innings", innNum);
                            point.put("over", entry.getKey());
                            point.put("runsThisOver", overRuns);
                            point.put("totalRuns", runningTotal);
                            point.put("runRate", Math.round(rr * 100.0) / 100.0);
                            result.add(point);
                        }
                    });
        }
        return result;
    }

    /** Match summary statistics */
    public Map<String, Object> getMatchSummary(String matchKey) {
        Match match = matchRepository.findByMatchKey(matchKey)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("matchKey", matchKey);
        summary.put("result", match.getResult());
        summary.put("status", match.getStatus());

        inningsRepository.findByMatchIdAndInningsNumber(match.getId(), 1)
                .ifPresent(inn -> summary.put("innings1", Map.of(
                        "team", inn.getBattingTeam(),
                        "runs", inn.getTotalRuns(),
                        "wickets", inn.getTotalWickets(),
                        "overs", inn.getLegalBallsBowled() / 6 + "." + inn.getLegalBallsBowled() % 6
                )));

        inningsRepository.findByMatchIdAndInningsNumber(match.getId(), 2)
                .ifPresent(inn -> summary.put("innings2", Map.of(
                        "team", inn.getBattingTeam(),
                        "runs", inn.getTotalRuns(),
                        "wickets", inn.getTotalWickets(),
                        "overs", inn.getLegalBallsBowled() / 6 + "." + inn.getLegalBallsBowled() % 6
                )));

        return summary;
    }

    /** All-time top 5 run scorers across all matches */
    public List<Map<String, Object>> getTopScorers() {
        List<Map<String, Object>> result = new ArrayList<>();
        battingPerfRepo.findAll().stream()
                .sorted((a, b) -> b.getRunsScored() - a.getRunsScored())
                .limit(10)
                .forEach(bp -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    playerRepository.findById(bp.getPlayerId())
                            .ifPresent(p -> row.put("playerName", p.getPlayerName()));
                    row.put("runs", bp.getRunsScored());
                    row.put("balls", bp.getBallsFaced());
                    row.put("fours", bp.getFours());
                    row.put("sixes", bp.getSixes());
                    result.add(row);
                });
        return result;
    }

    /** All-time top 5 wicket takers across all matches */
    public List<Map<String, Object>> getTopBowlers() {
        List<Map<String, Object>> result = new ArrayList<>();
        bowlingPerfRepo.findAll().stream()
                .sorted((a, b) -> b.getWicketsTaken() - a.getWicketsTaken())
                .limit(10)
                .forEach(bp -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    playerRepository.findById(bp.getPlayerId())
                            .ifPresent(p -> row.put("playerName", p.getPlayerName()));
                    row.put("wickets", bp.getWicketsTaken());
                    row.put("runs", bp.getRunsConceded());
                    row.put("maidens", bp.getMaidens());
                    result.add(row);
                });
        return result;
    }

    public List<Map<String, Object>> getTopScorersByMatch(String matchKey) {
        Match match = matchRepository.findByMatchKey(matchKey)
                .orElseThrow(() -> new RuntimeException("Match not found"));
        
        List<Map<String, Object>> result = new ArrayList<>();
        List<com.cricket.scorecard.model.BattingPerformance> allPerformances = new ArrayList<>();
        
        inningsRepository.findByMatchIdAndInningsNumber(match.getId(), 1)
                .ifPresent(inn -> allPerformances.addAll(battingPerfRepo.findByInningsIdOrderByBattingPositionAsc(inn.getId())));
        inningsRepository.findByMatchIdAndInningsNumber(match.getId(), 2)
                .ifPresent(inn -> allPerformances.addAll(battingPerfRepo.findByInningsIdOrderByBattingPositionAsc(inn.getId())));
        
        allPerformances.stream()
                .filter(bp -> bp.getPlayerId() != null)
                .sorted((a, b) -> b.getRunsScored() - a.getRunsScored())
                .limit(10)
                .forEach(bp -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    playerRepository.findById(bp.getPlayerId())
                            .ifPresent(p -> row.put("playerName", p.getPlayerName()));
                    row.put("runs", bp.getRunsScored());
                    row.put("balls", bp.getBallsFaced());
                    row.put("fours", bp.getFours());
                    row.put("sixes", bp.getSixes());
                    result.add(row);
                });
        return result;
    }

    public List<Map<String, Object>> getTopBowlersByMatch(String matchKey) {
        Match match = matchRepository.findByMatchKey(matchKey)
                .orElseThrow(() -> new RuntimeException("Match not found"));
                
        List<Map<String, Object>> result = new ArrayList<>();
        List<com.cricket.scorecard.model.BowlingPerformance> allPerformances = new ArrayList<>();
        
        inningsRepository.findByMatchIdAndInningsNumber(match.getId(), 1)
                .ifPresent(inn -> allPerformances.addAll(bowlingPerfRepo.findByInningsId(inn.getId())));
        inningsRepository.findByMatchIdAndInningsNumber(match.getId(), 2)
                .ifPresent(inn -> allPerformances.addAll(bowlingPerfRepo.findByInningsId(inn.getId())));
                
        allPerformances.stream()
                .filter(bp -> bp.getPlayerId() != null)
                .sorted((a, b) -> b.getWicketsTaken() - a.getWicketsTaken())
                .limit(10)
                .forEach(bp -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    playerRepository.findById(bp.getPlayerId())
                            .ifPresent(p -> row.put("playerName", p.getPlayerName()));
                    row.put("wickets", bp.getWicketsTaken());
                    row.put("runs", bp.getRunsConceded());
                    row.put("maidens", bp.getMaidens());
                    result.add(row);
                });
        return result;
    }
}
