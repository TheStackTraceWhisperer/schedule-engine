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
    
    public Season save(Season season) {
        return seasonRepository.save(season);
    }
    
    public Season update(Long id, Season season) {
        season.setId(id);
        return seasonRepository.save(season);
    }

    public void deleteById(Long id) {
        seasonRepository.deleteById(id);
    }
}
