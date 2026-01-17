package com.scheduleengine.payment;

import com.scheduleengine.common.TableColumnUtil;
import com.scheduleengine.league.domain.League;
import com.scheduleengine.league.service.LeagueService;
import com.scheduleengine.navigation.NavigationContext;
import com.scheduleengine.payment.domain.Transaction;
import com.scheduleengine.payment.service.TransactionService;
import com.scheduleengine.player.service.PlayerService;
import com.scheduleengine.team.domain.Team;
import com.scheduleengine.team.service.TeamService;
import com.scheduleengine.tournament.domain.Tournament;
import com.scheduleengine.tournament.service.TournamentRegistrationService;
import com.scheduleengine.tournament.service.TournamentService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.controlsfx.control.SearchableComboBox;

import java.io.StringReader;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;
import java.util.prefs.Preferences;

public class PaymentsView {
  private static final String PREF_PARTY_TYPE = "payments.filter.partyType";
  private static final String PREF_PARTY_NAME = "payments.filter.partyName";
  private static final String PREF_LEAGUE = "payments.filter.league";
  private static final String PREF_TEAM = "payments.filter.team";
  private static final String PREF_PLAYER = "payments.filter.player";
  private static final String PREF_CATEGORY = "payments.filter.category";
  private static final String PREF_DATE = "payments.filter.date";
  private static final String PREF_AMOUNT = "payments.filter.amount";
  private static final String PREF_NOTES = "payments.filter.notes";
  private static final String PREF_STATUSES = "payments.filter.statuses"; // comma-separated
  private final TransactionService transactionService;
  private final TeamService teamService;
  private final PlayerService playerService;
  private final LeagueService leagueService;
  private final TournamentService tournamentService;
  private final Preferences prefs = Preferences.userNodeForPackage(PaymentsView.class);
  private Consumer<NavigationContext> navigationHandler;
  // Add registration service
  private final TournamentRegistrationService tournamentRegistrationService;
  private TableView<Transaction> table;
  private ObservableList<Transaction> data;
  private ObservableList<Transaction> allData;
  // Filter fields
  private TextField filterPartyType;
  private TextField filterPartyName;
  private TextField filterCategory;
  private TextField filterDate;
  private TextField filterAmount;
  private ComboBox<String> filterStatus;
  private final ObservableList<Transaction.Status> selectedStatuses;
  private TextField filterNotes;
  // New filter fields for additional columns
  private TextField filterLeagueName;
  private TextField filterTeamName;
  private TextField filterPlayerName;
  private ContextMenu statusContextMenu;

  public PaymentsView(TransactionService transactionService,
                      TeamService teamService,
                      PlayerService playerService,
                      LeagueService leagueService,
                      TournamentService tournamentService,
                      TournamentRegistrationService tournamentRegistrationService) {
    this.transactionService = transactionService;
    this.teamService = teamService;
    this.playerService = playerService;
    this.leagueService = leagueService;
    this.tournamentService = tournamentService;
    this.tournamentRegistrationService = tournamentRegistrationService;
    this.selectedStatuses = FXCollections.observableArrayList();
  }

  public void setNavigationHandler(Consumer<NavigationContext> navigationHandler) {
    this.navigationHandler = navigationHandler;
  }

  /**
   * Set filter for party name and exclude PAID status
   */
  public void filterByTeam(String teamName) {
    clearFilters(); // Clear previous filters first

    if (filterPartyName != null) {
      filterPartyName.setText(teamName);
    }

    // Set selected statuses to all except PAID
    if (selectedStatuses != null) {
      selectedStatuses.clear();
      for (Transaction.Status status : Transaction.Status.values()) {
        if (status != Transaction.Status.PAID) {
          selectedStatuses.add(status);
        }
      }
    }

    // Update the status filter UI to reflect the change
    if (statusContextMenu != null) {
      statusContextMenu.getItems().forEach(item -> {
        if (item instanceof CheckMenuItem checkMenuItem) {
          Transaction.Status status = Transaction.Status.valueOf(checkMenuItem.getText());
          if (status != Transaction.Status.PAID) {
            checkMenuItem.setSelected(true);
          }
        }
      });
    }

    applyFilters();
  }

  /**
   * Preset filter for a league's due payments (Pending and Overdue)
   */
  public void filterByLeagueDue(String leagueName) {
    clearFilters();
    if (filterLeagueName != null) {
      filterLeagueName.setText(leagueName);
    }
    selectedStatuses.clear();
    selectedStatuses.add(Transaction.Status.PENDING);
    selectedStatuses.add(Transaction.Status.OVERDUE);
    // Update status context menu selections if present
    if (statusContextMenu != null) {
      statusContextMenu.getItems().forEach(item -> {
        if (item instanceof CheckMenuItem cm) {
          Transaction.Status s = Transaction.Status.valueOf(cm.getText());
          cm.setSelected(s == Transaction.Status.PENDING || s == Transaction.Status.OVERDUE);
        }
      });
    }
    applyFilters();
  }

