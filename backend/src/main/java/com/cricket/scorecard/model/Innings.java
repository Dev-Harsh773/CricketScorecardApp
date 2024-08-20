package com.cricket.scorecard.model;

import jakarta.persistence.*;

@Entity
@Table(name = "innings")
public class Innings {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "match_id", nullable = false)
    private Long matchId;
    
    @Column(name = "innings_number", nullable = false)
    private Integer inningsNumber;
    
    @Column(name = "batting_team", nullable = false)
    private String battingTeam;
    
    @Column(name = "bowling_team", nullable = false)
    private String bowlingTeam;
    
    @Column(name = "total_runs")
    private Integer totalRuns = 0;
    
    @Column(name = "total_wickets")
    private Integer totalWickets = 0;
    
    @Column(name = "legal_balls_bowled")
    private Integer legalBallsBowled = 0;
    
    @Column(name = "extras_total")
    private Integer extrasTotal = 0;
    
    private Integer wides = 0;
    
    @Column(name = "no_balls")
    private Integer noBalls = 0;
    
    private Integer byes = 0;
    
    @Column(name = "leg_byes")
    private Integer legByes = 0;
    
    private String status = "IN_PROGRESS";
    private Integer target;
    
    @Column(name = "striker_player_id")
    private Long strikerPlayerId;
    
    @Column(name = "non_striker_player_id")
    private Long nonStrikerPlayerId;
    
    @Column(name = "current_bowler_id")
    private Long currentBowlerId;
    
    @Column(name = "balls_this_over")
    private Integer ballsThisOver = 0;
    
    @Column(name = "innings_state")
    private String inningsState = "NEEDS_OPENERS";

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMatchId() { return matchId; }
    public void setMatchId(Long matchId) { this.matchId = matchId; }
    public Integer getInningsNumber() { return inningsNumber; }
    public void setInningsNumber(Integer inningsNumber) { this.inningsNumber = inningsNumber; }
    public String getBattingTeam() { return battingTeam; }
    public void setBattingTeam(String battingTeam) { this.battingTeam = battingTeam; }
    public String getBowlingTeam() { return bowlingTeam; }
    public void setBowlingTeam(String bowlingTeam) { this.bowlingTeam = bowlingTeam; }
    public Integer getTotalRuns() { return totalRuns; }
    public void setTotalRuns(Integer totalRuns) { this.totalRuns = totalRuns; }
    public Integer getTotalWickets() { return totalWickets; }
    public void setTotalWickets(Integer totalWickets) { this.totalWickets = totalWickets; }
    public Integer getLegalBallsBowled() { return legalBallsBowled; }
    public void setLegalBallsBowled(Integer legalBallsBowled) { this.legalBallsBowled = legalBallsBowled; }
    public Integer getExtrasTotal() { return extrasTotal; }
    public void setExtrasTotal(Integer extrasTotal) { this.extrasTotal = extrasTotal; }
    public Integer getWides() { return wides; }
    public void setWides(Integer wides) { this.wides = wides; }
    public Integer getNoBalls() { return noBalls; }
    public void setNoBalls(Integer noBalls) { this.noBalls = noBalls; }
    public Integer getByes() { return byes; }
    public void setByes(Integer byes) { this.byes = byes; }
    public Integer getLegByes() { return legByes; }
    public void setLegByes(Integer legByes) { this.legByes = legByes; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getTarget() { return target; }
    public void setTarget(Integer target) { this.target = target; }
    public Long getStrikerPlayerId() { return strikerPlayerId; }
    public void setStrikerPlayerId(Long strikerPlayerId) { this.strikerPlayerId = strikerPlayerId; }
    public Long getNonStrikerPlayerId() { return nonStrikerPlayerId; }
    public void setNonStrikerPlayerId(Long nonStrikerPlayerId) { this.nonStrikerPlayerId = nonStrikerPlayerId; }
    public Long getCurrentBowlerId() { return currentBowlerId; }
    public void setCurrentBowlerId(Long currentBowlerId) { this.currentBowlerId = currentBowlerId; }
    public Integer getBallsThisOver() { return ballsThisOver; }
    public void setBallsThisOver(Integer ballsThisOver) { this.ballsThisOver = ballsThisOver; }
    public String getInningsState() { return inningsState; }
    public void setInningsState(String inningsState) { this.inningsState = inningsState; }
}
