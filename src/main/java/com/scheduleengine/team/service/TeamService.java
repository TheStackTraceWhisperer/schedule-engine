package com.scheduleengine.team.service;

import com.scheduleengine.league.domain.League;
import com.scheduleengine.team.domain.Team;
import com.scheduleengine.team.repository.TeamRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TeamService {
    
    private final TeamRepository teamRepository;
    
    public TeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }
    
    public List<Team> findAll() {
        return teamRepository.findAll();
    }
    
    public Optional<Team> findById(Long id) {
        return teamRepository.findById(id);
    }
    
    public List<Team> findByLeagueId(Long leagueId) {
        return teamRepository.findByLeagueId(leagueId);
    }
    
    public List<Team> findByLeague(League league) {
        return teamRepository.findByLeague(league);
    }

    public Team save(Team team) {
        return teamRepository.save(team);
    }
    
    public Team update(Long id, Team team) {
        team.setId(id);
        return teamRepository.save(team);
    }

    public void deleteById(Long id) {
        teamRepository.deleteById(id);
    }
}