  public void clearFilters() {
    if (filterPartyType != null) filterPartyType.setText("");
    if (filterPartyName != null) filterPartyName.setText("");
    if (filterLeagueName != null) filterLeagueName.setText("");
    if (filterTeamName != null) filterTeamName.setText("");
    if (filterPlayerName != null) filterPlayerName.setText("");
    if (filterCategory != null) filterCategory.setText("");
    if (filterDate != null) filterDate.setText("");
    if (filterAmount != null) filterAmount.setText("");
    if (filterNotes != null) filterNotes.setText("");
    if (selectedStatuses != null) selectedStatuses.clear();

    // Reset status filter UI
    if (statusContextMenu != null) {
      statusContextMenu.getItems().forEach(item -> {
        if (item instanceof CheckMenuItem) {
          ((CheckMenuItem) item).setSelected(false);
        }
      });
    }

    applyFilters();
  }

  public void refresh() {
    // Load all transactions
    List<Transaction> all = transactionService.findAll();
    allData = FXCollections.observableArrayList(all);
    applyFilters();
  }

  private void applyFilters() {
    if (allData == null) {
      allData = FXCollections.observableArrayList();
    }
    String partyTypeFilter = filterPartyType != null ? filterPartyType.getText().toLowerCase() : "";
    String partyNameFilter = filterPartyName != null ? filterPartyName.getText().toLowerCase() : "";
    String leagueNameFilter = filterLeagueName != null ? filterLeagueName.getText().toLowerCase() : "";
    String teamNameFilter = filterTeamName != null ? filterTeamName.getText().toLowerCase() : "";
    String playerNameFilter = filterPlayerName != null ? filterPlayerName.getText().toLowerCase() : "";
    String categoryFilter = filterCategory != null ? filterCategory.getText().toLowerCase() : "";
    String dateFilter = filterDate != null ? filterDate.getText().toLowerCase() : "";
    String amountFilter = filterAmount != null ? filterAmount.getText().toLowerCase() : "";
    String notesFilter = filterNotes != null ? filterNotes.getText().toLowerCase() : "";

    ObservableList<Transaction> filtered = FXCollections.observableArrayList(
      allData.stream()
        .filter(tx -> tx.getPartyType().name().toLowerCase().contains(partyTypeFilter))
        .filter(tx -> (tx.getPartyName() == null ? "" : tx.getPartyName()).toLowerCase().contains(partyNameFilter))
        .filter(tx -> (tx.getLeagueName() == null ? "" : tx.getLeagueName()).toLowerCase().contains(leagueNameFilter))
        .filter(tx -> (tx.getTeamName() == null ? "" : tx.getTeamName()).toLowerCase().contains(teamNameFilter))
        .filter(tx -> (tx.getPlayerName() == null ? "" : tx.getPlayerName()).toLowerCase().contains(playerNameFilter))
        .filter(tx -> tx.getCategory().name().toLowerCase().contains(categoryFilter))
        .filter(tx -> String.valueOf(tx.getDate()).contains(dateFilter))
        .filter(tx -> String.valueOf(tx.getAmount()).contains(amountFilter))
        .filter(tx -> selectedStatuses.isEmpty() || selectedStatuses.contains(tx.getStatus()))
        .filter(tx -> (tx.getNotes() == null ? "" : tx.getNotes()).toLowerCase().contains(notesFilter))
        .toList()
    );

    data = filtered;
    if (table != null) table.setItems(data);
  }

  private void saveFilters() {
    prefs.put(PREF_PARTY_TYPE, filterPartyType == null ? "" : filterPartyType.getText());
    prefs.put(PREF_PARTY_NAME, filterPartyName == null ? "" : filterPartyName.getText());
    prefs.put(PREF_LEAGUE, filterLeagueName == null ? "" : filterLeagueName.getText());
    prefs.put(PREF_TEAM, filterTeamName == null ? "" : filterTeamName.getText());
    prefs.put(PREF_PLAYER, filterPlayerName == null ? "" : filterPlayerName.getText());
    prefs.put(PREF_CATEGORY, filterCategory == null ? "" : filterCategory.getText());
    prefs.put(PREF_DATE, filterDate == null ? "" : filterDate.getText());
    prefs.put(PREF_AMOUNT, filterAmount == null ? "" : filterAmount.getText());
    prefs.put(PREF_NOTES, filterNotes == null ? "" : filterNotes.getText());
    String statuses = selectedStatuses == null ? "" : String.join(",",
      selectedStatuses.stream().map(Enum::name).toList());
    prefs.put(PREF_STATUSES, statuses);
  }

