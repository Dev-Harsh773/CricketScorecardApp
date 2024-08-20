package com.cricket.scorecard.model;

import jakarta.persistence.*;

@Entity
@Table(name = "bowling_performances")
public class BowlingPerformance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "innings_id", nullable = false)
    private Long inningsId;
    
    @Column(name = "player_id", nullable = false)
    private Long playerId;
    
    @Column(name = "balls_bowled")
    private Integer ballsBowled = 0;
    
    private Integer maidens = 0;
    
    @Column(name = "runs_conceded")
    private Integer runsConceded = 0;
    
    @Column(name = "wickets_taken")
    private Integer wicketsTaken = 0;
    
    private Integer dots = 0;
    
    @Column(name = "fours_conceded")
    private Integer foursConceded = 0;
    
    @Column(name = "sixes_conceded")
    private Integer sixesConceded = 0;
    
    private Integer wides = 0;
    
    @Column(name = "no_balls")
    private Integer noBalls = 0;
    
    @Column(name = "runs_this_over")
    private Integer runsThisOver = 0;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getInningsId() { return inningsId; }
    public void setInningsId(Long inningsId) { this.inningsId = inningsId; }
    public Long getPlayerId() { return playerId; }
    public void setPlayerId(Long playerId) { this.playerId = playerId; }
    public Integer getBallsBowled() { return ballsBowled; }
    public void setBallsBowled(Integer ballsBowled) { this.ballsBowled = ballsBowled; }
    public Integer getMaidens() { return maidens; }
    public void setMaidens(Integer maidens) { this.maidens = maidens; }
    public Integer getRunsConceded() { return runsConceded; }
    public void setRunsConceded(Integer runsConceded) { this.runsConceded = runsConceded; }
    public Integer getWicketsTaken() { return wicketsTaken; }
    public void setWicketsTaken(Integer wicketsTaken) { this.wicketsTaken = wicketsTaken; }
    public Integer getDots() { return dots; }
    public void setDots(Integer dots) { this.dots = dots; }
    public Integer getFoursConceded() { return foursConceded; }
    public void setFoursConceded(Integer foursConceded) { this.foursConceded = foursConceded; }
    public Integer getSixesConceded() { return sixesConceded; }
    public void setSixesConceded(Integer sixesConceded) { this.sixesConceded = sixesConceded; }
    public Integer getWides() { return wides; }
    public void setWides(Integer wides) { this.wides = wides; }
    public Integer getNoBalls() { return noBalls; }
    public void setNoBalls(Integer noBalls) { this.noBalls = noBalls; }
    public Integer getRunsThisOver() { return runsThisOver; }
    public void setRunsThisOver(Integer runsThisOver) { this.runsThisOver = runsThisOver; }
}
