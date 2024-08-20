package com.cricket.scorecard.controller;

import com.cricket.scorecard.dto.*;
import com.cricket.scorecard.model.*;
import com.cricket.scorecard.repository.*;
import com.cricket.scorecard.service.MatchService;
import com.cricket.scorecard.service.ScoringService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchService matchService;
    private final ScoringService scoringService;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;

    public MatchController(MatchService matchService, ScoringService scoringService,
                           MatchRepository matchRepository, UserRepository userRepository,
                           PlayerRepository playerRepository) {
        this.matchService = matchService;
        this.scoringService = scoringService;
        this.matchRepository = matchRepository;
        this.userRepository = userRepository;
        this.playerRepository = playerRepository;
    }

    // POST /api/matches — Create a new match
    @PostMapping
    public ResponseEntity<Match> createMatch(@RequestBody MatchCreateRequest request,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        Match match = matchService.createMatch(request, userDetails.getUsername());
        return ResponseEntity.status(201).body(match);
    }

    // POST /api/matches/{matchKey}/players — Add players to both teams
    @PostMapping("/{matchKey}/players")
    public ResponseEntity<Map<String, String>> addPlayers(@PathVariable String matchKey,
                                                           @RequestBody List<PlayerRequest> players,
                                                           @AuthenticationPrincipal UserDetails userDetails) {
        matchService.addPlayers(matchKey, players, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("message", "Players added successfully"));
    }

    // GET /api/matches/{matchKey}/players — Get all players for a match
    @GetMapping("/{matchKey}/players")
    public ResponseEntity<List<Player>> getPlayers(@PathVariable String matchKey) {
        Match match = matchRepository.findByMatchKey(matchKey)
                .orElseThrow(() -> new RuntimeException("Match not found"));
        return ResponseEntity.ok(playerRepository.findByMatchId(match.getId()));
    }

    // POST /api/matches/{matchKey}/start — Start innings with openers and bowler
    @PostMapping("/{matchKey}/start")
    public ResponseEntity<Innings> startInnings(@PathVariable String matchKey,
                                                 @RequestBody InningsStartRequest request,
                                                 @AuthenticationPrincipal UserDetails userDetails) {
        Innings innings = scoringService.startInnings(matchKey, request, userDetails.getUsername());
        return ResponseEntity.ok(innings);
    }

    // POST /api/matches/{matchKey}/start-second-innings — Start second innings
    @PostMapping("/{matchKey}/start-second-innings")
    public ResponseEntity<Innings> startSecondInnings(@PathVariable String matchKey,
                                                       @RequestBody InningsStartRequest request,
                                                       @AuthenticationPrincipal UserDetails userDetails) {
        Innings innings = scoringService.startInnings(matchKey, request, userDetails.getUsername());
        return ResponseEntity.ok(innings);
    }

    // GET /api/matches/my — Get matches created by current user
    @GetMapping("/my")
    public ResponseEntity<List<MatchSummaryDTO>> getMyMatches(@AuthenticationPrincipal UserDetails userDetails) {
        var user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        List<MatchSummaryDTO> summaries = matchRepository.findByCreatedByUserId(user.getId())
                .stream().map(this::toSummaryDTO).collect(Collectors.toList());
        return ResponseEntity.ok(summaries);
    }

    // GET /api/matches/{matchKey} — Get match details
    @GetMapping("/{matchKey}")
    public ResponseEntity<Match> getMatch(@PathVariable String matchKey) {
        Match match = matchRepository.findByMatchKey(matchKey)
                .orElseThrow(() -> new RuntimeException("Match not found"));
        return ResponseEntity.ok(match);
    }

    // DELETE /api/matches/{matchKey} — Delete a match (creator or ADMIN)
    @DeleteMapping("/{matchKey}")
    public ResponseEntity<Map<String, String>> deleteMatch(@PathVariable String matchKey,
                                                            @AuthenticationPrincipal UserDetails userDetails) {
        Match match = matchRepository.findByMatchKey(matchKey)
                .orElseThrow(() -> new RuntimeException("Match not found"));
        var user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        boolean isAdmin = user.getRole().equals("ADMIN");
        if (!match.getCreatedByUserId().equals(user.getId()) && !isAdmin) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }
        matchRepository.delete(match);
        return ResponseEntity.ok(Map.of("message", "Match deleted"));
    }

    // GET /api/matches/{matchKey}/scorecard — Full scorecard
    @GetMapping("/{matchKey}/scorecard")
    public ResponseEntity<ScorecardResponse> getScorecard(@PathVariable String matchKey) {
        return ResponseEntity.ok(scoringService.buildScorecard(matchKey));
    }

    // GET /api/matches/{matchKey}/innings/current — Current innings live state
    @GetMapping("/{matchKey}/innings/current")
    public ResponseEntity<ScorecardResponse.LiveState> getCurrentInnings(@PathVariable String matchKey) {
        ScorecardResponse sc = scoringService.buildScorecard(matchKey);
        return ResponseEntity.ok(sc.getLiveState());
    }

    private MatchSummaryDTO toSummaryDTO(Match m) {
        MatchSummaryDTO dto = new MatchSummaryDTO();
        dto.setId(m.getId());
        dto.setMatchKey(m.getMatchKey());
        dto.setTeam1Name(m.getTeam1Name());
        dto.setTeam2Name(m.getTeam2Name());
        dto.setTotalOvers(m.getTotalOvers());
        dto.setVenue(m.getVenue());
        dto.setMatchDate(m.getMatchDate());
        dto.setStatus(m.getStatus());
        dto.setResult(m.getResult());
        return dto;
    }
}
