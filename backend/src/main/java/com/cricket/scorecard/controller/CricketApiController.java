package com.cricket.scorecard.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * CricketApiController — STUB only.
 * External cricket API integration is NOT implemented in this project.
 * Only scores from matches created within this application are shown.
 * TODO: Connect to external cricket API (e.g., CricAPI) in the future.
 */
@RestController
@RequestMapping("/api/cricket")
public class CricketApiController {

    // GET /api/cricket/live-matches
    // TODO: Integrate with external cricket API to fetch live matches
    @GetMapping("/live-matches")
    public ResponseEntity<List<Object>> getLiveMatches() {
        // Returns empty — no external API integration
        return ResponseEntity.ok(List.of());
    }

    // GET /api/cricket/icc-rankings/{type}
    // TODO: Integrate with external cricket API for ICC rankings
    @GetMapping("/icc-rankings/{type}")
    public ResponseEntity<List<Object>> getIccRankings(@PathVariable String type) {
        // Returns empty — no external API integration
        return ResponseEntity.ok(List.of());
    }
}
