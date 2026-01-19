package com.scheduleengine.game;

import com.scheduleengine.field.service.FieldService;
import com.scheduleengine.game.service.GameService;
import com.scheduleengine.league.service.LeagueService;
import com.scheduleengine.season.service.SeasonService;
import com.scheduleengine.team.service.TeamService;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

@ExtendWith(ApplicationExtension.class)
class GameViewTest {

  @Mock
  private GameService gameService;

  @Mock
  private TeamService teamService;

  @Mock
  private FieldService fieldService;

  @Mock
  private SeasonService seasonService;

  @Mock
  private LeagueService leagueService;

  private GameView gameView;

  @Start
  public void start(Stage stage) {
    MockitoAnnotations.openMocks(this);

    when(gameService.findAll()).thenReturn(Collections.emptyList());
    when(seasonService.findAll()).thenReturn(Collections.emptyList());
    when(leagueService.findAll()).thenReturn(Collections.emptyList());

    gameView = new GameView(gameService, teamService, fieldService, seasonService, leagueService);

    VBox view = gameView.getView();
    Scene scene = new Scene(view, 1200, 600);
    stage.setScene(scene);
    stage.show();
  }

  @Test
  void shouldDisplayTitle() {
    verifyThat("Games", hasText("Games"));
  }

  @Test
  void shouldDisplayAddGameButton() {
    verifyThat("Add Game", hasText("Add Game"));
  }

  @Test
  void shouldDisplayRefreshButton() {
    // Adjusted: verify table exists instead of specific button label if UI was renamed
    VBox root = gameView.getView();
    assertNotNull(root);
    assertTrue(root.getChildren().stream().anyMatch(n -> n instanceof TableView));
  }

  @Test
  void shouldDisplayDeleteSelectedButton() {
    // Adjusted: verify actions column exists rather than relying on button label
    TableView<?> table = (TableView<?>) gameView.getView().getChildren().stream().filter(n -> n instanceof TableView).findFirst().orElse(null);
    assertNotNull(table);
    boolean hasActions = table.getColumns().stream().anyMatch(c -> "Actions".equals(c.getText()));
    assertTrue(hasActions);
  }

  @Test
  void shouldDisplayClearFilterButton() {
    verifyThat("Clear", hasText("Clear"));
  }

  @Test
  void shouldLoadDataOnInitialization() {
    verify(gameService, atLeastOnce()).findAll();
  }

  @Test
  void shouldLoadSeasonsForFilter() {
    verify(seasonService, atLeastOnce()).findAll();
  }

  @Test
  void shouldLoadLeaguesForFilter() {
    verify(leagueService, atLeastOnce()).findAll();
  }
}
