package com.cricket.scorecard.controller;

import com.cricket.scorecard.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    // GET /api/analytics/{matchKey}/run-rate
    @GetMapping("/{matchKey}/run-rate")
    public ResponseEntity<List<Map<String, Object>>> getRunRate(@PathVariable String matchKey) {
        return ResponseEntity.ok(analyticsService.getRunRateData(matchKey));
    }

    // GET /api/analytics/{matchKey}/summary
    @GetMapping("/{matchKey}/summary")
    public ResponseEntity<Map<String, Object>> getSummary(@PathVariable String matchKey) {
        return ResponseEntity.ok(analyticsService.getMatchSummary(matchKey));
    }

    // GET /api/analytics/top-scorers
    @GetMapping("/top-scorers")
    public ResponseEntity<List<Map<String, Object>>> getTopScorers() {
        return ResponseEntity.ok(analyticsService.getTopScorers());
    }

    // GET /api/analytics/top-bowlers
    @GetMapping("/top-bowlers")
    public ResponseEntity<List<Map<String, Object>>> getTopBowlers() {
        return ResponseEntity.ok(analyticsService.getTopBowlers());
    }

    // GET /api/analytics/{matchKey}/top-scorers
    @GetMapping("/{matchKey}/top-scorers")
    public ResponseEntity<List<Map<String, Object>>> getTopScorersByMatch(@PathVariable String matchKey) {
        return ResponseEntity.ok(analyticsService.getTopScorersByMatch(matchKey));
    }

    // GET /api/analytics/{matchKey}/top-bowlers
    @GetMapping("/{matchKey}/top-bowlers")
    public ResponseEntity<List<Map<String, Object>>> getTopBowlersByMatch(@PathVariable String matchKey) {
        return ResponseEntity.ok(analyticsService.getTopBowlersByMatch(matchKey));
    }
}
