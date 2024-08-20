package com.cricket.scorecard.repository;

import com.cricket.scorecard.model.BowlingPerformance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BowlingPerformanceRepository extends JpaRepository<BowlingPerformance, Long> {
    List<BowlingPerformance> findByInningsId(Long inningsId);
    Optional<BowlingPerformance> findByInningsIdAndPlayerId(Long inningsId, Long playerId);
}
