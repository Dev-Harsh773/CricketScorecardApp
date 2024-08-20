package com.cricket.scorecard.repository;

import com.cricket.scorecard.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    List<Player> findByMatchIdAndTeamName(Long matchId, String teamName);
    List<Player> findByMatchId(Long matchId);
}
