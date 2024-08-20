package com.cricket.scorecard.model;

import jakarta.persistence.*;

@Entity
@Table(name = "batting_performances")
public class BattingPerformance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "innings_id", nullable = false)
    private Long inningsId;
    
    @Column(name = "player_id", nullable = false)
    private Long playerId;
    
    @Column(name = "runs_scored")
    private Integer runsScored = 0;
    
    @Column(name = "balls_faced")
    private Integer ballsFaced = 0;
    
    private Integer fours = 0;
    private Integer sixes = 0;
    
    @Column(name = "dismissal_info")
    private String dismissalInfo = "not out";
    
    @Column(name = "batting_position")
    private Integer battingPosition;
    
    @Column(name = "is_active")
    private Integer isActive = 1;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getInningsId() { return inningsId; }
    public void setInningsId(Long inningsId) { this.inningsId = inningsId; }
    public Long getPlayerId() { return playerId; }
    public void setPlayerId(Long playerId) { this.playerId = playerId; }
    public Integer getRunsScored() { return runsScored; }
    public void setRunsScored(Integer runsScored) { this.runsScored = runsScored; }
    public Integer getBallsFaced() { return ballsFaced; }
    public void setBallsFaced(Integer ballsFaced) { this.ballsFaced = ballsFaced; }
    public Integer getFours() { return fours; }
    public void setFours(Integer fours) { this.fours = fours; }
    public Integer getSixes() { return sixes; }
    public void setSixes(Integer sixes) { this.sixes = sixes; }
    public String getDismissalInfo() { return dismissalInfo; }
    public void setDismissalInfo(String dismissalInfo) { this.dismissalInfo = dismissalInfo; }
    public Integer getBattingPosition() { return battingPosition; }
    public void setBattingPosition(Integer battingPosition) { this.battingPosition = battingPosition; }
    public Integer getIsActive() { return isActive; }
    public void setIsActive(Integer isActive) { this.isActive = isActive; }
}
