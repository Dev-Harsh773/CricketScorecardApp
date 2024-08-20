package com.cricket.scorecard.model;

import jakarta.persistence.*;

@Entity
@Table(name = "players")
public class Player {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "match_id", nullable = false)
    private Long matchId;
    
    @Column(name = "team_name", nullable = false)
    private String teamName;
    
    @Column(name = "player_name", nullable = false)
    private String playerName;
    
    @Column(name = "batting_style")
    private String battingStyle;
    
    @Column(name = "bowling_style")
    private String bowlingStyle;
    
    @Column(name = "player_role")
    private String playerRole;
    
    @Column(name = "batting_position")
    private Integer battingPosition;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getMatchId() { return matchId; }
    public void setMatchId(Long matchId) { this.matchId = matchId; }
    
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    
    public String getBattingStyle() { return battingStyle; }
    public void setBattingStyle(String battingStyle) { this.battingStyle = battingStyle; }
    
    public String getBowlingStyle() { return bowlingStyle; }
    public void setBowlingStyle(String bowlingStyle) { this.bowlingStyle = bowlingStyle; }
    
    public String getPlayerRole() { return playerRole; }
    public void setPlayerRole(String playerRole) { this.playerRole = playerRole; }
    
    public Integer getBattingPosition() { return battingPosition; }
    public void setBattingPosition(Integer battingPosition) { this.battingPosition = battingPosition; }
}
