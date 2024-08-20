package com.cricket.scorecard.dto;

public class NextBowlerRequest {
    private Long nextBowlerId;
    private String bowlingSide;

    public Long getNextBowlerId() { return nextBowlerId; }
    public void setNextBowlerId(Long nextBowlerId) { this.nextBowlerId = nextBowlerId; }
    public String getBowlingSide() { return bowlingSide; }
    public void setBowlingSide(String bowlingSide) { this.bowlingSide = bowlingSide; }
}
