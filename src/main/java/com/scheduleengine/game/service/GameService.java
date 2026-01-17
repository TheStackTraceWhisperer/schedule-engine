package com.scheduleengine.game.service;

import com.scheduleengine.game.domain.Game;
import com.scheduleengine.game.repository.GameRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class GameService {

  private final GameRepository gameRepository;

  public GameService(GameRepository gameRepository) {
    this.gameRepository = gameRepository;
  }

  public List<Game> findAll() {
    return gameRepository.findAll();
  }

  public Optional<Game> findById(Long id) {
    return gameRepository.findById(id);
  }

  public List<Game> findBySeasonId(Long seasonId) {
    return gameRepository.findBySeasonId(seasonId);
  }

  public List<Game> findByTeamId(Long teamId) {
    return gameRepository.findByHomeTeamIdOrAwayTeamId(teamId, teamId);
  }

  public Game save(Game game) {
    return gameRepository.save(game);
  }

  public Game update(Long id, Game game) {
    game.setId(id);
    return gameRepository.save(game);
  }

  public void deleteById(Long id) {
    gameRepository.deleteById(id);
  }

  @Transactional
  public void deleteBySeasonId(Long seasonId) {
    gameRepository.deleteBySeasonId(seasonId);
  }
}
