package com.cricket.scorecard.dto;

public class BowlerCardDTO {
    private Long playerId;
    private String name;
    private Integer ballsBowled;
    private String oversDisplay;
    private Integer maidens;
    private Integer runsConceded;
    private Integer wickets;
    private Double economy;
    private String bowlingSide;

    public Long getPlayerId() { return playerId; }
    public void setPlayerId(Long playerId) { this.playerId = playerId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getBallsBowled() { return ballsBowled; }
    public void setBallsBowled(Integer ballsBowled) { this.ballsBowled = ballsBowled; }
    public String getOversDisplay() { return oversDisplay; }
    public void setOversDisplay(String oversDisplay) { this.oversDisplay = oversDisplay; }
    public Integer getMaidens() { return maidens; }
    public void setMaidens(Integer maidens) { this.maidens = maidens; }
    public Integer getRunsConceded() { return runsConceded; }
    public void setRunsConceded(Integer runsConceded) { this.runsConceded = runsConceded; }
    public Integer getWickets() { return wickets; }
    public void setWickets(Integer wickets) { this.wickets = wickets; }
    public Double getEconomy() { return economy; }
    public void setEconomy(Double economy) { this.economy = economy; }
    public String getBowlingSide() { return bowlingSide; }
    public void setBowlingSide(String bowlingSide) { this.bowlingSide = bowlingSide; }
}
