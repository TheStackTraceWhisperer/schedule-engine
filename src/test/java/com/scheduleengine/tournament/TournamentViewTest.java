package com.scheduleengine.tournament;

import com.scheduleengine.league.service.LeagueService;
import com.scheduleengine.team.service.TeamService;
import com.scheduleengine.tournament.service.TournamentRegistrationService;
import com.scheduleengine.tournament.service.TournamentService;
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
class TournamentViewTest {

  @Mock
  private TournamentService tournamentService;

  @Mock
  private TournamentRegistrationService registrationService;

  @Mock
  private LeagueService leagueService;

  @Mock
  private TeamService teamService;

  private TournamentView tournamentView;

  @Start
  public void start(Stage stage) {
    MockitoAnnotations.openMocks(this);

    when(tournamentService.findAll()).thenReturn(Collections.emptyList());

    tournamentView = new TournamentView(tournamentService, registrationService, leagueService, teamService);

    VBox view = tournamentView.getView();
    Scene scene = new Scene(view, 1200, 600);
    stage.setScene(scene);
    stage.show();
  }

  @Test
  void shouldDisplayTitle() {
    verifyThat("Tournaments", hasText("Tournaments"));
  }

  @Test
  void shouldDisplayAddTournamentButton() {
    verifyThat("Add Tournament", hasText("Add Tournament"));
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
  void shouldDisplayTypeFilterLabel() {
    verifyThat("Type:", hasText("Type:"));
  }

  @Test
  void shouldLoadDataOnInitialization() {
    verify(tournamentService, atLeastOnce()).findAll();
  }
}

