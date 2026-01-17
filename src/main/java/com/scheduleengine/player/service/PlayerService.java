package com.scheduleengine.player.service;

import com.scheduleengine.player.domain.Player;
import com.scheduleengine.player.repository.PlayerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PlayerService {

  private final PlayerRepository playerRepository;

  public PlayerService(PlayerRepository playerRepository) {
    this.playerRepository = playerRepository;
  }

  public List<Player> findAll() {
    return playerRepository.findAll();
  }

  public Optional<Player> findById(Long id) {
    return playerRepository.findById(id);
  }

  public List<Player> findByTeamId(Long teamId) {
    return playerRepository.findByTeamId(teamId);
  }

  public Optional<Player> findByFirstNameAndLastName(String firstName, String lastName) {
    return playerRepository.findByFirstNameAndLastName(firstName, lastName);
  }

  public Player save(Player player) {
    return playerRepository.save(player);
  }

  public Player update(Long id, Player player) {
    player.setId(id);
    return playerRepository.save(player);
  }

  public void deleteById(Long id) {
    playerRepository.deleteById(id);
  }

  public void deleteByTeamId(Long teamId) {
    playerRepository.deleteByTeamId(teamId);
  }
}

