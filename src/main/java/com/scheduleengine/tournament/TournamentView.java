package com.scheduleengine.tournament;

import com.scheduleengine.common.TableColumnUtil;
import com.scheduleengine.common.TablePreferencesUtil;
import com.scheduleengine.league.domain.League;
import com.scheduleengine.league.service.LeagueService;
import com.scheduleengine.team.domain.Team;
import com.scheduleengine.team.service.TeamService;
import com.scheduleengine.tournament.domain.Tournament;
import com.scheduleengine.tournament.domain.TournamentRegistration;
import com.scheduleengine.tournament.service.TournamentRegistrationService;
import com.scheduleengine.tournament.service.TournamentService;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

import java.time.LocalDate;

public class TournamentView {

  private final TournamentService tournamentService;
  private final TournamentRegistrationService registrationService;
  private final LeagueService leagueService;
  private final TeamService teamService;
  private TableView<Tournament> table;
  private final ObservableList<Tournament> data;
  private ComboBox<Tournament.TournamentType> typeFilter;
  private final TournamentBracketEditorView bracketEditorView;
  private com.scheduleengine.navigation.NavigationHandler navigationHandler;

  public TournamentView(TournamentService tournamentService, TournamentRegistrationService registrationService,
                        LeagueService leagueService, TeamService teamService) {
    this.tournamentService = tournamentService;
    this.registrationService = registrationService;
    this.leagueService = leagueService;
    this.teamService = teamService;
    this.data = FXCollections.observableArrayList();
    this.bracketEditorView = new TournamentBracketEditorView(tournamentService, registrationService);
  }

  public void setNavigationHandler(com.scheduleengine.navigation.NavigationHandler navigationHandler) {
    this.navigationHandler = navigationHandler;
  }

