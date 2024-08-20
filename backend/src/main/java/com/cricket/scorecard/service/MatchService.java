package com.cricket.scorecard.service;

import com.cricket.scorecard.dto.MatchCreateRequest;
import com.cricket.scorecard.dto.PlayerRequest;
import com.cricket.scorecard.model.Match;
import com.cricket.scorecard.model.Player;
import com.cricket.scorecard.model.User;
import com.cricket.scorecard.repository.MatchRepository;
import com.cricket.scorecard.repository.PlayerRepository;
import com.cricket.scorecard.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class MatchService {

    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;
    private final UserRepository userRepository;

    public MatchService(MatchRepository matchRepository, PlayerRepository playerRepository, UserRepository userRepository) {
        this.matchRepository = matchRepository;
        this.playerRepository = playerRepository;
        this.userRepository = userRepository;
    }

    public Match createMatch(MatchCreateRequest request, String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        
        Match match = new Match();
        // Generate a random matchKey e.g. IND-AUS-8H2K
        String key = (request.getTeam1Name().substring(0, Math.min(3, request.getTeam1Name().length())) + "-" +
                     request.getTeam2Name().substring(0, Math.min(3, request.getTeam2Name().length())) + "-" +
                     UUID.randomUUID().toString().substring(0, 4)).toUpperCase();
        
        match.setMatchKey(key);
        match.setTeam1Name(request.getTeam1Name());
        match.setTeam2Name(request.getTeam2Name());
        match.setTotalOvers(request.getTotalOvers());
        match.setVenue(request.getVenue());
        match.setMatchDate(request.getMatchDate());
        match.setTossWinner(request.getTossWinner());
        match.setTossDecision(request.getTossDecision());
        
        if (request.getTossWinner().equals(request.getTeam1Name())) {
            match.setBattingFirstTeam(request.getTossDecision().equalsIgnoreCase("BAT") ? request.getTeam1Name() : request.getTeam2Name());
            match.setBowlingFirstTeam(request.getTossDecision().equalsIgnoreCase("BOWL") ? request.getTeam1Name() : request.getTeam2Name());
        } else {
            match.setBattingFirstTeam(request.getTossDecision().equalsIgnoreCase("BAT") ? request.getTeam2Name() : request.getTeam1Name());
            match.setBowlingFirstTeam(request.getTossDecision().equalsIgnoreCase("BOWL") ? request.getTeam2Name() : request.getTeam1Name());
        }
        
        match.setCreatedByUserId(user.getId());
        match.setStatus("SETUP");
        return matchRepository.save(match);
    }

    public void addPlayers(String matchKey, List<PlayerRequest> players, String username) {
        Match match = matchRepository.findByMatchKey(matchKey).orElseThrow(() -> new RuntimeException("Match not found"));
        User user = userRepository.findByUsername(username).orElseThrow();
        
        if (!match.getCreatedByUserId().equals(user.getId())) {
            throw new RuntimeException("Only match creator can add players");
        }
        
        for (PlayerRequest pr : players) {
            Player p = new Player();
            p.setMatchId(match.getId());
            p.setTeamName(pr.getTeamName());
            p.setPlayerName(pr.getPlayerName());
            p.setPlayerRole(pr.getPlayerRole());
            p.setBattingStyle(pr.getBattingStyle());
            p.setBowlingStyle(pr.getBowlingStyle());
            playerRepository.save(p);
        }
    }
}
