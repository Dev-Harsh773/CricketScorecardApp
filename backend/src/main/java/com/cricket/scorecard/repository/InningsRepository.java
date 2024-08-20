package com.cricket.scorecard.repository;

import com.cricket.scorecard.model.Innings;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface InningsRepository extends JpaRepository<Innings, Long> {
    Optional<Innings> findByMatchIdAndInningsNumber(Long matchId, Integer inningsNumber);
}
