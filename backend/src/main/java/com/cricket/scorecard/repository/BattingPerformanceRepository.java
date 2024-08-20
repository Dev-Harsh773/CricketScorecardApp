package com.cricket.scorecard.repository;

import com.cricket.scorecard.model.BattingPerformance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BattingPerformanceRepository extends JpaRepository<BattingPerformance, Long> {
    List<BattingPerformance> findByInningsIdOrderByBattingPositionAsc(Long inningsId);
    Optional<BattingPerformance> findByInningsIdAndPlayerId(Long inningsId, Long playerId);
}
