package com.cricket.scorecard.model;

import jakarta.persistence.*;

@Entity
@Table(name = "matches")
public class Match {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "match_key", unique = true, nullable = false)
    private String matchKey;
    
    @Column(name = "team1_name", nullable = false)
    private String team1Name;
    
    @Column(name = "team2_name", nullable = false)
    private String team2Name;
    
    @Column(name = "total_overs", nullable = false)
    private Integer totalOvers;
    
    private String venue;
    
    @Column(name = "match_date")
    private String matchDate;
    
    @Column(name = "toss_winner")
    private String tossWinner;
    
    @Column(name = "toss_decision")
    private String tossDecision;
    
    @Column(name = "batting_first_team")
    private String battingFirstTeam;
    
    @Column(name = "bowling_first_team")
    private String bowlingFirstTeam;
    
    private String status = "SETUP";
    private String result;
    
    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getMatchKey() { return matchKey; }
    public void setMatchKey(String matchKey) { this.matchKey = matchKey; }
    
    public String getTeam1Name() { return team1Name; }
    public void setTeam1Name(String team1Name) { this.team1Name = team1Name; }
    
    public String getTeam2Name() { return team2Name; }
    public void setTeam2Name(String team2Name) { this.team2Name = team2Name; }
    
    public Integer getTotalOvers() { return totalOvers; }
    public void setTotalOvers(Integer totalOvers) { this.totalOvers = totalOvers; }
    
    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }
    
    public String getMatchDate() { return matchDate; }
    public void setMatchDate(String matchDate) { this.matchDate = matchDate; }
    
    public String getTossWinner() { return tossWinner; }
    public void setTossWinner(String tossWinner) { this.tossWinner = tossWinner; }
    
    public String getTossDecision() { return tossDecision; }
    public void setTossDecision(String tossDecision) { this.tossDecision = tossDecision; }
    
    public String getBattingFirstTeam() { return battingFirstTeam; }
    public void setBattingFirstTeam(String battingFirstTeam) { this.battingFirstTeam = battingFirstTeam; }
    
    public String getBowlingFirstTeam() { return bowlingFirstTeam; }
    public void setBowlingFirstTeam(String bowlingFirstTeam) { this.bowlingFirstTeam = bowlingFirstTeam; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    
    public Long getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(Long createdByUserId) { this.createdByUserId = createdByUserId; }
}
