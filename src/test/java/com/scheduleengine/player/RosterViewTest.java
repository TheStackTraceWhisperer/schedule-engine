package com.scheduleengine.player;

import com.scheduleengine.player.service.PlayerService;
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

import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

@ExtendWith(ApplicationExtension.class)
class RosterViewTest {

    @Mock
    private PlayerService playerService;

    @Mock
    private TeamService teamService;

    private RosterView rosterView;

    @Start
    public void start(Stage stage) {
        MockitoAnnotations.openMocks(this);

        when(teamService.findAll()).thenReturn(Collections.emptyList());

        rosterView = new RosterView(playerService, teamService);

        VBox view = rosterView.getView();
        Scene scene = new Scene(view, 1000, 600);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    void shouldDisplayTitle() {
        verifyThat("Team Rosters", hasText("Team Rosters"));
    }

    @Test
    void shouldDisplayAddPlayerButton() {
        verifyThat("Add Player", hasText("Add Player"));
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
    void shouldLoadTeamsForFilter() {
        verify(teamService, atLeastOnce()).findAll();
    }

    @Test
    void shouldDisplayTeamFilterLabel() {
        verifyThat("Team:", hasText("Team:"));
    }
}

