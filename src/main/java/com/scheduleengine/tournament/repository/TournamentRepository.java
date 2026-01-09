package com.scheduleengine.tournament.repository;

import com.scheduleengine.tournament.domain.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {
    List<Tournament> findByLeagueId(Long leagueId);
    List<Tournament> findByType(Tournament.TournamentType type);
    List<Tournament> findByStatus(Tournament.TournamentStatus status);
}

