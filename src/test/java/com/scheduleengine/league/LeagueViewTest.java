package com.scheduleengine.league;

import com.scheduleengine.league.service.LeagueService;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

@ExtendWith(ApplicationExtension.class)
class LeagueViewTest {

  @Mock
  private LeagueService leagueService;

  private LeagueView leagueView;

  @Start
  public void start(Stage stage) {
    MockitoAnnotations.openMocks(this);

    when(leagueService.findAll()).thenReturn(Collections.emptyList());

    leagueView = new LeagueView(leagueService);

    VBox view = leagueView.getView();
    Scene scene = new Scene(view, 900, 600);
    stage.setScene(scene);
    stage.show();
  }

  @Test
  void shouldDisplayTitle() {
    verifyThat("Leagues", hasText("Leagues"));
  }

  @Test
  void shouldDisplayAddLeagueButton() {
    verifyThat("Add League", hasText("Add League"));
  }

  @Test
  void shouldDisplayRefreshButton() {
    VBox root = leagueView.getView();
    assertNotNull(root);
    assertTrue(root.getChildren().stream().anyMatch(n -> n instanceof TableView));
  }


  @Test
  void shouldLoadDataOnInitialization() {
    verify(leagueService, atLeastOnce()).findAll();
  }
}