  private void loadFilters() {
    if (filterPartyType != null) filterPartyType.setText(prefs.get(PREF_PARTY_TYPE, ""));
    if (filterPartyName != null) filterPartyName.setText(prefs.get(PREF_PARTY_NAME, ""));
    if (filterLeagueName != null) filterLeagueName.setText(prefs.get(PREF_LEAGUE, ""));
    if (filterTeamName != null) filterTeamName.setText(prefs.get(PREF_TEAM, ""));
    if (filterPlayerName != null) filterPlayerName.setText(prefs.get(PREF_PLAYER, ""));
    if (filterCategory != null) filterCategory.setText(prefs.get(PREF_CATEGORY, ""));
    if (filterDate != null) filterDate.setText(prefs.get(PREF_DATE, ""));
    if (filterAmount != null) filterAmount.setText(prefs.get(PREF_AMOUNT, ""));
    if (filterNotes != null) filterNotes.setText(prefs.get(PREF_NOTES, ""));
    selectedStatuses.clear();
    String statuses = prefs.get(PREF_STATUSES, "");
    if (!statuses.isBlank()) {
      for (String s : statuses.split(",")) {
        try {
          selectedStatuses.add(Transaction.Status.valueOf(s));
        } catch (IllegalArgumentException ignored) {
        }
      }
    }
    // sync context menu checks
    if (statusContextMenu != null) {
      statusContextMenu.getItems().forEach(item -> {
        if (item instanceof CheckMenuItem cm) {
          Transaction.Status s = Transaction.Status.valueOf(cm.getText());
          cm.setSelected(selectedStatuses.contains(s));
        }
      });
    }
  }

  public VBox getView() {
    VBox container = new VBox(10);
    container.setPadding(new Insets(15));

    Label title = new Label("Payments");
    title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

    table = new TableView<>();
    table.setEditable(true);
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    table.setPlaceholder(new Label("No transactions"));
    table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    // Highlight overdue rows
    table.setRowFactory(tv -> new TableRow<>() {
      @Override
      protected void updateItem(Transaction item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setStyle("");
        } else if (isSelected()) {
          setStyle("");
        } else if (item.getStatus() == Transaction.Status.OVERDUE) {
          setStyle("-fx-background-color: rgba(231,76,60,0.12);");
        } else {
          setStyle("");
        }
      }
    });

