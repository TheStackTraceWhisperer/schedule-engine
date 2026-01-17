package com.scheduleengine.season.service;

import com.scheduleengine.league.domain.League;
import com.scheduleengine.season.domain.Season;
import com.scheduleengine.season.repository.SeasonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeasonServiceTest {

  @Mock
  private SeasonRepository seasonRepository;

  private SeasonService seasonService;
  private Season testSeason;
  private League testLeague;

  @BeforeEach
  void setUp() {
    seasonService = new SeasonService(seasonRepository);

    testLeague = new League("Community League");
    testLeague.setId(1L);

    testSeason = new Season();
    testSeason.setId(1L);
    testSeason.setName("Spring 2026");
    testSeason.setLeague(testLeague);
    testSeason.setStartDate(LocalDate.of(2026, 3, 1));
    testSeason.setEndDate(LocalDate.of(2026, 5, 31));
  }

  @Test
  void shouldFindAllSeasons() {
    Season season2 = new Season();
    season2.setId(2L);
    season2.setName("Fall 2026");

    when(seasonRepository.findAll()).thenReturn(Arrays.asList(testSeason, season2));

    List<Season> seasons = seasonService.findAll();

    assertEquals(2, seasons.size());
    assertEquals("Spring 2026", seasons.get(0).getName());
    assertEquals("Fall 2026", seasons.get(1).getName());
    verify(seasonRepository, times(1)).findAll();
  }

  @Test
  void shouldFindSeasonById() {
    when(seasonRepository.findById(1L)).thenReturn(Optional.of(testSeason));

    Optional<Season> found = seasonService.findById(1L);

    assertTrue(found.isPresent());
    assertEquals("Spring 2026", found.get().getName());
    verify(seasonRepository, times(1)).findById(1L);
  }

  @Test
  void shouldReturnEmptyOptionalWhenSeasonNotFound() {
    when(seasonRepository.findById(999L)).thenReturn(Optional.empty());

    Optional<Season> found = seasonService.findById(999L);

    assertFalse(found.isPresent());
  }

  @Test
  void shouldFindSeasonsByLeagueId() {
    when(seasonRepository.findByLeagueId(1L)).thenReturn(Collections.singletonList(testSeason));

    List<Season> seasons = seasonService.findByLeagueId(1L);

    assertEquals(1, seasons.size());
    assertEquals(1L, seasons.get(0).getLeague().getId());
    verify(seasonRepository, times(1)).findByLeagueId(1L);
  }

  @Test
  void shouldFindSeasonByName() {
    when(seasonRepository.findByName("Spring 2026")).thenReturn(Optional.of(testSeason));

    Optional<Season> found = seasonService.findByName("Spring 2026");

    assertTrue(found.isPresent());
    assertEquals("Spring 2026", found.get().getName());
    verify(seasonRepository, times(1)).findByName("Spring 2026");
  }

  @Test
  void shouldCheckIfSeasonNameExists() {
    when(seasonRepository.existsByName("Spring 2026")).thenReturn(true);
    when(seasonRepository.existsByName("Summer 2026")).thenReturn(false);

    assertTrue(seasonService.existsByName("Spring 2026"));
    assertFalse(seasonService.existsByName("Summer 2026"));
  }

  @Test
  void shouldThrowExceptionWhenSavingDuplicateSeasonName() {
    Season newSeason = new Season();
    newSeason.setName("Spring 2026");

    when(seasonRepository.existsByName("Spring 2026")).thenReturn(true);

    assertThrows(IllegalArgumentException.class, () -> seasonService.save(newSeason));
    verify(seasonRepository, never()).save(any());
  }

  @Test
  void shouldSaveSeasonWithUniqueName() {
    Season newSeason = new Season();
    newSeason.setName("Summer 2026");

    when(seasonRepository.existsByName("Summer 2026")).thenReturn(false);
    when(seasonRepository.save(any(Season.class))).thenReturn(newSeason);

    Season saved = seasonService.save(newSeason);

    assertNotNull(saved);
    assertEquals("Summer 2026", saved.getName());
    verify(seasonRepository, times(1)).save(newSeason);
  }

  @Test
  void shouldUpdateSeasonWithoutCheckingDuplicate() {
    testSeason.setId(1L);

    when(seasonRepository.save(any(Season.class))).thenReturn(testSeason);

    Season result = seasonService.update(1L, testSeason);

    assertEquals(1L, result.getId());
    verify(seasonRepository, times(1)).save(any(Season.class));
  }

  @Test
  void shouldDeleteSeasonById() {
    seasonService.deleteById(1L);

    verify(seasonRepository, times(1)).deleteById(1L);
  }

  @Test
  void shouldHandleEmptySeasonList() {
    when(seasonRepository.findAll()).thenReturn(List.of());

    List<Season> seasons = seasonService.findAll();

    assertTrue(seasons.isEmpty());
  }

  @Test
  void shouldHandleMultipleSeasonsInLeague() {
    Season season2 = new Season();
    season2.setId(2L);
    season2.setName("Summer 2026");
    season2.setLeague(testLeague);

    Season season3 = new Season();
    season3.setId(3L);
    season3.setName("Fall 2026");
    season3.setLeague(testLeague);

    when(seasonRepository.findByLeagueId(1L))
      .thenReturn(Arrays.asList(testSeason, season2, season3));

    List<Season> seasons = seasonService.findByLeagueId(1L);

    assertEquals(3, seasons.size());
    assertEquals("Spring 2026", seasons.get(0).getName());
    assertEquals("Summer 2026", seasons.get(1).getName());
    assertEquals("Fall 2026", seasons.get(2).getName());
  }

  @Test
  void shouldHandleSeasonWithDates() {
    when(seasonRepository.save(any(Season.class))).thenReturn(testSeason);

    Season saved = seasonService.save(testSeason);

    assertNotNull(saved.getStartDate());
    assertNotNull(saved.getEndDate());
    assertEquals(LocalDate.of(2026, 3, 1), saved.getStartDate());
    assertEquals(LocalDate.of(2026, 5, 31), saved.getEndDate());
  }

}

