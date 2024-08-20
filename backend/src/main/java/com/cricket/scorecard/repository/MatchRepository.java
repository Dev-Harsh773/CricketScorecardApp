package com.cricket.scorecard.repository;

import com.cricket.scorecard.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {
    Optional<Match> findByMatchKey(String matchKey);
    List<Match> findByCreatedByUserId(Long userId);
}