    TableColumn<Transaction, String> partyTypeCol = new TableColumn<>("Party Type");
    partyTypeCol.setId("partyType");
    partyTypeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPartyType().name()));

    TableColumn<Transaction, String> partyNameCol = new TableColumn<>("Party Name");
    partyNameCol.setId("partyName");
    partyNameCol.setCellValueFactory(c -> new SimpleStringProperty(
      c.getValue().getPartyName() != null ? c.getValue().getPartyName() : "Unknown"
    ));

    TableColumn<Transaction, String> leagueCol = new TableColumn<>("League");
    leagueCol.setId("league");
    leagueCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLeagueName() != null ? c.getValue().getLeagueName() : ""));

    TableColumn<Transaction, String> teamCol = new TableColumn<>("Team");
    teamCol.setId("team");
    teamCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTeamName() != null ? c.getValue().getTeamName() : ""));

    TableColumn<Transaction, String> playerCol = new TableColumn<>("Contact");
    playerCol.setId("player");
    playerCol.setCellValueFactory(c -> {
      Transaction tx = c.getValue();
      String contact = tx.getPlayerName() != null ? tx.getPlayerName() : "";
      if ((contact == null || contact.isBlank()) && tx.getPartyType() == Transaction.PartyType.TEAM) {
        try {
          Team team = teamService.findById(tx.getPartyId()).orElse(null);
          if (team != null && team.getCoach() != null) {
            contact = team.getCoach();
          }
        } catch (Exception ignored) {
        }
      }
      return new SimpleStringProperty(contact);
    });

    TableColumn<Transaction, String> categoryCol = new TableColumn<>("Category");
    categoryCol.setId("category");
    categoryCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategory().name()));

    TableColumn<Transaction, String> dateCol = new TableColumn<>("Date");
    dateCol.setId("date");
    dateCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getDate())));
    dateCol.setCellFactory(TextFieldTableCell.forTableColumn());
    dateCol.setOnEditCommit(e -> {
      try {
        e.getRowValue().setDate(LocalDate.parse(e.getNewValue()));
        transactionService.save(e.getRowValue());
      } catch (Exception ex) {
        showError("Invalid date");
      }
    });

    TableColumn<Transaction, Number> amountCol = new TableColumn<>("Amount");
    amountCol.setId("amount");
    amountCol.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getAmount()));
    amountCol.setComparator(TableColumnUtil.comparingDouble());
    amountCol.setCellFactory(TextFieldTableCell.forTableColumn(TableColumnUtil.doubleStringConverter()));
    amountCol.setOnEditCommit(e -> {
      try {
        Number n = e.getNewValue();
        if (n == null) throw new IllegalArgumentException("Amount required");
        e.getRowValue().setAmount(n.doubleValue());
        transactionService.save(e.getRowValue());
      } catch (Exception ex) {
        showError("Invalid amount");
      }
    });

    TableColumn<Transaction, String> statusCol = new TableColumn<>("Status");
    statusCol.setId("status");
    statusCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus().name()));
    // Replace text edit with ComboBox for enum statuses
    statusCol.setCellFactory(col -> {
      ComboBoxTableCell<Transaction, String> cell = new ComboBoxTableCell<>(FXCollections.observableArrayList(
        java.util.Arrays.stream(Transaction.Status.values()).map(Enum::name).toList()
      ));
      cell.setComboBoxEditable(false);
      return cell;
    });
    statusCol.setOnEditCommit(e -> {
      try {
        Transaction.Status newStatus = Transaction.Status.valueOf(e.getNewValue());
        e.getRowValue().setStatus(newStatus);
        transactionService.save(e.getRowValue());
      } catch (Exception ex) {
        showError("Invalid status selection");
      }
    });

    TableColumn<Transaction, String> notesCol = new TableColumn<>("Notes");
    notesCol.setId("notes");
    notesCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNotes() == null ? "" : c.getValue().getNotes()));
    notesCol.setCellFactory(TextFieldTableCell.forTableColumn());
    notesCol.setOnEditCommit(e -> {
      e.getRowValue().setNotes(e.getNewValue());
      transactionService.save(e.getRowValue());
    });

    table.getColumns().setAll(partyTypeCol, partyNameCol, leagueCol, teamCol, playerCol, categoryCol, dateCol, amountCol, statusCol, notesCol);
    // Persist table state (widths, visibility, sort)
    com.scheduleengine.common.TablePreferencesUtil.bind(table, "payments");
    com.scheduleengine.common.TablePreferencesUtil.attachToggleMenu(table, "payments");
    VBox.setVgrow(table, Priority.ALWAYS);

    // Create filter row integrated into column headers
    createColumnHeaderFilters(partyTypeCol, partyNameCol, leagueCol, teamCol, playerCol, categoryCol, dateCol, amountCol, statusCol, notesCol);

    // Load persisted filters
    loadFilters();
    // If no statuses saved, default to exclude PAID
    if (selectedStatuses.isEmpty()) {
      for (Transaction.Status s : Transaction.Status.values()) {
        if (s != Transaction.Status.PAID) selectedStatuses.add(s);
      }
      if (statusContextMenu != null) {
        statusContextMenu.getItems().forEach(item -> {
          if (item instanceof CheckMenuItem cm) {
            Transaction.Status s = Transaction.Status.valueOf(cm.getText());
            cm.setSelected(s != Transaction.Status.PAID);
          }
        });
      }
    }
    applyFilters();
    saveFilters();

    // Controls
    Button importBtn = new Button("Import CSV");
    importBtn.setOnAction(e -> {
      TextInputDialog dlg = new TextInputDialog("partyType,partyId,category,date,amount,status,notes\nTEAM,1,INVOICE,2025-01-10,100.00,PENDING,Sample");
      dlg.setTitle("Import CSV");
      dlg.setHeaderText("Paste CSV data");
      dlg.setContentText("CSV:");
      dlg.showAndWait().ifPresent(csv -> {
        try {
          transactionService.importCsv(new StringReader(csv));
          refresh();
        } catch (Exception ex) {
          showError("Import failed: " + ex.getMessage());
        }
      });
    });

    Button exportBtn = new Button("Export CSV");
    exportBtn.setOnAction(e -> {
      String csv = transactionService.exportCsv(data == null ? List.of() : data);
      TextArea area = new TextArea(csv);
      area.setPrefRowCount(12);
      Dialog<Void> d = new Dialog<>();
      d.setTitle("Export CSV");
      d.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
      d.getDialogPane().setContent(area);
      d.showAndWait();
    });

    Button addBtn = new Button("+ Add");
    addBtn.setOnAction(e -> showAddTransactionDialog());

    Button createLeagueInvoiceBtn = new Button("Create League Invoice");
    createLeagueInvoiceBtn.setOnAction(e -> showCreateLeagueInvoiceDialog());

    Button createTournamentInvoiceBtn = new Button("Create Tournament Invoice");
    createTournamentInvoiceBtn.setOnAction(e -> showCreateTournamentInvoiceDialog());

    Button resetFiltersBtn = new Button("Reset Filters");
    resetFiltersBtn.setOnAction(e -> {
      clearFilters();
      saveFilters();
    });

    Button voidBtn = new Button("Void");
    voidBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
    voidBtn.setOnAction(e -> {
      ObservableList<Transaction> selected = table.getSelectionModel().getSelectedItems();
      if (selected.isEmpty()) {
        showError("Please select one or more transactions to void");
        return;
      }

      Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
      confirm.setTitle("Confirm Void");
      confirm.setHeaderText("Void " + selected.size() + " transaction(s)?");
      confirm.setContentText("This action cannot be undone.");
      confirm.showAndWait().ifPresent(result -> {
        if (result == ButtonType.OK) {
          for (Transaction tx : selected) {
            tx.setStatus(Transaction.Status.VOID);
            transactionService.save(tx);
          }
          refresh();
        }
      });
    });

    Button paidBtn = new Button("Mark as Paid");
    paidBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
    paidBtn.setOnAction(e -> {
      ObservableList<Transaction> selected = table.getSelectionModel().getSelectedItems();
      if (selected.isEmpty()) {
        showError("Please select one or more transactions to mark as paid");
        return;
      }

      Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
      confirm.setTitle("Confirm Payment");
      confirm.setHeaderText("Mark " + selected.size() + " transaction(s) as paid?");
      confirm.setContentText("The status will be changed to PAID.");
      confirm.showAndWait().ifPresent(result -> {
        if (result == ButtonType.OK) {
          for (Transaction tx : selected) {
            tx.setStatus(Transaction.Status.PAID);
            transactionService.save(tx);
          }
          refresh();
        }
      });
    });

    HBox controls = new HBox(10, addBtn, createLeagueInvoiceBtn, createTournamentInvoiceBtn, resetFiltersBtn, paidBtn, voidBtn, importBtn, exportBtn);
    controls.setAlignment(Pos.CENTER_LEFT);

    container.getChildren().addAll(title, controls, table);
    refresh();
    return container;
  }

  private void createColumnHeaderFilters(
    TableColumn<?, ?> partyTypeCol,
    TableColumn<?, ?> partyNameCol,
    TableColumn<?, ?> leagueCol,
    TableColumn<?, ?> teamCol,
    TableColumn<?, ?> playerCol,
    TableColumn<?, ?> categoryCol,
    TableColumn<?, ?> dateCol,
    TableColumn<?, ?> amountCol,
    TableColumn<?, ?> statusCol,
    TableColumn<?, ?> notesCol) {

    // Party Type Filter
    filterPartyType = new TextField();
    filterPartyType.setPromptText("Type");
    VBox partyTypeHeader = createHeaderWithFilter(partyTypeCol, filterPartyType);
    filterPartyType.textProperty().addListener((obs, o, n) -> {
      applyFilters();
      saveFilters();
    });
    partyTypeCol.setText("");
    partyTypeCol.setGraphic(partyTypeHeader);

    // Party Name Filter
    filterPartyName = new TextField();
    filterPartyName.setPromptText("Name");
    VBox partyNameHeader = createHeaderWithFilter(partyNameCol, filterPartyName);
    filterPartyName.textProperty().addListener((obs, o, n) -> {
      applyFilters();
      saveFilters();
    });
    partyNameCol.setText("");
    partyNameCol.setGraphic(partyNameHeader);

    // League Name Filter
    filterLeagueName = new TextField();
    filterLeagueName.setPromptText("League");
    VBox leagueNameHeader = createHeaderWithFilter(leagueCol, filterLeagueName);
    filterLeagueName.textProperty().addListener((obs, o, n) -> {
      applyFilters();
      saveFilters();
    });
    leagueCol.setText("");
    leagueCol.setGraphic(leagueNameHeader);

    // Team Name Filter
    filterTeamName = new TextField();
    filterTeamName.setPromptText("Team");
    VBox teamNameHeader = createHeaderWithFilter(teamCol, filterTeamName);
    filterTeamName.textProperty().addListener((obs, o, n) -> {
      applyFilters();
      saveFilters();
    });
    teamCol.setText("");
    teamCol.setGraphic(teamNameHeader);

    // Player Name Filter
    filterPlayerName = new TextField();
    filterPlayerName.setPromptText("Contact");
    VBox playerNameHeader = createHeaderWithFilter(playerCol, filterPlayerName);
    filterPlayerName.textProperty().addListener((obs, o, n) -> {
      applyFilters();
      saveFilters();
    });
    playerCol.setText("");
    playerCol.setGraphic(playerNameHeader);

    // Category Filter
    filterCategory = new TextField();
    filterCategory.setPromptText("Cat");
    VBox categoryHeader = createHeaderWithFilter(categoryCol, filterCategory);
    filterCategory.textProperty().addListener((obs, o, n) -> {
      applyFilters();
      saveFilters();
    });
    categoryCol.setText("");
    categoryCol.setGraphic(categoryHeader);

    // Date Filter
    filterDate = new TextField();
    filterDate.setPromptText("Date");
    VBox dateHeader = createHeaderWithFilter(dateCol, filterDate);
    filterDate.textProperty().addListener((obs, o, n) -> {
      applyFilters();
      saveFilters();
    });
    dateCol.setText("");
    dateCol.setGraphic(dateHeader);

    // Amount Filter
    filterAmount = new TextField();
    filterAmount.setPromptText("$");
    VBox amountHeader = createHeaderWithFilter(amountCol, filterAmount);
    filterAmount.textProperty().addListener((obs, o, n) -> {
      applyFilters();
      saveFilters();
    });
    amountCol.setText("");
    amountCol.setGraphic(amountHeader);

    // Status Filter (Multi-select)
    filterStatus = new ComboBox<>();
    filterStatus.setPromptText("Status");

    statusContextMenu = new ContextMenu();
    for (Transaction.Status status : Transaction.Status.values()) {
      CheckMenuItem item = new CheckMenuItem(status.name());
      item.selectedProperty().addListener((obs, oldVal, newVal) -> {
        if (newVal) {
          if (!selectedStatuses.contains(status)) {
            selectedStatuses.add(status);
          }
        } else {
          selectedStatuses.remove(status);
        }
        applyFilters();
      });
      statusContextMenu.getItems().add(item);
    }

    filterStatus.setOnMousePressed(e -> {
      statusContextMenu.show(filterStatus, e.getScreenX(), e.getScreenY());
    });

    VBox statusHeader = createHeaderWithFilter(statusCol, filterStatus);
    statusCol.setText("");
    statusCol.setGraphic(statusHeader);

    // Notes Filter
    filterNotes = new TextField();
    filterNotes.setPromptText("Notes");
    VBox notesHeader = createHeaderWithFilter(notesCol, filterNotes);
    filterNotes.textProperty().addListener((obs, o, n) -> {
      applyFilters();
      saveFilters();
    });
    notesCol.setText("");
    notesCol.setGraphic(notesHeader);
  }

  private VBox createHeaderWithFilter(TableColumn<?, ?> column, Control filterControl) {
    VBox vbox = new VBox(2);

    // Add column title
    Label headerLabel = new Label(column.getText());

    // Constrain filter control to fit within column bounds
    filterControl.setStyle("-fx-padding: 2 2 2 2;");
    filterControl.setMaxWidth(Double.MAX_VALUE);
    filterControl.setPrefWidth(100);
    filterControl.setMinWidth(50);

    vbox.getChildren().addAll(headerLabel, filterControl);

    return vbox;
  }

  private void showError(String msg) {
    Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
    a.showAndWait();
  }

  private void showAddTransactionDialog() {
    Dialog<Transaction> dialog = new Dialog<>();
    dialog.setTitle("Add Transaction");
    dialog.setHeaderText("Create a new transaction");

    ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

    VBox content = new VBox(10);
    content.setPadding(new Insets(10));

    // Party Type selection
    ComboBox<Transaction.PartyType> partyTypeCombo = new ComboBox<>();
    partyTypeCombo.getItems().addAll(Transaction.PartyType.values());
    partyTypeCombo.setValue(Transaction.PartyType.TEAM);

    // Party selection (dynamically populated based on party type)
    SearchableComboBox<PartyOption> partyCombo = new SearchableComboBox<>();
    partyCombo.setPromptText("Select Team or Player");

    // Update party combo when party type changes
    partyTypeCombo.setOnAction(e -> {
      partyCombo.getSelectionModel().clearSelection();
      partyCombo.getItems().clear();
      if (partyTypeCombo.getValue() == Transaction.PartyType.TEAM) {
        teamService.findAll().forEach(team ->
          partyCombo.getItems().add(new PartyOption(team.getId(), team.getName()))
        );
      } else if (partyTypeCombo.getValue() == Transaction.PartyType.PLAYER) {
        playerService.findAll().forEach(player ->
          partyCombo.getItems().add(new PartyOption(player.getId(),
            player.getFirstName() + " " + player.getLastName()))
        );
      }
      if (!partyCombo.getItems().isEmpty()) {
        partyCombo.setValue(partyCombo.getItems().get(0));
      } else {
        partyCombo.setValue(null);
      }
    });
    partyTypeCombo.fireEvent(new ActionEvent()); // Trigger initial population

    // Category selection
    ComboBox<Transaction.Category> categoryCombo = new ComboBox<>();
    categoryCombo.getItems().addAll(Transaction.Category.values());
    categoryCombo.setValue(Transaction.Category.INVOICE);

    // Date picker
    DatePicker datePicker = new DatePicker(LocalDate.now());

    // Amount field
    TextField amountField = new TextField("0.00");

    // Status selection
    ComboBox<Transaction.Status> statusCombo = new ComboBox<>();
    statusCombo.getItems().addAll(Transaction.Status.values());
    statusCombo.setValue(Transaction.Status.PENDING);

    // Notes field
    TextArea notesField = new TextArea();
    notesField.setPromptText("Enter notes");
    notesField.setPrefRowCount(3);

    content.getChildren().addAll(
      new Label("Party Type:"), partyTypeCombo,
      new Label("Party:"), partyCombo,
      new Label("Category:"), categoryCombo,
      new Label("Date:"), datePicker,
      new Label("Amount:"), amountField,
      new Label("Status:"), statusCombo,
      new Label("Notes:"), notesField
    );

    dialog.getDialogPane().setContent(content);

    dialog.setResultConverter(dialogButton -> {
      if (dialogButton == saveButtonType) {
        try {
          Transaction tx = new Transaction();
          tx.setPartyType(partyTypeCombo.getValue());
          tx.setPartyId(partyCombo.getValue().id);
          tx.setCategory(categoryCombo.getValue());
          tx.setDate(datePicker.getValue());
          tx.setAmount(Double.parseDouble(amountField.getText()));
          tx.setStatus(statusCombo.getValue());
          tx.setNotes(notesField.getText());
          return tx;
        } catch (Exception e) {
          showError("Invalid input: " + e.getMessage());
          return null;
        }
      }
      return null;
    });

    dialog.showAndWait().ifPresent(tx -> {
      transactionService.save(tx);
      refresh();
    });
  }

  private void showCreateLeagueInvoiceDialog() {
    Dialog<Transaction> dialog = new Dialog<>();
    dialog.setTitle("Create League Invoice");
    dialog.setHeaderText("Create a new league invoice for a team");

    ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

    VBox content = new VBox(10);
    content.setPadding(new Insets(10));

    ComboBox<League> leagueCombo = new ComboBox<>(FXCollections.observableArrayList(leagueService.findAll()));
    leagueCombo.setPromptText("Select League");
    // Configure League ComboBox display BEFORE use
    leagueCombo.setConverter(new javafx.util.StringConverter<League>() {
      @Override public String toString(League l) { return l == null ? "" : l.getName(); }
      @Override public League fromString(String s) { return null; }
    });
    leagueCombo.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
      @Override protected void updateItem(League item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty || item == null ? null : item.getName());
      }
    });
    leagueCombo.setButtonCell(leagueCombo.getCellFactory().call(null));

    org.controlsfx.control.SearchableComboBox<Team> teamCombo = new org.controlsfx.control.SearchableComboBox<>();
    teamCombo.setPromptText("Select Team");
    // Configure Team ComboBox display BEFORE items set
    teamCombo.setConverter(new javafx.util.StringConverter<Team>() {
      @Override public String toString(Team t) { return t == null ? "" : t.getName(); }
      @Override public Team fromString(String s) { return null; }
    });
    teamCombo.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
      @Override protected void updateItem(Team item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty || item == null ? null : item.getName());
      }
    });
    teamCombo.setButtonCell(teamCombo.getCellFactory().call(null));

    CheckBox billAllTeams = new CheckBox("Bill all teams in this league");

    // Populate teams when a league is selected
    leagueCombo.setOnAction(e -> {
      League selectedLeague = leagueCombo.getValue();
      if (selectedLeague != null) {
        teamCombo.setItems(FXCollections.observableArrayList(
          teamService.findByLeagueId(selectedLeague.getId())
        ));
        teamCombo.getSelectionModel().clearSelection();
      } else {
        teamCombo.setItems(FXCollections.observableArrayList());
      }
    });

    TextField amountField = new TextField("0.00");
    DatePicker datePicker = new DatePicker(LocalDate.now());
    TextArea notesField = new TextArea();
    notesField.setPromptText("Enter notes");
    notesField.setPrefRowCount(3);

    content.getChildren().addAll(
      new Label("League:"), leagueCombo,
      billAllTeams,
      new Label("Team:"), teamCombo,
      new Label("Amount:"), amountField,
      new Label("Date:"), datePicker,
      new Label("Notes:"), notesField
    );

    dialog.getDialogPane().setContent(content);

    dialog.setResultConverter(dialogButton -> {
      if (dialogButton == saveButtonType) {
        try {
          if (billAllTeams.isSelected()) {
            League selectedLeague = leagueCombo.getValue();
            if (selectedLeague == null) throw new IllegalArgumentException("Select a league");
            List<Team> teams = teamService.findByLeagueId(selectedLeague.getId());
            double amt = Double.parseDouble(amountField.getText());
            LocalDate date = datePicker.getValue();
            String notes = notesField.getText();
            for (Team t : teams) {
              Transaction tx = new Transaction();
              tx.setPartyType(Transaction.PartyType.TEAM);
              tx.setPartyId(t.getId());
              tx.setCategory(Transaction.Category.INVOICE);
              tx.setDate(date);
              tx.setAmount(amt);
              tx.setStatus(Transaction.Status.PENDING);
              tx.setNotes("League registration: " + selectedLeague.getName() + " - " + notes);
              transactionService.save(tx);
            }
            refresh();
            return null; // We've already saved all
          } else {
            Transaction tx = new Transaction();
            tx.setPartyType(Transaction.PartyType.TEAM);
            tx.setPartyId(teamCombo.getValue().getId());
            tx.setCategory(Transaction.Category.INVOICE);
            tx.setDate(datePicker.getValue());
            tx.setAmount(Double.parseDouble(amountField.getText()));
            tx.setStatus(Transaction.Status.PENDING);
            tx.setNotes("League registration: " + (leagueCombo.getValue() != null ? leagueCombo.getValue().getName() : "") + " - " + notesField.getText());
            return tx;
          }
        } catch (Exception e) {
          showError("Invalid input: " + e.getMessage());
          return null;
        }
      }
      return null;
    });

    dialog.showAndWait().ifPresent(tx -> {
      transactionService.save(tx);
      refresh();
    });
  }

  private void showCreateTournamentInvoiceDialog() {
    Dialog<Transaction> dialog = new Dialog<>();
    dialog.setTitle("Create Tournament Invoice");
    dialog.setHeaderText("Create a new tournament invoice for a team");

    ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

    VBox content = new VBox(10);
    content.setPadding(new Insets(10));

    ComboBox<Tournament> tournamentCombo = new ComboBox<>(FXCollections.observableArrayList(tournamentService.findAll()));
    tournamentCombo.setPromptText("Select Tournament");
    // Configure Tournament combo BEFORE use
    tournamentCombo.setConverter(new javafx.util.StringConverter<Tournament>() {
      @Override public String toString(Tournament t) { return t == null ? "" : t.getName(); }
      @Override public Tournament fromString(String s) { return null; }
    });
    tournamentCombo.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
      @Override protected void updateItem(Tournament item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty || item == null ? null : item.getName());
      }
    });
    tournamentCombo.setButtonCell(tournamentCombo.getCellFactory().call(null));

    org.controlsfx.control.SearchableComboBox<Team> teamCombo = new org.controlsfx.control.SearchableComboBox<>();
    teamCombo.setPromptText("Select Team");
    teamCombo.setConverter(new javafx.util.StringConverter<Team>() {
      @Override public String toString(Team t) { return t == null ? "" : t.getName(); }
      @Override public Team fromString(String s) { return null; }
    });
    teamCombo.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
      @Override protected void updateItem(Team item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty || item == null ? null : item.getName());
      }
    });
    teamCombo.setButtonCell(teamCombo.getCellFactory().call(null));

    // Populate teams from tournament registrations when a tournament is selected
    tournamentCombo.setOnAction(e -> {
      Tournament t = tournamentCombo.getValue();
      if (t != null) {
        var regs = tournamentRegistrationService.findByTournamentIdAndStatus(t.getId(), com.scheduleengine.tournament.domain.TournamentRegistration.RegistrationStatus.APPROVED);
        List<Team> teams = regs.stream().map(r -> r.getTeam()).distinct().toList();
        teamCombo.setItems(FXCollections.observableArrayList(teams));
        teamCombo.getSelectionModel().clearSelection();
      } else {
        teamCombo.setItems(FXCollections.observableArrayList());
      }
    });

    CheckBox billAllTeams = new CheckBox("Bill all registered teams");

    TextField amountField = new TextField("0.00");
    DatePicker datePicker = new DatePicker(LocalDate.now());
    TextArea notesField = new TextArea();
    notesField.setPromptText("Enter notes");
    notesField.setPrefRowCount(3);

    content.getChildren().addAll(
      new Label("Tournament:"), tournamentCombo,
      billAllTeams,
      new Label("Team:"), teamCombo,
      new Label("Amount:"), amountField,
      new Label("Date:"), datePicker,
      new Label("Notes:"), notesField
    );

    dialog.getDialogPane().setContent(content);

    dialog.setResultConverter(dialogButton -> {
      if (dialogButton == saveButtonType) {
        try {
          double amt = Double.parseDouble(amountField.getText());
          LocalDate date = datePicker.getValue();
          String notes = notesField.getText();
          Tournament t = tournamentCombo.getValue();
          if (t == null) throw new IllegalArgumentException("Select a tournament");

          if (billAllTeams.isSelected()) {
            var regs = tournamentRegistrationService.findByTournamentIdAndStatus(t.getId(), com.scheduleengine.tournament.domain.TournamentRegistration.RegistrationStatus.APPROVED);
            List<Team> teams = regs.stream().map(r -> r.getTeam()).distinct().toList();
            for (Team team : teams) {
              Transaction tx = new Transaction();
              tx.setPartyType(Transaction.PartyType.TEAM);
              tx.setPartyId(team.getId());
              tx.setCategory(Transaction.Category.INVOICE);
              tx.setDate(date);
              tx.setAmount(amt);
              tx.setStatus(Transaction.Status.PENDING);
              tx.setNotes("Tournament registration: " + t.getName() + " - " + notes);
              transactionService.save(tx);
            }
            refresh();
            return null;
          } else {
            Team selTeam = teamCombo.getValue();
            if (selTeam == null) throw new IllegalArgumentException("Select a team");
            Transaction tx = new Transaction();
            tx.setPartyType(Transaction.PartyType.TEAM);
            tx.setPartyId(selTeam.getId());
            tx.setCategory(Transaction.Category.INVOICE);
            tx.setDate(date);
            tx.setAmount(amt);
            tx.setStatus(Transaction.Status.PENDING);
            tx.setNotes("Tournament registration: " + t.getName() + " - " + notes);
            return tx;
          }
        } catch (Exception e) {
          showError("Invalid input: " + e.getMessage());
          return null;
        }
      }
      return null;
    });

    dialog.showAndWait().ifPresent(tx -> {
      if (tx != null) {
        transactionService.save(tx);
        refresh();
      }
    });
  }

    // Helper class for ComboBox items
    private record PartyOption(Long id, String name) {

        @Override
        public String toString() {
            return name;
        }
    }
}

