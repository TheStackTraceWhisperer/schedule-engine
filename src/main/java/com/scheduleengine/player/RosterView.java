package com.scheduleengine.player;

import com.scheduleengine.player.domain.Player;
import com.scheduleengine.team.domain.Team;
import com.scheduleengine.player.service.PlayerService;
import com.scheduleengine.team.service.TeamService;
import com.scheduleengine.common.TablePreferencesUtil;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

public class RosterView {

    private final PlayerService playerService;
    private final TeamService teamService;
    private final com.scheduleengine.navigation.NavigationHandler navigationHandler;
    private TableView<Player> table;
    private ObservableList<Player> data;
    private ComboBox<Team> teamFilter;
    private Team selectedTeam;

    public RosterView(PlayerService playerService, TeamService teamService, com.scheduleengine.navigation.NavigationHandler navigationHandler) {
        this.playerService = playerService;
        this.teamService = teamService;
        this.navigationHandler = navigationHandler;
        this.data = FXCollections.observableArrayList();
    }

    public VBox getView() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        HBox topBox = new HBox(10);
        String titleText = selectedTeam != null ?
            "Roster - " + selectedTeam.getName() : "Team Rosters";
        Label title = new Label(titleText);
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        teamFilter = new ComboBox<>(FXCollections.observableArrayList(teamService.findAll()));
        teamFilter.setPromptText("Select a Team");
        teamFilter.setConverter(new StringConverter<>() {
            @Override public String toString(Team t) { return t == null ? "" : t.getName(); }
            @Override public Team fromString(String s) { return null; }
        });
        teamFilter.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Team item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        teamFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            selectedTeam = newVal;
            loadData();
        });

        // If a team filter is already set, pre-select it in the combo box
        if (selectedTeam != null) {
            teamFilter.setValue(selectedTeam);
        }

        Button addButton = new Button("Add Player");
        addButton.setStyle("-fx-background-color: #667eea; -fx-text-fill: white;");
        addButton.setOnAction(e -> {
            if (selectedTeam == null) {
                showError("Select Team", "Please select a team first.");
                return;
            }
            showAddDialog();
        });

        topBox.getChildren().addAll(title, spacer, new Label("Team:"), teamFilter, addButton);

        table = new TableView<>();
        table.setItems(data);
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        TableColumn<Player, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);
        idCol.setVisible(false);

        TableColumn<Player, String> firstNameCol = new TableColumn<>("First Name");
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        firstNameCol.setPrefWidth(140);

        TableColumn<Player, String> lastNameCol = new TableColumn<>("Last Name");
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        lastNameCol.setPrefWidth(140);

        TableColumn<Player, Integer> jerseyCol = new TableColumn<>("Jersey #");
        jerseyCol.setCellValueFactory(new PropertyValueFactory<>("jerseyNumber"));
        jerseyCol.setPrefWidth(100);

        TableColumn<Player, String> positionCol = new TableColumn<>("Position");
        positionCol.setCellValueFactory(new PropertyValueFactory<>("position"));
        positionCol.setPrefWidth(140);

        TableColumn<Player, String> teamCol = new TableColumn<>("Team");
        teamCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(
            c.getValue().getTeam() != null ? c.getValue().getTeam().getName() : ""
        ));
        teamCol.setPrefWidth(140);

        TableColumn<Player, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(150);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("View Details");
            {
                viewBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white;");
                viewBtn.setOnAction(e -> {
                    Player player = getTableView().getItems().get(getIndex());
                    if (navigationHandler != null) {
                        com.scheduleengine.navigation.NavigationContext newContext =
                            new com.scheduleengine.navigation.NavigationContext()
                                .navigateTo("rosters", "Rosters")
                                .navigateTo("player-detail", player.getFullName(), player);
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

        table.getColumns().addAll(idCol, firstNameCol, lastNameCol, jerseyCol, positionCol, teamCol, actionCol);

        // Setup column width persistence
        TablePreferencesUtil.setupTableColumnPersistence(table, "roster.table");

        loadData();

        vbox.getChildren().addAll(topBox, table);
        VBox.setVgrow(table, Priority.ALWAYS);

        return vbox;
    }

    public void refresh() {
        loadData();
    }

    /**
     * Set the team to filter roster by
     */
    public void setFilterTeam(Team team) {
        this.selectedTeam = team;
    }

    /**
     * Clear any team filter
     */
    public void clearFilter() {
        this.selectedTeam = null;
    }

    private void loadData() {
        data.clear();
        if (selectedTeam != null) {
            data.addAll(playerService.findByTeamId(selectedTeam.getId()));
        }
    }

    private void showAddDialog() {
        Dialog<Player> dialog = new Dialog<>();
        dialog.setTitle("Add Player");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First Name");
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");
        Spinner<Integer> jerseySpinner = new Spinner<>(0, 99, 0);
        TextField positionField = new TextField();
        positionField.setPromptText("Position (e.g., Forward, Midfielder)");

        grid.add(new Label("First Name:"), 0, 0);
        grid.add(firstNameField, 1, 0);
        grid.add(new Label("Last Name:"), 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(new Label("Jersey Number:"), 0, 2);
        grid.add(jerseySpinner, 1, 2);
        grid.add(new Label("Position:"), 0, 3);
        grid.add(positionField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveButtonType) {
                if (firstNameField.getText().isBlank() || lastNameField.getText().isBlank()) {
                    showError("Validation", "First and last names are required.");
                    return null;
                }
                Player player = new Player();
                player.setFirstName(firstNameField.getText());
                player.setLastName(lastNameField.getText());
                player.setJerseyNumber(jerseySpinner.getValue());
                player.setPosition(positionField.getText());
                player.setTeam(selectedTeam);
                return player;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(player -> {
            playerService.save(player);
            loadData();
        });
    }


    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}

