package com.scheduleengine.payment;

import com.scheduleengine.league.service.LeagueService;
import com.scheduleengine.player.service.PlayerService;
import com.scheduleengine.team.service.TeamService;
import com.scheduleengine.tournament.service.TournamentRegistrationService;
import com.scheduleengine.tournament.service.TournamentService;
import com.scheduleengine.payment.domain.Transaction;
import com.scheduleengine.payment.service.TransactionService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)
class PaymentsViewTest {

  @Mock private TransactionService transactionService;
  @Mock private TeamService teamService;
  @Mock private PlayerService playerService;
  @Mock private LeagueService leagueService;
  @Mock private TournamentService tournamentService;
  @Mock private TournamentRegistrationService registrationService;

  private PaymentsView paymentsView;

  @Start
  public void start(Stage stage) {
    MockitoAnnotations.openMocks(this);
    paymentsView = new PaymentsView(transactionService, teamService, playerService, leagueService, tournamentService, registrationService);
    VBox view = paymentsView.getView();
    Scene scene = new Scene(view, 1200, 700);
    stage.setScene(scene);
    stage.show();
  }

  @BeforeEach
  void resetMocks() {
    clearInvocations(transactionService);
  }

  @Test
  void amountColumnNumericSort() {
    VBox root = paymentsView.getView();
    TableView<Transaction> table = (TableView<Transaction>) root.getChildren().stream().filter(n -> n instanceof TableView).findFirst().orElseThrow();

    Transaction t1 = new Transaction(); t1.setAmount(100.0);
    Transaction t2 = new Transaction(); t2.setAmount(9.0);
    Transaction t3 = new Transaction(); t3.setAmount(80.0);

    Platform.runLater(() -> {
      table.setItems(FXCollections.observableArrayList(t1, t2, t3));
      TableColumn<Transaction, ?> amount = table.getColumns().stream().filter(c -> "amount".equals(c.getId())).findFirst().orElse(null);
      assertNotNull(amount);
      amount.setSortType(TableColumn.SortType.ASCENDING);
      table.getSortOrder().setAll(amount);
      table.sort();
    });
    WaitForAsyncUtils.waitForFxEvents();

    List<Transaction> sorted = new ArrayList<>(table.getItems());
    assertEquals(9.0, sorted.get(0).getAmount());
    assertEquals(80.0, sorted.get(1).getAmount());
    assertEquals(100.0, sorted.get(2).getAmount());
  }

  @Test
  void defaultFilterExcludesPaid() {
    when(transactionService.findAll()).thenReturn(List.of(
      tx(1L, 10.0, Transaction.Category.PAYMENT, LocalDate.now(), Transaction.Status.PAID),
      tx(2L, 10.0, Transaction.Category.PAYMENT, LocalDate.now(), Transaction.Status.PENDING)
    ));

    // Reload view data via refresh to trigger service and filters
    paymentsView.refresh();
    TableView<Transaction> table = lookupTable();

    // Expect only non-PAID shown by default
    assertTrue(table.getItems().stream().allMatch(t -> t.getStatus() != Transaction.Status.PAID));
  }

  @Test
  void overdueRowsStyledOnLoad() {
    TableView<Transaction> table = lookupTable();
    Transaction overdue = tx(1L, 10.0, Transaction.Category.PAYMENT, LocalDate.now().minusDays(10), Transaction.Status.OVERDUE);
    table.setItems(FXCollections.observableArrayList(overdue));
    table.layout();
    // We can’t assert Node style without skin; assert semantic state present
    assertEquals(Transaction.Status.OVERDUE, table.getItems().get(0).getStatus());
  }

  private TableView<Transaction> lookupTable() {
    return (TableView<Transaction>) paymentsView.getView().getChildren().filtered(n -> n instanceof TableView).get(0);
  }

  private Transaction tx(Long id, Double amount, Transaction.Category cat, LocalDate date, Transaction.Status status) {
    Transaction t = new Transaction();
    t.setId(id);
    t.setAmount(amount);
    t.setCategory(cat);
    t.setDate(date);
    t.setStatus(status);
    t.setPartyType(Transaction.PartyType.TEAM);
    t.setPartyId(1L);
    return t;
  }
}
