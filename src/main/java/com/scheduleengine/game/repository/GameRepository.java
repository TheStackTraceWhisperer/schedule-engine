package com.scheduleengine.game.repository;

import com.scheduleengine.game.domain.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
  List<Game> findBySeasonId(Long seasonId);

  List<Game> findByHomeTeamIdOrAwayTeamId(Long homeTeamId, Long awayTeamId);

  void deleteBySeasonId(Long seasonId);
}
