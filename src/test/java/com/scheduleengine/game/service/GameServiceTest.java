package com.scheduleengine.game.service;

import com.scheduleengine.field.domain.Field;
import com.scheduleengine.game.domain.Game;
import com.scheduleengine.game.repository.GameRepository;
import com.scheduleengine.season.domain.Season;
import com.scheduleengine.team.domain.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

    private GameService gameService;
    private Game testGame;
    private Team homeTeam;
    private Team awayTeam;
    private Season testSeason;
    private Field testField;

    @BeforeEach
    void setUp() {
        gameService = new GameService(gameRepository);

        homeTeam = new Team("Home Team");
        homeTeam.setId(1L);

        awayTeam = new Team("Away Team");
        awayTeam.setId(2L);

        testSeason = new Season();
        testSeason.setId(1L);
        testSeason.setName("Spring 2026");

        testField = new Field("Test Field");
        testField.setId(1L);

        testGame = new Game();
        testGame.setId(1L);
        testGame.setHomeTeam(homeTeam);
        testGame.setAwayTeam(awayTeam);
        testGame.setSeason(testSeason);
        testGame.setField(testField);
        testGame.setGameDate(LocalDateTime.of(2026, 1, 15, 18, 0));
    }

    @Test
    void shouldFindAllGames() {
        Game game2 = new Game();
        game2.setId(2L);

        when(gameRepository.findAll()).thenReturn(Arrays.asList(testGame, game2));

        List<Game> games = gameService.findAll();

        assertEquals(2, games.size());
        verify(gameRepository, times(1)).findAll();
    }

    @Test
    void shouldFindGameById() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(testGame));

        Optional<Game> found = gameService.findById(1L);

        assertTrue(found.isPresent());
        assertEquals(1L, found.get().getId());
        verify(gameRepository, times(1)).findById(1L);
    }

    @Test
    void shouldReturnEmptyOptionalWhenGameNotFound() {
        when(gameRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Game> found = gameService.findById(999L);

        assertFalse(found.isPresent());
    }

    @Test
    void shouldFindGamesBySeasonId() {
        when(gameRepository.findBySeasonId(1L)).thenReturn(Arrays.asList(testGame));

        List<Game> games = gameService.findBySeasonId(1L);

        assertEquals(1, games.size());
        assertEquals(1L, games.get(0).getSeason().getId());
        verify(gameRepository, times(1)).findBySeasonId(1L);
    }

    @Test
    void shouldFindGamesByTeamId() {
        when(gameRepository.findByHomeTeamIdOrAwayTeamId(1L, 1L))
            .thenReturn(Arrays.asList(testGame));

        List<Game> games = gameService.findByTeamId(1L);

        assertEquals(1, games.size());
        verify(gameRepository, times(1)).findByHomeTeamIdOrAwayTeamId(1L, 1L);
    }

    @Test
    void shouldSaveGame() {
        when(gameRepository.save(any(Game.class))).thenReturn(testGame);

        Game saved = gameService.save(testGame);

        assertNotNull(saved);
        assertEquals(1L, saved.getId());
        verify(gameRepository, times(1)).save(testGame);
    }

    @Test
    void shouldUpdateGame() {
        Game updatedGame = new Game();
        updatedGame.setHomeTeam(homeTeam);
        updatedGame.setAwayTeam(awayTeam);
        updatedGame.setSeason(testSeason);

        when(gameRepository.save(any(Game.class))).thenReturn(updatedGame);

        Game result = gameService.update(1L, updatedGame);

        assertEquals(1L, result.getId());
        verify(gameRepository, times(1)).save(any(Game.class));
    }

    @Test
    void shouldDeleteGameById() {
        gameService.deleteById(1L);

        verify(gameRepository, times(1)).deleteById(1L);
    }

    @Test
    void shouldDeleteGamesBySeasonId() {
        gameService.deleteBySeasonId(1L);

        verify(gameRepository, times(1)).deleteBySeasonId(1L);
    }

    @Test
    void shouldHandleMultipleGamesInSeason() {
        Game game2 = new Game();
        game2.setId(2L);
        game2.setSeason(testSeason);

        when(gameRepository.findBySeasonId(1L))
            .thenReturn(Arrays.asList(testGame, game2));

        List<Game> games = gameService.findBySeasonId(1L);

        assertEquals(2, games.size());
    }

    @Test
    void shouldHandleGameWithHomeAndAwayTeam() {
        when(gameRepository.save(any(Game.class))).thenReturn(testGame);

        Game saved = gameService.save(testGame);

        assertNotNull(saved.getHomeTeam());
        assertNotNull(saved.getAwayTeam());
        assertNotEquals(saved.getHomeTeam().getId(), saved.getAwayTeam().getId());
    }

    @Test
    void shouldHandleGameWithFieldAndDateTime() {
        when(gameRepository.save(any(Game.class))).thenReturn(testGame);

        Game saved = gameService.save(testGame);

        assertNotNull(saved.getField());
        assertNotNull(saved.getGameDate());
    }

}

