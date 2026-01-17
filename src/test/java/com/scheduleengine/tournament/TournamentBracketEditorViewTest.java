package com.scheduleengine.tournament;

import com.scheduleengine.tournament.domain.Tournament;
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

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

@ExtendWith(ApplicationExtension.class)
class TournamentBracketEditorViewTest {

  @Mock
  private TournamentService tournamentService;

  @Mock
  private TournamentRegistrationService registrationService;

  private TournamentBracketEditorView bracketEditorView;

  @Start
  public void start(Stage stage) {
    MockitoAnnotations.openMocks(this);

    when(registrationService.findByTournamentId(anyLong())).thenReturn(Collections.emptyList());

    bracketEditorView = new TournamentBracketEditorView(tournamentService, registrationService);

    Tournament testTournament = new Tournament();
    testTournament.setId(1L);
    testTournament.setName("Spring Championship");
    testTournament.setType(Tournament.TournamentType.OPEN);
    testTournament.setStatus(Tournament.TournamentStatus.REGISTRATION);
    testTournament.setStartDate(LocalDate.of(2024, 4, 1));
    testTournament.setEndDate(LocalDate.of(2024, 4, 15));
    testTournament.setMaxTeams(16);

    VBox view = bracketEditorView.getView(testTournament);
    Scene scene = new Scene(view, 1000, 600);
    stage.setScene(scene);
    stage.show();
  }

  @Test
  void shouldDisplayTournamentNameInTitle() {
    verifyThat("Tournament Bracket - Spring Championship",
      hasText("Tournament Bracket - Spring Championship"));
  }

  @Test
  void shouldDisplayAutoSeedTeamsButton() {
    verifyThat("Auto-Seed Teams", hasText("Auto-Seed Teams"));
  }

  @Test
  void shouldDisplayGenerateBracketButton() {
    verifyThat("Generate Bracket", hasText("Generate Bracket"));
  }

  @Test
  void shouldDisplayCloseButton() {
    verifyThat("Close", hasText("Close"));
  }

  @Test
  void shouldDisplayTournamentType() {
    verifyThat("OPEN", hasText("OPEN"));
  }
}

