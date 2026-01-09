package com.scheduleengine.tournament.service;

import com.scheduleengine.tournament.domain.Tournament;
import com.scheduleengine.tournament.repository.TournamentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TournamentService {

    private final TournamentRepository tournamentRepository;

    public TournamentService(TournamentRepository tournamentRepository) {
        this.tournamentRepository = tournamentRepository;
    }

    public List<Tournament> findAll() {
        return tournamentRepository.findAll();
    }

    public Optional<Tournament> findById(Long id) {
        return tournamentRepository.findById(id);
    }

    public List<Tournament> findByLeagueId(Long leagueId) {
        return tournamentRepository.findByLeagueId(leagueId);
    }

    public List<Tournament> findByType(Tournament.TournamentType type) {
        return tournamentRepository.findByType(type);
    }

    public List<Tournament> findByStatus(Tournament.TournamentStatus status) {
        return tournamentRepository.findByStatus(status);
    }

    public Tournament save(Tournament tournament) {
        return tournamentRepository.save(tournament);
    }

    public Tournament update(Long id, Tournament tournament) {
        tournament.setId(id);
        return tournamentRepository.save(tournament);
    }

    public void deleteById(Long id) {
        tournamentRepository.deleteById(id);
    }
}

