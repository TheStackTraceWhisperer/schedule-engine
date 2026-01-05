package com.scheduleengine.repository;

import com.scheduleengine.domain.Game;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;

import java.util.List;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    List<Game> findAll();
    List<Game> findBySeasonId(Long seasonId);
    List<Game> findByHomeTeamIdOrAwayTeamId(Long homeTeamId, Long awayTeamId);
}
