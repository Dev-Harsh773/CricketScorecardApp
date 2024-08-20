package com.cricket.scorecard.dto;

public class WicketRequest {
    private String wicketType;
    private Long batsmanOutId;
    private String fielderName;
    private Integer runsBeforeDismissal;
    private Boolean isNoBall;
    private String bowlingSide;

    public String getWicketType() { return wicketType; }
    public void setWicketType(String wicketType) { this.wicketType = wicketType; }
    public Long getBatsmanOutId() { return batsmanOutId; }
    public void setBatsmanOutId(Long batsmanOutId) { this.batsmanOutId = batsmanOutId; }
    public String getFielderName() { return fielderName; }
    public void setFielderName(String fielderName) { this.fielderName = fielderName; }
    public Integer getRunsBeforeDismissal() { return runsBeforeDismissal != null ? runsBeforeDismissal : 0; }
    public void setRunsBeforeDismissal(Integer runsBeforeDismissal) { this.runsBeforeDismissal = runsBeforeDismissal; }
    public Boolean getIsNoBall() { return isNoBall != null ? isNoBall : false; }
    public void setIsNoBall(Boolean isNoBall) { this.isNoBall = isNoBall; }
    public String getBowlingSide() { return bowlingSide; }
    public void setBowlingSide(String bowlingSide) { this.bowlingSide = bowlingSide; }
}
