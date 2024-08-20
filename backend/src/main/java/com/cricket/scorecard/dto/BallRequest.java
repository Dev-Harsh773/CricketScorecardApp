package com.cricket.scorecard.dto;

public class BallRequest {
    private Integer runsScored;
    private Boolean isWide;
    private Boolean isNoBall;
    private Integer extraRuns;
    private Boolean isBye;
    private Boolean isLegBye;
    private String bowlingSide;

    public Integer getRunsScored() { return runsScored != null ? runsScored : 0; }
    public void setRunsScored(Integer runsScored) { this.runsScored = runsScored; }
    public Boolean getIsWide() { return isWide != null ? isWide : false; }
    public void setIsWide(Boolean isWide) { this.isWide = isWide; }
    public Boolean getIsNoBall() { return isNoBall != null ? isNoBall : false; }
    public void setIsNoBall(Boolean isNoBall) { this.isNoBall = isNoBall; }
    public Integer getExtraRuns() { return extraRuns != null ? extraRuns : 0; }
    public void setExtraRuns(Integer extraRuns) { this.extraRuns = extraRuns; }
    public Boolean getIsBye() { return isBye != null ? isBye : false; }
    public void setIsBye(Boolean isBye) { this.isBye = isBye; }
    public Boolean getIsLegBye() { return isLegBye != null ? isLegBye : false; }
    public void setIsLegBye(Boolean isLegBye) { this.isLegBye = isLegBye; }
    public String getBowlingSide() { return bowlingSide; }
    public void setBowlingSide(String bowlingSide) { this.bowlingSide = bowlingSide; }
}