  public VBox getView() {
    VBox vbox = new VBox(10);
    vbox.setPadding(new Insets(10));

    HBox topBox = new HBox(10);
    Label title = new Label("Tournaments");
    title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    typeFilter = new ComboBox<>(FXCollections.observableArrayList(Tournament.TournamentType.values()));
    typeFilter.setPromptText("Filter by Type");
    typeFilter.valueProperty().addListener((obs, oldVal, newVal) -> loadData());

    Button clearFilter = new Button("Clear");
    clearFilter.setOnAction(e -> {
      typeFilter.setValue(null);
      loadData();
    });

    Button addButton = new Button("Add Tournament");
    addButton.setStyle("-fx-background-color: #667eea; -fx-text-fill: white;");
    addButton.setOnAction(e -> showAddDialog());

    Button refreshButton = new Button("Refresh");
    refreshButton.setOnAction(e -> loadData());

    Button deleteButton = new Button("Delete Selected");
    //deleteButton.setOnAction(e -> deleteSelectedTournament());

    topBox.getChildren().addAll(title, spacer, new Label("Type:"), typeFilter, clearFilter, addButton, refreshButton, deleteButton);

    table = new TableView<>();
    table.setItems(data);
    table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

    TableColumn<Tournament, Long> idCol = new TableColumn<>("ID");
    idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
    idCol.setPrefWidth(50);
    idCol.setVisible(false);

    TableColumn<Tournament, String> nameCol = new TableColumn<>("Name");
    nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
    nameCol.setPrefWidth(200);

    TableColumn<Tournament, String> typeCol = new TableColumn<>("Type");
    typeCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getType().toString()));
    typeCol.setPrefWidth(130);

    TableColumn<Tournament, LocalDate> startCol = new TableColumn<>("Start Date");
    startCol.setCellValueFactory(new PropertyValueFactory<>("startDate"));
    startCol.setPrefWidth(110);

    TableColumn<Tournament, LocalDate> endCol = new TableColumn<>("End Date");
    endCol.setCellValueFactory(new PropertyValueFactory<>("endDate"));
    endCol.setPrefWidth(110);

    TableColumn<Tournament, String> leagueCol = new TableColumn<>("League");
    leagueCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(
      c.getValue().getLeague() != null ? c.getValue().getLeague().getName() : "Open/All"
    ));
    leagueCol.setPrefWidth(150);

    TableColumn<Tournament, String> statusCol = new TableColumn<>("Status");
    statusCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getStatus().toString()));
    statusCol.setPrefWidth(110);

    TableColumn<Tournament, String> teamsCol = new TableColumn<>("Teams");
    teamsCol.setCellValueFactory(c -> {
      long count = registrationService.countApprovedTeams(c.getValue().getId());
      Integer max = c.getValue().getMaxTeams();
      String text = String.valueOf(count);
      if (max != null) {
        text += "/" + max;
      }
      return new ReadOnlyStringWrapper(text);
    });
    // Numeric comparator on registered count
    teamsCol.setComparator((a, b) -> {
      int ca = TableColumnUtil.parseLeadingInt(a);
      int cb = TableColumnUtil.parseLeadingInt(b);
      return Integer.compare(ca, cb);
    });
    teamsCol.setPrefWidth(80);

    TableColumn<Tournament, Void> actionCol = new TableColumn<>("Actions");
    actionCol.setPrefWidth(280);
    actionCol.setCellFactory(col -> new TableCell<>() {
      private final Button viewBtn = new Button("View Details");
      private final Button bracketBtn = new Button("Bracket");
      private final Button registerBtn = new Button("Register");
      private final Tooltip registerHint = new Tooltip("Set a league on the tournament to enable registration.");

      {
        viewBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white;");
        viewBtn.setOnAction(e -> {
          Tournament t = getTableView().getItems().get(getIndex());
          if (navigationHandler != null) {
            com.scheduleengine.navigation.NavigationContext ctx = new com.scheduleengine.navigation.NavigationContext()
              .navigateTo("tournaments", "Tournaments")
              .navigateTo("tournament-detail", t.getName(), t);
            navigationHandler.navigate(ctx);
          }
        });
        bracketBtn.setOnAction(e -> openBracketEditor(getTableView().getItems().get(getIndex())));
        bracketBtn.setStyle("-fx-background-color: #ffa502; -fx-text-fill: white;");
        registerBtn.setOnAction(e -> showRegisterDialog(getTableView().getItems().get(getIndex())));
        registerBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
      }

      @Override
      protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
          setGraphic(null);
          return;
        }
        Tournament t = getTableView().getItems().get(getIndex());
        boolean disableRegister = t.getType() != Tournament.TournamentType.OPEN && t.getLeague() == null;
        registerBtn.setDisable(disableRegister);
        registerBtn.setTooltip(disableRegister ? registerHint : null);
        setGraphic(new HBox(6, viewBtn, bracketBtn, registerBtn));
      }
    });

    table.getColumns().addAll(idCol, nameCol, typeCol, statusCol, startCol, endCol, leagueCol, actionCol);

    // Persist table state (widths, visibility, sort)
    com.scheduleengine.common.TablePreferencesUtil.bind(table, "tournament");
    com.scheduleengine.common.TablePreferencesUtil.attachToggleMenu(table, "tournament");

    // Setup column width persistence (unique table id)
    TablePreferencesUtil.setupTableColumnPersistence(table, "tournament.table");

    loadData();

    vbox.getChildren().addAll(topBox, table);
    VBox.setVgrow(table, Priority.ALWAYS);

    return vbox;
  }

  public void refresh() {
    loadData();
  }

  private void loadData() {
    data.clear();
    Tournament.TournamentType selectedType = typeFilter != null ? typeFilter.getValue() : null;
    if (selectedType != null) {
      data.addAll(tournamentService.findByType(selectedType));
    } else {
      data.addAll(tournamentService.findAll());
    }
  }

  private void showAddDialog() {
    Dialog<Tournament> dialog = new Dialog<>();
    dialog.setTitle("Add Tournament");

    ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

    GridPane grid = buildTournamentForm(null);
    dialog.getDialogPane().setContent(grid);

    dialog.setResultConverter(btn -> {
      if (btn == saveButtonType) {
        return buildTournamentFromForm(grid, null);
      }
      return null;
    });

    dialog.showAndWait().ifPresent(tournament -> {
      tournamentService.save(tournament);
      loadData();
    });
  }

  private void showEditDialog(Tournament tournament) {
    Dialog<Tournament> dialog = new Dialog<>();
    dialog.setTitle("Edit Tournament");

    ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

    GridPane grid = buildTournamentForm(tournament);
    dialog.getDialogPane().setContent(grid);

    dialog.setResultConverter(btn -> {
      if (btn == saveButtonType) {
        return buildTournamentFromForm(grid, tournament);
      }
      return null;
    });

    dialog.showAndWait().ifPresent(updated -> {
      tournamentService.update(tournament.getId(), updated);
      loadData();
    });
  }

  private GridPane buildTournamentForm(Tournament existing) {
    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 150, 10, 10));

    TextField nameField = new TextField(existing != null ? existing.getName() : "");
    TextArea descField = new TextArea(existing != null && existing.getDescription() != null ? existing.getDescription() : "");
    descField.setPrefRowCount(3);

    ComboBox<Tournament.TournamentType> typeCombo = new ComboBox<>(FXCollections.observableArrayList(Tournament.TournamentType.values()));
    if (existing != null) typeCombo.setValue(existing.getType());

    DatePicker startPicker = new DatePicker(existing != null ? existing.getStartDate() : null);
    DatePicker endPicker = new DatePicker(existing != null ? existing.getEndDate() : null);
    DatePicker regDeadlinePicker = new DatePicker(existing != null ? existing.getRegistrationDeadline() : null);

    ComboBox<League> leagueCombo = new ComboBox<>(FXCollections.observableArrayList(leagueService.findAll()));
    leagueCombo.setPromptText("LEAGUE");
    leagueCombo.setConverter(new StringConverter<>() {
      @Override
      public String toString(League l) {
        return l == null ? "" : l.getName();
      }

      @Override
      public League fromString(String s) {
        return null;
      }
    });
    leagueCombo.setCellFactory(lv -> new ListCell<>() {
      @Override
      protected void updateItem(League item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty || item == null ? null : item.getName());
      }
    });
    if (existing != null) leagueCombo.setValue(existing.getLeague());

    Spinner<Integer> maxTeamsSpinner = new Spinner<>(4, 128, existing != null && existing.getMaxTeams() != null ? existing.getMaxTeams() : 16);

    TextField entryFeeField = new TextField(existing != null && existing.getEntryFee() != null ? existing.getEntryFee().toString() : "0.00");
    TextField locationField = new TextField(existing != null && existing.getLocation() != null ? existing.getLocation() : "");

    ComboBox<Tournament.TournamentStatus> statusCombo = new ComboBox<>(FXCollections.observableArrayList(Tournament.TournamentStatus.values()));
    if (existing != null) statusCombo.setValue(existing.getStatus());
    else statusCombo.setValue(Tournament.TournamentStatus.DRAFT);

    int row = 0;
    grid.add(new Label("Name:"), 0, row);
    grid.add(nameField, 1, row++);
    grid.add(new Label("Description:"), 0, row);
    grid.add(descField, 1, row++);
    grid.add(new Label("Type:"), 0, row);
    grid.add(typeCombo, 1, row++);
    grid.add(new Label("Start Date:"), 0, row);
    grid.add(startPicker, 1, row++);
    grid.add(new Label("End Date:"), 0, row);
    grid.add(endPicker, 1, row++);
    grid.add(new Label("League:"), 0, row);
    grid.add(leagueCombo, 1, row++);
    grid.add(new Label("Max Teams:"), 0, row);
    grid.add(maxTeamsSpinner, 1, row++);
    grid.add(new Label("Registration Deadline:"), 0, row);
    grid.add(regDeadlinePicker, 1, row++);
    grid.add(new Label("Entry Fee:"), 0, row);
    grid.add(entryFeeField, 1, row++);
    grid.add(new Label("Location:"), 0, row);
    grid.add(locationField, 1, row++);
    grid.add(new Label("Status:"), 0, row);
    grid.add(statusCombo, 1, row++);

    return grid;
  }

  private Tournament buildTournamentFromForm(GridPane grid, Tournament base) {
    TextField nameField = (TextField) grid.getChildren().filtered(n -> GridPane.getRowIndex(n) == 0 && GridPane.getColumnIndex(n) == 1).get(0);
    TextArea descField = (TextArea) grid.getChildren().filtered(n -> GridPane.getRowIndex(n) == 1 && GridPane.getColumnIndex(n) == 1).get(0);
    @SuppressWarnings("unchecked")
    ComboBox<Tournament.TournamentType> typeCombo = (ComboBox<Tournament.TournamentType>) grid.getChildren().filtered(n -> GridPane.getRowIndex(n) == 2 && GridPane.getColumnIndex(n) == 1).get(0);
    DatePicker startPicker = (DatePicker) grid.getChildren().filtered(n -> GridPane.getRowIndex(n) == 3 && GridPane.getColumnIndex(n) == 1).get(0);
    DatePicker endPicker = (DatePicker) grid.getChildren().filtered(n -> GridPane.getRowIndex(n) == 4 && GridPane.getColumnIndex(n) == 1).get(0);
    @SuppressWarnings("unchecked")
    ComboBox<League> leagueCombo = (ComboBox<League>) grid.getChildren().filtered(n -> GridPane.getRowIndex(n) == 5 && GridPane.getColumnIndex(n) == 1).get(0);
    @SuppressWarnings("unchecked")
    Spinner<Integer> maxTeamsSpinner = (Spinner<Integer>) grid.getChildren().filtered(n -> GridPane.getRowIndex(n) == 6 && GridPane.getColumnIndex(n) == 1).get(0);
    DatePicker regDeadlinePicker = (DatePicker) grid.getChildren().filtered(n -> GridPane.getRowIndex(n) == 7 && GridPane.getColumnIndex(n) == 1).get(0);
    TextField entryFeeField = (TextField) grid.getChildren().filtered(n -> GridPane.getRowIndex(n) == 8 && GridPane.getColumnIndex(n) == 1).get(0);
    TextField locationField = (TextField) grid.getChildren().filtered(n -> GridPane.getRowIndex(n) == 9 && GridPane.getColumnIndex(n) == 1).get(0);
    @SuppressWarnings("unchecked")
    ComboBox<Tournament.TournamentStatus> statusCombo = (ComboBox<Tournament.TournamentStatus>) grid.getChildren().filtered(n -> GridPane.getRowIndex(n) == 10 && GridPane.getColumnIndex(n) == 1).get(0);

    if (nameField.getText().isBlank() || typeCombo.getValue() == null ||
      startPicker.getValue() == null || endPicker.getValue() == null) {
      showError("Validation", "Name, type, start date and end date are required");
      return null;
    }

    Tournament t = base == null ? new Tournament() : base;
    t.setName(nameField.getText());
    t.setDescription(descField.getText());
    t.setType(typeCombo.getValue());
    t.setStartDate(startPicker.getValue());
    t.setEndDate(endPicker.getValue());
    t.setLeague(leagueCombo.getValue());
    t.setMaxTeams(maxTeamsSpinner.getValue());
    t.setRegistrationDeadline(regDeadlinePicker.getValue());
    try {
      t.setEntryFee(Double.parseDouble(entryFeeField.getText()));
    } catch (NumberFormatException e) {
      t.setEntryFee(0.0);
    }
    t.setLocation(locationField.getText());
    t.setStatus(statusCombo.getValue());

    return t;
  }

  private void showRegisterDialog(Tournament tournament) {
    Dialog<TournamentRegistration> dialog = new Dialog<>();
    dialog.setTitle("Register Team for Tournament");
    dialog.setHeaderText("Tournament: " + tournament.getName());

    ButtonType registerButtonType = new ButtonType("Register", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(registerButtonType, ButtonType.CANCEL);

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 150, 10, 10));

    // Multi-select teams list instead of single ComboBox
    ListView<Team> teamList = new ListView<>();
    teamList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    teamList.setPrefHeight(220);
    teamList.setCellFactory(lv -> new ListCell<>() {
      @Override
      protected void updateItem(Team item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty || item == null ? null : item.getName());
      }
    });

    // Filter teams based on tournament type/league:
    // OPEN -> allow any team; otherwise restrict to same league
    final boolean isOpen = tournament.getType() == Tournament.TournamentType.OPEN;
    final League league = tournament.getLeague();
    if (isOpen) {
      teamList.setItems(FXCollections.observableArrayList(teamService.findAll()));
    } else if (league != null) {
      teamList.setItems(FXCollections.observableArrayList(teamService.findByLeagueId(league.getId())));
    } else {
      // Non-OPEN and no league — block selection and require league to be set
      teamList.setItems(FXCollections.observableArrayList());
      teamList.setDisable(true);
    }

    Label hint = new Label();
    if (!isOpen && league == null) {
      hint.setText("This tournament is league-scoped. Please set a league on the tournament before registering teams.");
      hint.setStyle("-fx-text-fill: #e74c3c;");
    } else if (!isOpen) {
      hint.setText("Only teams from league: " + league.getName() + " are eligible.");
      hint.setStyle("-fx-text-fill: #7f8c8d;");
    }

    // Add hint below selector when applicable
    if (!hint.getText().isBlank()) {
      grid.add(hint, 1, 2);
    }

    TextArea notesField = new TextArea();
    notesField.setPrefRowCount(3);
    notesField.setPromptText("Optional notes...");

    grid.add(new Label("Teams:"), 0, 0);
    grid.add(teamList, 1, 0);
    grid.add(new Label("Notes:"), 0, 1);
    grid.add(notesField, 1, 1);

    dialog.getDialogPane().setContent(grid);

    dialog.setResultConverter(btn -> {
      if (btn == registerButtonType) {
        if (!isOpen && league == null) {
          showError("Validation", "Set the tournament's league first (non-OPEN tournaments require a league).");
          return null;
        }
        ObservableList<Team> selectedTeams = teamList.getSelectionModel().getSelectedItems();
        if (selectedTeams == null || selectedTeams.isEmpty()) {
          showError("Validation", "Please select at least one team");
          return null;
        }
        try {
          for (Team t : selectedTeams) {
            registrationService.registerTeam(tournament, t, notesField.getText());
          }
          return new TournamentRegistration(); // Dummy non-null to trigger success flow
        } catch (IllegalStateException e) {
          showError("Registration Error", e.getMessage());
          return null;
        }
      }
      return null;
    });

    dialog.showAndWait().ifPresent(reg -> {
      Alert a = new Alert(Alert.AlertType.INFORMATION);
      a.setTitle("Registration Successful");
      a.setHeaderText("Teams Registered");
      int count = teamList.getSelectionModel().getSelectedItems().size();
      String msg = count + " team(s) registered for tournament.";
      a.setContentText(msg);
      a.showAndWait();
      loadData();
    });
  }

  private void deleteTournament(Tournament tournament) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Delete Tournament");
    alert.setHeaderText("Are you sure?");
    alert.setContentText("Do you want to delete the tournament: " + tournament.getName() + "?");
    alert.showAndWait().ifPresent(response -> {
      if (response == ButtonType.OK) {
        tournamentService.deleteById(tournament.getId());
        loadData();
      }
    });
  }

  private void showError(String title, String msg) {
    Alert a = new Alert(Alert.AlertType.ERROR);
    a.setTitle(title);
    a.setHeaderText(null);
    a.setContentText(msg);
    a.showAndWait();
  }

  private void openBracketEditor(Tournament tournament) {
    Dialog<Void> dialog = new Dialog<>();
    dialog.setTitle("Tournament Bracket - " + tournament.getName());
    dialog.setWidth(1000);
    dialog.setHeight(700);

    VBox bracketView = bracketEditorView.getView(tournament);
    dialog.getDialogPane().setContent(bracketView);
    dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

    dialog.showAndWait();
    loadData();
  }
}
