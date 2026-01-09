package com.scheduleengine.tournament.repository;

import com.scheduleengine.tournament.domain.TournamentRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TournamentRegistrationRepository extends JpaRepository<TournamentRegistration, Long> {
    List<TournamentRegistration> findByTournamentId(Long tournamentId);
    List<TournamentRegistration> findByTeamId(Long teamId);
    List<TournamentRegistration> findByTournamentIdAndStatus(Long tournamentId, TournamentRegistration.RegistrationStatus status);
    Optional<TournamentRegistration> findByTournamentIdAndTeamId(Long tournamentId, Long teamId);
    long countByTournamentIdAndStatus(Long tournamentId, TournamentRegistration.RegistrationStatus status);
}

