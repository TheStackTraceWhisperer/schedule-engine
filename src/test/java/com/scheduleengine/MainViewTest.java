package com.scheduleengine;

import com.scheduleengine.common.service.ScheduleGeneratorService;
import com.scheduleengine.field.service.FieldService;
import com.scheduleengine.field.service.FieldAvailabilityService;
import com.scheduleengine.field.service.FieldUsageBlockService;
import com.scheduleengine.game.service.GameService;
import com.scheduleengine.league.service.LeagueService;
import com.scheduleengine.player.service.PlayerService;
import com.scheduleengine.season.service.SeasonService;
import com.scheduleengine.team.service.TeamService;
import com.scheduleengine.tournament.service.TournamentRegistrationService;
import com.scheduleengine.tournament.service.TournamentService;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

@ExtendWith(ApplicationExtension.class)
class MainViewTest {

    @Mock
    private LeagueService leagueService;

    @Mock
    private TeamService teamService;

    @Mock
    private FieldService fieldService;

    @Mock
    private FieldAvailabilityService fieldAvailabilityService;

    @Mock
    private FieldUsageBlockService fieldUsageBlockService;

    @Mock
    private SeasonService seasonService;

    @Mock
    private GameService gameService;

    @Mock
    private ScheduleGeneratorService scheduleGeneratorService;

    @Mock
    private PlayerService playerService;

    @Mock
    private TournamentService tournamentService;

    @Mock
    private TournamentRegistrationService tournamentRegistrationService;

    private MainView mainView;

    @Start
    public void start(Stage stage) {
        MockitoAnnotations.openMocks(this);

        // Setup mocks to return empty lists
        when(leagueService.findAll()).thenReturn(Collections.emptyList());
        when(teamService.findAll()).thenReturn(Collections.emptyList());
        when(fieldService.findAll()).thenReturn(Collections.emptyList());
        when(seasonService.findAll()).thenReturn(Collections.emptyList());
        when(gameService.findAll()).thenReturn(Collections.emptyList());
        when(tournamentService.findAll()).thenReturn(Collections.emptyList());

        mainView = new MainView(
            leagueService,
            teamService,
            fieldService,
            seasonService,
            gameService,
            scheduleGeneratorService,
            playerService,
            tournamentService,
            tournamentRegistrationService,
            fieldAvailabilityService,
            fieldUsageBlockService
        );

        mainView.start(stage);
    }

    @Test
    void shouldDisplayApplicationTitle() {
        verifyThat("Schedule Engine", hasText("Schedule Engine"));
    }

    @Test
    void shouldDisplayApplicationSubtitle() {
        verifyThat("Sports Management", hasText("Sports Management"));
    }

    @Test
    void shouldDisplayLeaguesNavigationButton() {
        verifyThat("Leagues", hasText("Leagues"));
    }

    @Test
    void shouldDisplayTeamsNavigationButton() {
        verifyThat("Teams", hasText("Teams"));
    }

    @Test
    void shouldDisplayRostersNavigationButton() {
        verifyThat("Rosters", hasText("Rosters"));
    }

    @Test
    void shouldDisplayFieldsNavigationButton() {
        verifyThat("Fields", hasText("Fields"));
    }

    @Test
    void shouldDisplaySeasonsNavigationButton() {
        verifyThat("Seasons", hasText("Seasons"));
    }

    @Test
    void shouldDisplayTournamentsNavigationButton() {
        verifyThat("Tournaments", hasText("Tournaments"));
    }

    @Test
    void shouldDisplayGamesNavigationButton() {
        verifyThat("Games", hasText("Games"));
    }

    @Test
    void shouldLoadLeagueViewByDefault() {
        // The default view is leagues, so league service should be called
        verify(leagueService, atLeastOnce()).findAll();
    }
}

