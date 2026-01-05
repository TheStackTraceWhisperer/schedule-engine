package com.scheduleengine.service;

import com.scheduleengine.domain.League;
import com.scheduleengine.repository.LeagueRepository;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@Singleton
public class LeagueService {
    
    private final LeagueRepository leagueRepository;
    
    public LeagueService(LeagueRepository leagueRepository) {
        this.leagueRepository = leagueRepository;
    }
    
    public List<League> findAll() {
        return leagueRepository.findAll();
    }
    
    public Optional<League> findById(Long id) {
        return leagueRepository.findById(id);
    }
    
    @Transactional
    public League save(League league) {
        return leagueRepository.save(league);
    }
    
    @Transactional
    public League update(Long id, League league) {
        league.setId(id);
        return leagueRepository.update(league);
    }
    
    @Transactional
    public void deleteById(Long id) {
        leagueRepository.deleteById(id);
    }
}
