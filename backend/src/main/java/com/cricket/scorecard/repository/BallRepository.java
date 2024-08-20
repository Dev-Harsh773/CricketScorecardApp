package com.cricket.scorecard.repository;

import com.cricket.scorecard.model.Ball;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BallRepository extends JpaRepository<Ball, Long> {
    List<Ball> findByInningsIdOrderByOverNumberAscBallNumberAsc(Long inningsId);
}
