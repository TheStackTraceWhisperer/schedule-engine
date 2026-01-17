package com.scheduleengine.season.service;

import com.scheduleengine.season.domain.Season;
import com.scheduleengine.season.repository.SeasonRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SeasonService {

  private final SeasonRepository seasonRepository;

  public SeasonService(SeasonRepository seasonRepository) {
    this.seasonRepository = seasonRepository;
  }

  public List<Season> findAll() {
    return seasonRepository.findAll();
  }

  public Optional<Season> findById(Long id) {
    return seasonRepository.findById(id);
  }

  public List<Season> findByLeagueId(Long leagueId) {
    return seasonRepository.findByLeagueId(leagueId);
  }

  public Optional<Season> findByName(String name) {
    return seasonRepository.findByName(name);
  }

  public boolean existsByName(String name) {
    return seasonRepository.existsByName(name);
  }

  /**
   * Save a new season. Validates that the name is unique.
   *
   * @throws IllegalArgumentException if a season with the same name already exists
   */
  public Season save(Season season) {
    // Check for duplicate name only for new seasons (id is null)
    if (season.getId() == null && existsByName(season.getName())) {
      throw new IllegalArgumentException("A season with the name '" + season.getName() + "' already exists. Please choose a different name.");
    }
    return seasonRepository.save(season);
  }

  /**
   * Update an existing season. Validates that the name is unique (excluding the current season).
   *
   * @throws IllegalArgumentException if a different season with the same name already exists
   */
  public Season update(Long id, Season season) {
    season.setId(id);

    // Check if another season with the same name exists (excluding this one)
    Optional<Season> existingWithName = findByName(season.getName());
    if (existingWithName.isPresent() && !existingWithName.get().getId().equals(id)) {
      throw new IllegalArgumentException("A season with the name '" + season.getName() + "' already exists. Please choose a different name.");
    }

    return seasonRepository.save(season);
  }

  public void deleteById(Long id) {
    seasonRepository.deleteById(id);
  }
}
