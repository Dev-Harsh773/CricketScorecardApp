package com.cricket.scorecard.controller;

import com.cricket.scorecard.dto.*;
import com.cricket.scorecard.model.Innings;
import com.cricket.scorecard.service.ScoringService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/matches")
public class ScoringController {

    private final ScoringService scoringService;

    public ScoringController(ScoringService scoringService) {
        this.scoringService = scoringService;
    }

    // POST /api/matches/{matchKey}/ball — Record a delivery
    @PostMapping("/{matchKey}/ball")
    public ResponseEntity<Innings> recordBall(@PathVariable String matchKey,
                                               @RequestBody BallRequest request,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        Innings innings = scoringService.recordBall(matchKey, request, userDetails.getUsername());
        return ResponseEntity.ok(innings);
    }

    // POST /api/matches/{matchKey}/wicket — Record a wicket
    @PostMapping("/{matchKey}/wicket")
    public ResponseEntity<Innings> recordWicket(@PathVariable String matchKey,
                                                 @RequestBody WicketRequest request,
                                                 @AuthenticationPrincipal UserDetails userDetails) {
        Innings innings = scoringService.recordWicket(matchKey, request, userDetails.getUsername());
        return ResponseEntity.ok(innings);
    }

    // POST /api/matches/{matchKey}/next-batsman — Select next batsman after wicket
    @PostMapping("/{matchKey}/next-batsman")
    public ResponseEntity<Innings> nextBatsman(@PathVariable String matchKey,
                                                @RequestBody NextBatsmanRequest request,
                                                @AuthenticationPrincipal UserDetails userDetails) {
        Innings innings = scoringService.selectNextBatsman(matchKey, request, userDetails.getUsername());
        return ResponseEntity.ok(innings);
    }

    // POST /api/matches/{matchKey}/next-bowler — Select next bowler after over ends
    @PostMapping("/{matchKey}/next-bowler")
    public ResponseEntity<Innings> nextBowler(@PathVariable String matchKey,
                                               @RequestBody NextBowlerRequest request,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        Innings innings = scoringService.selectNextBowler(matchKey, request, userDetails.getUsername());
        return ResponseEntity.ok(innings);
    }
}
