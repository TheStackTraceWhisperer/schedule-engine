package com.scheduleengine.season;

import com.scheduleengine.common.service.ScheduleGeneratorService;
import com.scheduleengine.game.GameView;
import com.scheduleengine.game.service.GameService;
import com.scheduleengine.league.service.LeagueService;
import com.scheduleengine.season.service.SeasonService;
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
public class SeasonViewTest {

  @Mock
  private SeasonService seasonService;

  @Mock
  private LeagueService leagueService;

  @Mock
  private ScheduleGeneratorService scheduleGeneratorService;

  @Mock
  private GameView gameView;

  @Mock
  private GameService gameService;

  private SeasonView seasonView;

  @Start
  public void start(Stage stage) {
    MockitoAnnotations.openMocks(this);

    when(seasonService.findAll()).thenReturn(Collections.emptyList());
    when(leagueService.findAll()).thenReturn(Collections.emptyList());

    seasonView = new SeasonView(seasonService, leagueService,
      scheduleGeneratorService, gameView, gameService);

    VBox view = seasonView.getView();
    Scene scene = new Scene(view, 800, 600);
    stage.setScene(scene);
    stage.show();
  }

  @Test
  void shouldDisplayTitle() {
    verifyThat("Seasons", hasText("Seasons"));
  }

  @Test
  void shouldDisplayAddSeasonButton() {
    verifyThat("Add Season", hasText("Add Season"));
  }

  @Test
  void shouldDisplayRefreshButton() {
    verifyThat("Refresh", hasText("Refresh"));
  }

  @Test
  void shouldDisplayGenerateScheduleButton() {
    verifyThat("Generate Schedule", hasText("Generate Schedule"));
  }


  @Test
  void shouldLoadDataOnInitialization() {
    verify(seasonService, atLeastOnce()).findAll();
  }
}
