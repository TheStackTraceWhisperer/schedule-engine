package com.scheduleengine.league.service;

import com.scheduleengine.league.domain.League;
import com.scheduleengine.league.repository.LeagueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeagueServiceTest {

    @Mock
    private LeagueRepository leagueRepository;

    private LeagueService leagueService;
    private League testLeague;

    @BeforeEach
    void setUp() {
        leagueService = new LeagueService(leagueRepository);
        testLeague = new League("Community League");
        testLeague.setId(1L);
        testLeague.setDescription("A league for community participation");
    }

    @Test
    void shouldFindAllLeagues() {
        League league2 = new League("Professional League");
        league2.setId(2L);

        when(leagueRepository.findAll()).thenReturn(Arrays.asList(testLeague, league2));

        List<League> leagues = leagueService.findAll();

        assertEquals(2, leagues.size());
        assertEquals("Community League", leagues.get(0).getName());
        assertEquals("Professional League", leagues.get(1).getName());
        verify(leagueRepository, times(1)).findAll();
    }

    @Test
    void shouldFindLeagueById() {
        when(leagueRepository.findById(1L)).thenReturn(Optional.of(testLeague));

        Optional<League> found = leagueService.findById(1L);

        assertTrue(found.isPresent());
        assertEquals("Community League", found.get().getName());
        verify(leagueRepository, times(1)).findById(1L);
    }

    @Test
    void shouldReturnEmptyOptionalWhenLeagueNotFound() {
        when(leagueRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<League> found = leagueService.findById(999L);

        assertFalse(found.isPresent());
    }

    @Test
    void shouldSaveLeague() {
        when(leagueRepository.save(any(League.class))).thenReturn(testLeague);

        League saved = leagueService.save(testLeague);

        assertNotNull(saved);
        assertEquals("Community League", saved.getName());
        verify(leagueRepository, times(1)).save(testLeague);
    }

    @Test
    void shouldUpdateLeague() {
        League updatedLeague = new League("Updated League");
        updatedLeague.setDescription("Updated description");

        when(leagueRepository.save(any(League.class))).thenReturn(updatedLeague);

        League result = leagueService.update(1L, updatedLeague);

        assertEquals(1L, result.getId());
        assertEquals("Updated League", result.getName());
        verify(leagueRepository, times(1)).save(any(League.class));
    }

    @Test
    void shouldDeleteLeagueById() {
        leagueService.deleteById(1L);

        verify(leagueRepository, times(1)).deleteById(1L);
    }

    @Test
    void shouldHandleEmptyLeagueList() {
        when(leagueRepository.findAll()).thenReturn(Arrays.asList());

        List<League> leagues = leagueService.findAll();

        assertTrue(leagues.isEmpty());
    }

    @Test
    void shouldHandleLeagueWithDescription() {
        when(leagueRepository.save(any(League.class))).thenReturn(testLeague);

        League saved = leagueService.save(testLeague);

        assertEquals("A league for community participation", saved.getDescription());
    }

    @Test
    void shouldHandleMultipleLeaguesWithDifferentProperties() {
        League league2 = new League("Youth League");
        league2.setId(2L);
        league2.setDescription("For young players");

        League league3 = new League("Senior League");
        league3.setId(3L);
        league3.setDescription("For experienced players");

        when(leagueRepository.findAll()).thenReturn(Arrays.asList(testLeague, league2, league3));

        List<League> leagues = leagueService.findAll();

        assertEquals(3, leagues.size());
        assertEquals("Community League", leagues.get(0).getName());
        assertEquals("Youth League", leagues.get(1).getName());
        assertEquals("Senior League", leagues.get(2).getName());
    }

}

