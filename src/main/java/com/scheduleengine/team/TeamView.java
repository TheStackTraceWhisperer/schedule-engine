package com.scheduleengine.team;

import com.scheduleengine.league.domain.League;
import com.scheduleengine.team.domain.Team;
import com.scheduleengine.team.service.TeamService;
import com.scheduleengine.league.service.LeagueService;
import com.scheduleengine.common.DialogUtil;
import com.scheduleengine.common.TablePreferencesUtil;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class TeamView {

    private final TeamService teamService;
    private final LeagueService leagueService;
    private TableView<Team> table;
    private ObservableList<Team> data;
    private com.scheduleengine.navigation.NavigationHandler navigationHandler;
    private ComboBox<League> leagueFilter;
    private League filterLeague; // League to filter by, if any

    public TeamView(TeamService teamService, LeagueService leagueService) {
        this.teamService = teamService;
        this.leagueService = leagueService;
        this.data = FXCollections.observableArrayList();
    }

    /**
     * Set the navigation handler for drill-down navigation
     */
    public void setNavigationHandler(com.scheduleengine.navigation.NavigationHandler navigationHandler) {
        this.navigationHandler = navigationHandler;
    }

    public VBox getView() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        HBox topBox = new HBox(10);
        String titleText = filterLeague != null ?
            "Teams - " + filterLeague.getName() : "Teams";
        Label title = new Label(titleText);
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        leagueFilter = new ComboBox<>(FXCollections.observableArrayList(leagueService.findAll()));
        leagueFilter.setPromptText("Filter by League");
        leagueFilter.setConverter(new StringConverter<>() {
            @Override public String toString(League l) { return l == null ? "" : l.getName(); }
            @Override public League fromString(String s) { return null; }
        });
        leagueFilter.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(League item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        leagueFilter.valueProperty().addListener((obs, oldVal, newVal) -> loadData());

        // If a league filter is already set, pre-select it in the combo box
        if (filterLeague != null) {
            leagueFilter.setValue(filterLeague);
        }

        Button clearFilter = new Button("Clear");
        clearFilter.setOnAction(e -> { leagueFilter.setValue(null); loadData(); });

        Button addButton = new Button("Add Team");
        addButton.setStyle("-fx-background-color: #667eea; -fx-text-fill: white;");
        addButton.setOnAction(e -> showAddDialog());

        topBox.getChildren().addAll(title, spacer, new Label("League:"), leagueFilter, clearFilter, addButton);

        table = new TableView<>();
        table.setItems(data);
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        TableColumn<Team, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);
        idCol.setVisible(false);

        TableColumn<Team, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(180);

        TableColumn<Team, String> coachCol = new TableColumn<>("Coach");
        coachCol.setCellValueFactory(new PropertyValueFactory<>("coach"));
        coachCol.setPrefWidth(140);


        TableColumn<Team, String> leagueCol = new TableColumn<>("League");
        leagueCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
            cell.getValue().getLeague() != null ? cell.getValue().getLeague().getName() : ""
        ));
        leagueCol.setPrefWidth(180);

        TableColumn<Team, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(150);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("View Details");
            {
                viewBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white;");
                viewBtn.setOnAction(e -> {
                    Team team = getTableView().getItems().get(getIndex());
                    if (navigationHandler != null) {
                        com.scheduleengine.navigation.NavigationContext newContext =
                            new com.scheduleengine.navigation.NavigationContext()
                                .navigateTo("teams", "Teams")
                                .navigateTo("team-detail", team.getName(), team);
                        navigationHandler.navigate(newContext);
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(6, viewBtn);
                    setGraphic(box);
                }
            }
        });

        table.getColumns().addAll(idCol, nameCol, coachCol, leagueCol, actionCol);

        // Setup column width persistence
        TablePreferencesUtil.setupTableColumnPersistence(table, "team.table");

        loadData();

        vbox.getChildren().addAll(topBox, table);
        VBox.setVgrow(table, Priority.ALWAYS);

        return vbox;
    }

    public void refresh() {
        loadData();
    }

    /**
     * Set the league to filter teams by
     */
    public void setFilterLeague(League league) {
        this.filterLeague = league;
    }

    /**
     * Clear any league filter
     */
    public void clearFilter() {
        this.filterLeague = null;
    }

    private void loadData() {
        data.clear();
        League selected = leagueFilter != null ? leagueFilter.getValue() : filterLeague;
        if (selected != null) {
            data.addAll(teamService.findByLeagueId(selected.getId()));
        } else {
            data.addAll(teamService.findAll());
        }
    }

    private void showAddDialog() {
        Dialog<Team> dialog = new Dialog<>();
        dialog.setTitle("Add Team");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Configure columns: label column fixed, field column grows
        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setMinWidth(Region.USE_PREF_SIZE);
        ColumnConstraints fieldCol = new ColumnConstraints();
        fieldCol.setHgrow(Priority.ALWAYS);
        fieldCol.setMinWidth(350);
        grid.getColumnConstraints().addAll(labelCol, fieldCol);

        TextField nameField = new TextField();
        nameField.setMaxWidth(Double.MAX_VALUE);
        TextField coachField = new TextField();
        coachField.setMaxWidth(Double.MAX_VALUE);
        TextField emailField = new TextField();
        emailField.setMaxWidth(Double.MAX_VALUE);
        ComboBox<League> leagueCombo = new ComboBox<>(FXCollections.observableArrayList(leagueService.findAll()));
        leagueCombo.setMaxWidth(Double.MAX_VALUE);
        configureLeagueCombo(leagueCombo);

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Coach:"), 0, 1);
        grid.add(coachField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("League:"), 0, 3);
        grid.add(leagueCombo, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // Make dialog resizable and persist size
        dialog.getDialogPane().getScene().getWindow().setOnShown(e ->
            DialogUtil.makeResizable(dialog, "team.add", 600, 450));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (nameField.getText().isBlank()) {
                    showError("Validation", "Name is required");
                    return null;
                }
                Team team = new Team();
                team.setName(nameField.getText());
                team.setCoach(coachField.getText());
                team.setContactEmail(emailField.getText());
                team.setLeague(leagueCombo.getValue());
                return team;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(team -> {
            teamService.save(team);
            loadData();
        });
    }



    private void configureLeagueCombo(ComboBox<League> combo) {
        combo.setConverter(new StringConverter<>() {
            @Override
            public String toString(League object) {
                return object == null ? "" : object.getName();
            }
            @Override
            public League fromString(String string) { return null; }
        });
        Callback<ListView<League>, ListCell<League>> cellFactory = lv -> new ListCell<>() {
            @Override
            protected void updateItem(League item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        };
        combo.setCellFactory(cellFactory);
        combo.setButtonCell(cellFactory.call(null));
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
