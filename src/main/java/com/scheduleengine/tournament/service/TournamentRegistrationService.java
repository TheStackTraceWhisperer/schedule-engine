package com.scheduleengine.tournament.service;

import com.scheduleengine.tournament.domain.Tournament;
import com.scheduleengine.tournament.domain.TournamentRegistration;
import com.scheduleengine.tournament.repository.TournamentRegistrationRepository;
import com.scheduleengine.team.domain.Team;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TournamentRegistrationService {

    private final TournamentRegistrationRepository registrationRepository;

    public TournamentRegistrationService(TournamentRegistrationRepository registrationRepository) {
        this.registrationRepository = registrationRepository;
    }

    public List<TournamentRegistration> findAll() {
        return registrationRepository.findAll();
    }

    public Optional<TournamentRegistration> findById(Long id) {
        return registrationRepository.findById(id);
    }

    public List<TournamentRegistration> findByTournamentId(Long tournamentId) {
        return registrationRepository.findByTournamentId(tournamentId);
    }

    public List<TournamentRegistration> findByTeamId(Long teamId) {
        return registrationRepository.findByTeamId(teamId);
    }

    public List<TournamentRegistration> findByTournamentIdAndStatus(Long tournamentId, TournamentRegistration.RegistrationStatus status) {
        return registrationRepository.findByTournamentIdAndStatus(tournamentId, status);
    }

    public Optional<TournamentRegistration> findByTournamentIdAndTeamId(Long tournamentId, Long teamId) {
        return registrationRepository.findByTournamentIdAndTeamId(tournamentId, teamId);
    }

    public long countApprovedTeams(Long tournamentId) {
        return registrationRepository.countByTournamentIdAndStatus(tournamentId, TournamentRegistration.RegistrationStatus.APPROVED);
    }

    @Transactional
    public TournamentRegistration registerTeam(Tournament tournament, Team team, String notes) {
        // Check if already registered
        Optional<TournamentRegistration> existing = findByTournamentIdAndTeamId(tournament.getId(), team.getId());
        if (existing.isPresent()) {
            throw new IllegalStateException("Team already registered for this tournament");
        }

        TournamentRegistration registration = new TournamentRegistration();
        registration.setTournament(tournament);
        registration.setTeam(team);
        registration.setRegistrationDate(LocalDateTime.now());
        registration.setNotes(notes);

        // Auto-approve for open and league tournaments
        if (tournament.getType() == Tournament.TournamentType.OPEN ||
            tournament.getType() == Tournament.TournamentType.LEAGUE) {
            registration.setStatus(TournamentRegistration.RegistrationStatus.APPROVED);
        } else {
            // Invitational requires approval
            registration.setStatus(TournamentRegistration.RegistrationStatus.PENDING);
        }

        return registrationRepository.save(registration);
    }

    public TournamentRegistration save(TournamentRegistration registration) {
        return registrationRepository.save(registration);
    }

    public TournamentRegistration update(Long id, TournamentRegistration registration) {
        registration.setId(id);
        return registrationRepository.save(registration);
    }

    public void deleteById(Long id) {
        registrationRepository.deleteById(id);
    }
}
