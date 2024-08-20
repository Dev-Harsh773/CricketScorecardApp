package com.cricket.scorecard.model;

import jakarta.persistence.*;

@Entity
@Table(name = "balls")
public class Ball {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "innings_id", nullable = false)
    private Long inningsId;
    
    @Column(name = "over_number", nullable = false)
    private Integer overNumber;
    
    @Column(name = "ball_number", nullable = false)
    private Integer ballNumber;
    
    @Column(name = "batsman_id", nullable = false)
    private Long batsmanId;
    
    @Column(name = "bowler_id", nullable = false)
    private Long bowlerId;
    
    @Column(name = "runs_scored")
    private Integer runsScored = 0;
    
    @Column(name = "is_wicket")
    private Integer isWicket = 0;
    
    @Column(name = "wicket_type")
    private String wicketType;
    
    @Column(name = "fielder_name")
    private String fielderName;
    
    @Column(name = "is_wide")
    private Integer isWide = 0;
    
    @Column(name = "is_no_ball")
    private Integer isNoBall = 0;
    
    @Column(name = "is_free_hit")
    private Integer isFreeHit = 0;
    
    @Column(name = "is_bye")
    private Integer isBye = 0;
    
    @Column(name = "is_leg_bye")
    private Integer isLegBye = 0;
    
    @Column(name = "extra_runs")
    private Integer extraRuns = 0;
    
    @Column(name = "is_legal_delivery")
    private Integer isLegalDelivery = 1;
    
    @Column(name = "bowling_side")
    private String bowlingSide;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getInningsId() { return inningsId; }
    public void setInningsId(Long inningsId) { this.inningsId = inningsId; }
    public Integer getOverNumber() { return overNumber; }
    public void setOverNumber(Integer overNumber) { this.overNumber = overNumber; }
    public Integer getBallNumber() { return ballNumber; }
    public void setBallNumber(Integer ballNumber) { this.ballNumber = ballNumber; }
    public Long getBatsmanId() { return batsmanId; }
    public void setBatsmanId(Long batsmanId) { this.batsmanId = batsmanId; }
    public Long getBowlerId() { return bowlerId; }
    public void setBowlerId(Long bowlerId) { this.bowlerId = bowlerId; }
    public Integer getRunsScored() { return runsScored; }
    public void setRunsScored(Integer runsScored) { this.runsScored = runsScored; }
    public Integer getIsWicket() { return isWicket; }
    public void setIsWicket(Integer isWicket) { this.isWicket = isWicket; }
    public String getWicketType() { return wicketType; }
    public void setWicketType(String wicketType) { this.wicketType = wicketType; }
    public String getFielderName() { return fielderName; }
    public void setFielderName(String fielderName) { this.fielderName = fielderName; }
    public Integer getIsWide() { return isWide; }
    public void setIsWide(Integer isWide) { this.isWide = isWide; }
    public Integer getIsNoBall() { return isNoBall; }
    public void setIsNoBall(Integer isNoBall) { this.isNoBall = isNoBall; }
    public Integer getIsFreeHit() { return isFreeHit; }
    public void setIsFreeHit(Integer isFreeHit) { this.isFreeHit = isFreeHit; }
    public Integer getIsBye() { return isBye; }
    public void setIsBye(Integer isBye) { this.isBye = isBye; }
    public Integer getIsLegBye() { return isLegBye; }
    public void setIsLegBye(Integer isLegBye) { this.isLegBye = isLegBye; }
    public Integer getExtraRuns() { return extraRuns; }
    public void setExtraRuns(Integer extraRuns) { this.extraRuns = extraRuns; }
    public Integer getIsLegalDelivery() { return isLegalDelivery; }
    public void setIsLegalDelivery(Integer isLegalDelivery) { this.isLegalDelivery = isLegalDelivery; }
    public String getBowlingSide() { return bowlingSide; }
    public void setBowlingSide(String bowlingSide) { this.bowlingSide = bowlingSide; }
}
