package com.scheduleengine.team.service;

import com.scheduleengine.league.domain.League;
import com.scheduleengine.team.domain.Team;
import com.scheduleengine.team.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

  @Mock
  private TeamRepository teamRepository;

  private TeamService teamService;
  private Team testTeam;
  private League testLeague;

  @BeforeEach
  void setUp() {
    teamService = new TeamService(teamRepository);

    testLeague = new League("Community League");
    testLeague.setId(1L);

    testTeam = new Team("Red Hawks");
    testTeam.setId(1L);
    testTeam.setLeague(testLeague);
    testTeam.setContactEmail("redhawks@example.com");
    testTeam.setContactPhone("555-1234");
    testTeam.setCoach("John Smith");
  }

  @Test
  void shouldFindAllTeams() {
    Team team2 = new Team("Blue Jays");
    team2.setId(2L);

    when(teamRepository.findAll()).thenReturn(Arrays.asList(testTeam, team2));

    List<Team> teams = teamService.findAll();

    assertEquals(2, teams.size());
    assertEquals("Red Hawks", teams.get(0).getName());
    assertEquals("Blue Jays", teams.get(1).getName());
    verify(teamRepository, times(1)).findAll();
  }

  @Test
  void shouldFindTeamById() {
    when(teamRepository.findById(1L)).thenReturn(Optional.of(testTeam));

    Optional<Team> found = teamService.findById(1L);

    assertTrue(found.isPresent());
    assertEquals("Red Hawks", found.get().getName());
    verify(teamRepository, times(1)).findById(1L);
  }

  @Test
  void shouldReturnEmptyOptionalWhenTeamNotFound() {
    when(teamRepository.findById(999L)).thenReturn(Optional.empty());

    Optional<Team> found = teamService.findById(999L);

    assertFalse(found.isPresent());
  }

  @Test
  void shouldFindTeamsByLeagueId() {
    when(teamRepository.findByLeagueId(1L)).thenReturn(Collections.singletonList(testTeam));

    List<Team> teams = teamService.findByLeagueId(1L);

    assertEquals(1, teams.size());
    assertEquals(1L, teams.get(0).getLeague().getId());
    verify(teamRepository, times(1)).findByLeagueId(1L);
  }

  @Test
  void shouldSaveTeam() {
    when(teamRepository.save(any(Team.class))).thenReturn(testTeam);

    Team saved = teamService.save(testTeam);

    assertNotNull(saved);
    assertEquals("Red Hawks", saved.getName());
    verify(teamRepository, times(1)).save(testTeam);
  }

  @Test
  void shouldUpdateTeam() {
    Team updatedTeam = new Team("Updated Team");
    updatedTeam.setContactEmail("updated@example.com");
    updatedTeam.setCoach("Jane Doe");

    when(teamRepository.save(any(Team.class))).thenReturn(updatedTeam);

    Team result = teamService.update(1L, updatedTeam);

    assertEquals(1L, result.getId());
    assertEquals("Updated Team", result.getName());
    verify(teamRepository, times(1)).save(any(Team.class));
  }

  @Test
  void shouldDeleteTeamById() {
    teamService.deleteById(1L);

    verify(teamRepository, times(1)).deleteById(1L);
  }

  @Test
  void shouldHandleEmptyTeamList() {
    when(teamRepository.findAll()).thenReturn(List.of());

    List<Team> teams = teamService.findAll();

    assertTrue(teams.isEmpty());
  }

  @Test
  void shouldHandleTeamWithAllProperties() {
    when(teamRepository.save(any(Team.class))).thenReturn(testTeam);

    Team saved = teamService.save(testTeam);

    assertNotNull(saved.getLeague());
    assertEquals("redhawks@example.com", saved.getContactEmail());
    assertEquals("555-1234", saved.getContactPhone());
    assertEquals("John Smith", saved.getCoach());
  }

  @Test
  void shouldHandleMultipleTeamsInLeague() {
    Team team2 = new Team("Blue Jays");
    team2.setId(2L);
    team2.setLeague(testLeague);
    team2.setContactEmail("bluejays@example.com");

    Team team3 = new Team("Green Sox");
    team3.setId(3L);
    team3.setLeague(testLeague);
    team3.setContactEmail("greensox@example.com");

    when(teamRepository.findByLeagueId(1L))
      .thenReturn(Arrays.asList(testTeam, team2, team3));

    List<Team> teams = teamService.findByLeagueId(1L);

    assertEquals(3, teams.size());
    assertEquals("redhawks@example.com", teams.get(0).getContactEmail());
    assertEquals("bluejays@example.com", teams.get(1).getContactEmail());
    assertEquals("greensox@example.com", teams.get(2).getContactEmail());
  }

  @Test
  void shouldHandleMultipleTeamsWithDifferentLeagues() {
    League league2 = new League("Pro League");
    league2.setId(2L);

    Team team2 = new Team("Elite Team");
    team2.setId(2L);
    team2.setLeague(league2);

    when(teamRepository.findByLeagueId(1L)).thenReturn(Collections.singletonList(testTeam));
    when(teamRepository.findByLeagueId(2L)).thenReturn(List.of(team2));

    List<Team> league1Teams = teamService.findByLeagueId(1L);
    List<Team> league2Teams = teamService.findByLeagueId(2L);

    assertEquals(1, league1Teams.size());
    assertEquals(1, league2Teams.size());
    assertEquals("Red Hawks", league1Teams.get(0).getName());
    assertEquals("Elite Team", league2Teams.get(0).getName());
  }

}

