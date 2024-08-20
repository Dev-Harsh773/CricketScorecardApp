package com.cricket.scorecard.dto;

import java.util.List;

public class ScorecardResponse {
    private String matchKey;
    private String team1Name;
    private String team2Name;
    private String status;
    private String result;
    private String tossWinner;
    private String tossDecision;
    private String venue;
    private String matchDate;
    private Integer totalOvers;
    
    // Innings 1
    private InningsData innings1;
    // Innings 2
    private InningsData innings2;
    
    // Current live state (for live matches)
    private LiveState liveState;

    public static class InningsData {
        private String battingTeam;
        private String bowlingTeam;
        private Integer totalRuns;
        private Integer totalWickets;
        private Integer legalBallsBowled;
        private Integer extrasTotal;
        private Integer wides;
        private Integer noBalls;
        private Integer byes;
        private Integer legByes;
        private String status;
        private Integer target;
        private List<BatsmanCardDTO> batting;
        private List<BowlerCardDTO> bowling;
        
        // Getters and Setters
        public String getBattingTeam() { return battingTeam; }
        public void setBattingTeam(String battingTeam) { this.battingTeam = battingTeam; }
        public String getBowlingTeam() { return bowlingTeam; }
        public void setBowlingTeam(String bowlingTeam) { this.bowlingTeam = bowlingTeam; }
        public Integer getTotalRuns() { return totalRuns; }
        public void setTotalRuns(Integer totalRuns) { this.totalRuns = totalRuns; }
        public Integer getTotalWickets() { return totalWickets; }
        public void setTotalWickets(Integer totalWickets) { this.totalWickets = totalWickets; }
        public Integer getLegalBallsBowled() { return legalBallsBowled; }
        public void setLegalBallsBowled(Integer legalBallsBowled) { this.legalBallsBowled = legalBallsBowled; }
        public Integer getExtrasTotal() { return extrasTotal; }
        public void setExtrasTotal(Integer extrasTotal) { this.extrasTotal = extrasTotal; }
        public Integer getWides() { return wides; }
        public void setWides(Integer wides) { this.wides = wides; }
        public Integer getNoBalls() { return noBalls; }
        public void setNoBalls(Integer noBalls) { this.noBalls = noBalls; }
        public Integer getByes() { return byes; }
        public void setByes(Integer byes) { this.byes = byes; }
        public Integer getLegByes() { return legByes; }
        public void setLegByes(Integer legByes) { this.legByes = legByes; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Integer getTarget() { return target; }
        public void setTarget(Integer target) { this.target = target; }
        public List<BatsmanCardDTO> getBatting() { return batting; }
        public void setBatting(List<BatsmanCardDTO> batting) { this.batting = batting; }
        public List<BowlerCardDTO> getBowling() { return bowling; }
        public void setBowling(List<BowlerCardDTO> bowling) { this.bowling = bowling; }
    }

    public static class LiveState {
        private String inningsState;
        private String battingTeam;
        private String bowlingTeam;
        private Integer inningsNumber;
        private Integer totalRuns;
        private Integer totalWickets;
        private Integer legalBallsBowled;
        private String oversDisplay;
        private Double currentRunRate;
        private Double projectedScore;
        private Integer target;
        private Double requiredRunRate;
        private BatsmanCardDTO striker;
        private BatsmanCardDTO nonStriker;
        private BowlerCardDTO currentBowler;
        private String lastBallDescription;
        private Boolean isFreehit;
        private List<String> recentBalls;

        // Getters and Setters
        public String getInningsState() { return inningsState; }
        public void setInningsState(String inningsState) { this.inningsState = inningsState; }
        public String getBattingTeam() { return battingTeam; }
        public void setBattingTeam(String battingTeam) { this.battingTeam = battingTeam; }
        public String getBowlingTeam() { return bowlingTeam; }
        public void setBowlingTeam(String bowlingTeam) { this.bowlingTeam = bowlingTeam; }
        public Integer getInningsNumber() { return inningsNumber; }
        public void setInningsNumber(Integer inningsNumber) { this.inningsNumber = inningsNumber; }
        public Integer getTotalRuns() { return totalRuns; }
        public void setTotalRuns(Integer totalRuns) { this.totalRuns = totalRuns; }
        public Integer getTotalWickets() { return totalWickets; }
        public void setTotalWickets(Integer totalWickets) { this.totalWickets = totalWickets; }
        public Integer getLegalBallsBowled() { return legalBallsBowled; }
        public void setLegalBallsBowled(Integer legalBallsBowled) { this.legalBallsBowled = legalBallsBowled; }
        public String getOversDisplay() { return oversDisplay; }
        public void setOversDisplay(String oversDisplay) { this.oversDisplay = oversDisplay; }
        public Double getCurrentRunRate() { return currentRunRate; }
        public void setCurrentRunRate(Double currentRunRate) { this.currentRunRate = currentRunRate; }
        public Double getProjectedScore() { return projectedScore; }
        public void setProjectedScore(Double projectedScore) { this.projectedScore = projectedScore; }
        public Integer getTarget() { return target; }
        public void setTarget(Integer target) { this.target = target; }
        public Double getRequiredRunRate() { return requiredRunRate; }
        public void setRequiredRunRate(Double requiredRunRate) { this.requiredRunRate = requiredRunRate; }
        public BatsmanCardDTO getStriker() { return striker; }
        public void setStriker(BatsmanCardDTO striker) { this.striker = striker; }
        public BatsmanCardDTO getNonStriker() { return nonStriker; }
        public void setNonStriker(BatsmanCardDTO nonStriker) { this.nonStriker = nonStriker; }
        public BowlerCardDTO getCurrentBowler() { return currentBowler; }
        public void setCurrentBowler(BowlerCardDTO currentBowler) { this.currentBowler = currentBowler; }
        public String getLastBallDescription() { return lastBallDescription; }
        public void setLastBallDescription(String lastBallDescription) { this.lastBallDescription = lastBallDescription; }
        public Boolean getIsFreehit() { return isFreehit; }
        public void setIsFreehit(Boolean isFreehit) { this.isFreehit = isFreehit; }
        public List<String> getRecentBalls() { return recentBalls; }
        public void setRecentBalls(List<String> recentBalls) { this.recentBalls = recentBalls; }
    }

    // Getters and Setters
    public String getMatchKey() { return matchKey; }
    public void setMatchKey(String matchKey) { this.matchKey = matchKey; }
    public String getTeam1Name() { return team1Name; }
    public void setTeam1Name(String team1Name) { this.team1Name = team1Name; }
    public String getTeam2Name() { return team2Name; }
    public void setTeam2Name(String team2Name) { this.team2Name = team2Name; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public String getTossWinner() { return tossWinner; }
    public void setTossWinner(String tossWinner) { this.tossWinner = tossWinner; }
    public String getTossDecision() { return tossDecision; }
    public void setTossDecision(String tossDecision) { this.tossDecision = tossDecision; }
    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }
    public String getMatchDate() { return matchDate; }
    public void setMatchDate(String matchDate) { this.matchDate = matchDate; }
    public Integer getTotalOvers() { return totalOvers; }
    public void setTotalOvers(Integer totalOvers) { this.totalOvers = totalOvers; }
    public InningsData getInnings1() { return innings1; }
    public void setInnings1(InningsData innings1) { this.innings1 = innings1; }
    public InningsData getInnings2() { return innings2; }
    public void setInnings2(InningsData innings2) { this.innings2 = innings2; }
    public LiveState getLiveState() { return liveState; }
    public void setLiveState(LiveState liveState) { this.liveState = liveState; }
}
