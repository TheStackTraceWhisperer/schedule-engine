package com.scheduleengine.service;

import com.scheduleengine.domain.Game;
import com.scheduleengine.repository.GameRepository;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@Singleton
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
    
    @Transactional
    public Game save(Game game) {
        return gameRepository.save(game);
    }
    
    @Transactional
    public Game update(Long id, Game game) {
        game.setId(id);
        return gameRepository.update(game);
    }
    
    @Transactional
    public void deleteById(Long id) {
        gameRepository.deleteById(id);
    }
}
