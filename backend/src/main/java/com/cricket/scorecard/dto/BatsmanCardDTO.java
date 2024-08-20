package com.cricket.scorecard.dto;

public class BatsmanCardDTO {
    private Long playerId;
    private String name;
    private Integer runs;
    private Integer balls;
    private Integer fours;
    private Integer sixes;
    private Double strikeRate;
    private String dismissalInfo;
    private Integer battingPosition;
    private Boolean isStriker;

    public Long getPlayerId() { return playerId; }
    public void setPlayerId(Long playerId) { this.playerId = playerId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getRuns() { return runs; }
    public void setRuns(Integer runs) { this.runs = runs; }
    public Integer getBalls() { return balls; }
    public void setBalls(Integer balls) { this.balls = balls; }
    public Integer getFours() { return fours; }
    public void setFours(Integer fours) { this.fours = fours; }
    public Integer getSixes() { return sixes; }
    public void setSixes(Integer sixes) { this.sixes = sixes; }
    public Double getStrikeRate() { return strikeRate; }
    public void setStrikeRate(Double strikeRate) { this.strikeRate = strikeRate; }
    public String getDismissalInfo() { return dismissalInfo; }
    public void setDismissalInfo(String dismissalInfo) { this.dismissalInfo = dismissalInfo; }
    public Integer getBattingPosition() { return battingPosition; }
    public void setBattingPosition(Integer battingPosition) { this.battingPosition = battingPosition; }
    public Boolean getIsStriker() { return isStriker; }
    public void setIsStriker(Boolean isStriker) { this.isStriker = isStriker; }
}
