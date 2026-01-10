package com.scheduleengine.team;

import com.scheduleengine.league.domain.League;
import com.scheduleengine.league.service.LeagueService;
import com.scheduleengine.team.domain.Team;
import com.scheduleengine.team.service.TeamService;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

@ExtendWith(ApplicationExtension.class)
class TeamViewTest {

    @Mock
    private TeamService teamService;

    @Mock
    private LeagueService leagueService;

    private TeamView teamView;

    @Start
    public void start(Stage stage) {
        MockitoAnnotations.openMocks(this);

        when(teamService.findAll()).thenReturn(Collections.emptyList());
        when(leagueService.findAll()).thenReturn(Collections.emptyList());

        teamView = new TeamView(teamService, leagueService);

        VBox view = teamView.getView();
        Scene scene = new Scene(view, 1000, 600);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    void shouldDisplayTitle() {
        verifyThat("Teams", hasText("Teams"));
    }

    @Test
    void shouldDisplayAddTeamButton() {
        verifyThat("Add Team", hasText("Add Team"));
    }

    @Test
    void shouldDisplayRefreshButton() {
        verifyThat("Refresh", hasText("Refresh"));
    }

    @Test
    void shouldDisplayDeleteSelectedButton() {
        verifyThat("Delete Selected", hasText("Delete Selected"));
    }

    @Test
    void shouldDisplayClearFilterButton() {
        verifyThat("Clear", hasText("Clear"));
    }

    @Test
    void shouldLoadDataOnInitialization() {
        verify(teamService, atLeastOnce()).findAll();
    }

    @Test
    void shouldLoadLeaguesForFilter() {
        verify(leagueService, atLeastOnce()).findAll();
    }
}

