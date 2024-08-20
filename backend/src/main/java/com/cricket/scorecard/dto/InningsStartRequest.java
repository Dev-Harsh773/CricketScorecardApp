package com.cricket.scorecard.dto;

public class InningsStartRequest {
    private Long strikerId;
    private Long nonStrikerId;
    private Long bowlerId;
    private String bowlingSide;

    // Getters and Setters
    public Long getStrikerId() { return strikerId; }
    public void setStrikerId(Long strikerId) { this.strikerId = strikerId; }
    public Long getNonStrikerId() { return nonStrikerId; }
    public void setNonStrikerId(Long nonStrikerId) { this.nonStrikerId = nonStrikerId; }
    public Long getBowlerId() { return bowlerId; }
    public void setBowlerId(Long bowlerId) { this.bowlerId = bowlerId; }
    public String getBowlingSide() { return bowlingSide; }
    public void setBowlingSide(String bowlingSide) { this.bowlingSide = bowlingSide; }
}
